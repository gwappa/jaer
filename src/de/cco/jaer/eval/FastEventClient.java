/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cco.jaer.eval;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author viktor
 */
public final class FastEventClient {
    
    private static volatile FastEventClient instance = null;
    
    // hardcoded messages
    public final String SNYC_ON = "1";
    public final String SYNC_OFF = "2";
    public final String LASER_ON = "A";
    public final String LASER_OFF = "B";
    public final String OKAY = "Y";
    
    // tcp socket to server
    Socket socket;
    // Output stream object
    PrintWriter out;
    // Input stream object
    BufferedReader in;
    
    
    // boolean that states socket connection state
    private boolean connected;
    
    private FastEventClient() {}

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
                }
            }
        }
        return tmp;
    }
    
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
    
    public synchronized void disconnect() {
        if (isConnected()) {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                System.err.println("Failed to disconnect sockets.");
            }
        }
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public boolean send(String msg) {
        if (!isConnected()) {
            return false;
        }
        out.write(msg);
        out.flush();
        try {
            String recv = in.readLine();
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
