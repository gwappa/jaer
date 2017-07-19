/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cco.jaer.eval;

/**
 * Handle connection to Arduino board.
 * 
 * @author viktor
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier; 
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent; 
import gnu.io.SerialPortEventListener; 
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class ArduinoConnector implements SerialPortEventListener {
    
    // singleton instance
    private static volatile ArduinoConnector instance = null;
    
    // hardcoded messages
    public final String SNYC_ON = "0";
    public final String SYNC_OFF = "1";
    public final String LASER_ON = "A";
    public final String LASER_OFF = "B";
    
    SerialPort serialPort;
    /** The port we're normally going to use. */
    private static final String PORT_NAMES[] = { 
                    "/dev/tty.usbserial-A9007UX1", // Mac OS X
                    "/dev/ttyACM0", // Linux
                    "/dev/ttyACM1", // Linux
                    "/dev/ttyUSB0", // Linux
                    "COM3", // Windows
    };
    /**
    * A BufferedReader which will be fed by a InputStreamReader 
    * converting the bytes into characters 
    * making the displayed results codepage independent
    */
    private BufferedReader input;
    /** The output stream to the port */
    private OutputStream output;
    /** Milliseconds to block while waiting for port open */
    private static final int TIME_OUT = 2000;
    /** Default bits per second for COM port. */
    private static final int DATA_RATE = 9600;

    private boolean state;
    
    private ArduinoConnector() {}

    public static ArduinoConnector getInstance() {
        ArduinoConnector tmp = instance;
        if (tmp == null) {
            synchronized(ArduinoConnector.class) {
                tmp = instance;
                if (tmp == null) {
                    instance = tmp = new ArduinoConnector();
                    instance.initialize();
                }
            }
        }
        return tmp;
    }

    /**
     * Try to set up connection to Arduino board.
     */
    private void initialize() {
            // the next line is for Raspberry Pi and 
            // gets us into the while loop and was suggested here was suggested http://www.raspberrypi.org/phpBB3/viewtopic.php?f=81&t=32186
            System.setProperty("gnu.io.rxtx.SerialPorts", "COM3");

            CommPortIdentifier portId = null;
            Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

            //First, Find an instance of serial port as set in PORT_NAMES.
            while (portEnum.hasMoreElements()) {
                    CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
                    for (String portName : PORT_NAMES) {
                            if (currPortId.getName().equals(portName)) {
                                    portId = currPortId;
                                    break;
                            }
                    }
            }
            if (portId == null) {
                    System.out.println("Could not find COM port.");
                    state = false;
                    return;
            }

            try {
                    // open serial port, and use class name for the appName.
                    serialPort = (SerialPort) portId.open(this.getClass().getName(),
                                    TIME_OUT);

                    // set port parameters
                    serialPort.setSerialPortParams(DATA_RATE,
                                    SerialPort.DATABITS_8,
                                    SerialPort.STOPBITS_1,
                                    SerialPort.PARITY_NONE);

                    // open the streams
                    input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
                    output = serialPort.getOutputStream();

                    // add event listeners
                    serialPort.addEventListener(this);
                    serialPort.notifyOnDataAvailable(true);

                    // send test package
                    Thread.sleep(2000);
                    System.out.println("Trying to connect to Arduino and sending 'Hello' packet.");
                    send("Java says 'Hello'");
                    state = true;
            } catch (Exception e) {
                    System.err.println(e.toString());
                    state = false;
            }
    }

    /**
     * This should be called when you stop using the port.
     * This will prevent port locking on platforms like Linux.
     */
    public synchronized void close() {
            if (serialPort != null) {
                    serialPort.removeEventListener();
                    serialPort.close();
            }
    }

    /**
     * Getter for BufferedReader input stream
     * @return Opened BufferedReader 
     */
    public synchronized BufferedReader getIntputStream(){
        return input;
    }

    /**
     * Getter for OutputStreat ouput stream object
     * @return Opened OutputStream 
     */
    public synchronized OutputStream getOutputStream(){
        return output;
    }

    /**
     * Check if connection to Arduino is established
     * @return boolean, true if connection is established
     */
    public boolean isConnected(){
        return state;
    }

    /**
     * Try to send String to Arduino board.
     * @param str String to send
     */
    public synchronized void send(String str){
        if (str.isEmpty()) {
            return;
        }
        if (isConnected()){
            try {
                getOutputStream().write(str.getBytes());
            } catch (IOException ex) {
                Logger.getLogger(ArduinoConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("ResultEvaluator sends: '" + str + "'");
        }
    }

    /**
     * Handle an event on the serial port. Read the data and print it.
     * @param oEvent
     */
    @Override
    public synchronized void serialEvent(SerialPortEvent oEvent) {
            if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
                    try {
                            String inputLine=input.readLine();
                            System.out.println("Uno says: " + inputLine);
                    } catch (Exception e) {
                            System.err.println(e.toString());
                    }
            }
            // Ignore all the other eventTypes, but you should consider the other ones.
    }

    protected void finalize() {
        close();
    }
}
