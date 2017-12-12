/*
 * Copyright (C) 2017 Viktor Bahr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package de.cco.jaer.eval;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Client for handling communication of evaluation result
 * Opens TCP socket and sends messaged to FastEventServer
 * Replaces direct serial connection via ArduinoConnector.java
 * 
 * @author viktor
 */
public final class FastEventClient {
    
    /**
     * Singelton instance
     */
    private static volatile FastEventClient instance = null;
    
    /**
     * Predefined messages for communicating with FastEventServer
     * 
     * SNYC_ON      Started DVS recording in jAER
     * SYNC_OFF     Stopped DVS recording in jAER
     * LASER_ON     Successfull evaluation, turn on optogenetic laser
     * LASER_OFF    Evaluated event did not exceed threshold, disable optogenetic laser
     * OKAY         Server acknowledges message reception
     * CLOSE        Tell the Server to shut down TCP connection
     */
    public final String SNYC_ON = "1";
    public final String SYNC_OFF = "2";
    public final String LASER_ON = "A";
    public final String LASER_OFF = "B";
    public final String OKAY = "Y";
    public final String CLOSE = "Q";
    
    /**
     * Default TCP port
     */
    static final int default_port = 666;
    
    /**
     * TCP socket to FastEventServer
     */
    Socket socket;
    
    /**
     * Output stream object,
     * write newline seperated messages to socket
     */
    PrintWriter out;
    
    /**
     * Input stream object,
     * buffered read from socket
     */
    BufferedReader in;
    
    
    /**
     * Is socket connected to FastEventServer?
     */
    private boolean connected;
    
    private FastEventClient() {}

    /**
     * Singelton object pseudo constructor,
     * connect to default port when first creating the instance
     * 
     * @return Singelton instance
     */
    public static FastEventClient getInstance() {
        FastEventClient tmp = instance;
        if (tmp == null) {
            synchronized(FastEventClient.class) {
                tmp = instance;
                if (tmp == null) {
                    instance = tmp = new FastEventClient();
                    tmp.in = null;
                    tmp.out = null;
                    tmp.socket = null;
                    tmp.connected = false;
                    tmp.connect("localhost", default_port);
                }
            }
        }
        return tmp;
    }
    
    /**
     * Try to connect to FastEventServer via TCP-Socket
     * 
     * @param host FastEventServer host adress
     * @param port FastEventServer port
     */
    public synchronized void connect(String host, int port) {
        if (isConnected()) {
            return;
        }
        try {
            // connect to server
            socket = new Socket(host, port);
            // open output stream writer with automatic flushing
            out = new PrintWriter(socket.getOutputStream(), true);
            // open input stream object
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + host + ".");
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                host + ".");
        }
    }
    
    /**
     * Disconnect from FastEventServer
     */
    public synchronized void disconnect() {
        if (isConnected()) {
            try {
                out.println(CLOSE); // send shutdown message
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                System.err.println("Failed to disconnect sockets.");
            }
        }
    }
    
    /**
     * @return Is client connected to FastEventServer?
     */
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * Send message to FastEventServer
     * 
     * @param msg Message to send to FastEventServer
     * @return True, if server acknowledged message reception
     */
    public boolean send(String msg) {
        if (!isConnected()) {
            return false;
        }
        out.write(msg);
        out.flush(); // TODO: Do we really need to flush every time?
        try {
            String recv = in.readLine(); // expects newline char at the end
            if (recv.equals(OKAY)) {
                return true;
            }
        } catch (IOException ex) {
            System.err.println("Error reading return message from server.");
            return false;
        }
        return false;
    }
}
