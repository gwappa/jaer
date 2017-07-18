/*
 * Copyright (C) 2017 viktor
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

import java.beans.PropertyChangeEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Handle output of tracker results.
 * Log to file, console or do nothing.
 * @author viktor
 */
public class OutputHandler {
    
    // output source
    OutputSource outsrc;
    
    // output file object
    BufferedWriter outstream;
    
    PropertyChangeSupport pcs;
    PropertyChangeListener li = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            if (pce.getPropertyName().equals("filterEnabled")) {
                boolean val = (boolean) pce.getNewValue();
                if (val == false) {
                    try {
                        System.out.println("Filter disabled.");
                        System.out.println("Closing BufferedWriter.");
                        outstream.close();
                    } catch (IOException ex) {
                        Logger.getLogger(OutputHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                else if (val == true) {
                    System.out.println("Filter enabled.");
                    String path = genFileName();
                    System.out.println("Saving data to new file '" + path + "'");
                    outstream = openFile(path);
                }
            }
        }
    };
    
    private String name, header;
    private int linecount = 0;
    private boolean listening = false;
    
    public enum OutputSource{

        /**
         * Log data to file.
         */
        FILE,

        /**
         * Log data to STDOUT
         */
        CONSOLE,

        /**
         * Log data to STDOUT
         */
        NONE
    }
    
    /**
     * Create new instance of OutputHandler
     * @param str String with path to logfile
     */
    public OutputHandler(String str){
        setOutput(str);
        setName("Unknown");
        setHeader("");
    }
    
    /**
     * Create new instance of OutputHandler
     * @param src OutputSource enum
     */
    public OutputHandler(OutputSource src){
        setName("Unknown");
        setHeader("");
        setOutput(src);
    }
    
    public OutputHandler(OutputSource src, String name, String header) {
        setName(name);
        setHeader(header);
        setOutput(src);
    }
    
    public OutputHandler(String path, String name, String header) {
        setName(name);
        setHeader(header);
        setOutput(path);
    }
    
    public synchronized final void setOutput(OutputSource src) {
        this.outsrc = src;
        if (src == OutputSource.FILE){
            String path = genFileName();
            System.out.println("Saving data to '" + path + "'");
            outstream = openFile(path);
            write(getHeader());
        }
    }

    public synchronized final void setOutput(String str) {
        System.out.println("Saving data to '" + str + "'");
        outsrc = OutputSource.FILE;
        outstream = openFile(str);
        write(getHeader());
    }
    
    public synchronized final void setName(String n) {
        name = n;
    }
    
    public synchronized final void setHeader(String h) {
        header = h;
    }
    
    public synchronized String getName() {
        return name;
    }
    
    public synchronized String getHeader() {
        return header;
    }
    
    public synchronized void write(String str) {
        switch (outsrc){
            case CONSOLE:
                System.out.println(str);
                linecount++;
                break;
            case FILE:
                try {
                    outstream.write(str + "\n");
                    outstream.flush();
                    linecount++;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                break;
            case NONE:
                break;
        }
        
    }
    
    /**
     * Generate new filename from current date and working directory.
     * @return String, new file name.
     */
    private synchronized String genFileName(){
        Path current = Paths.get(System.getProperty("user.dir"));
        Date d = new Date();
        DateFormat dformat = new SimpleDateFormat("ddMMyyyyHHmmss");
        Path path = Paths.get(current.toString(), getName() + "_" + dformat.format(d) + ".log");
        return path.toString();
    }
    
    /**
     * Open new log file.
     * 
     * @param path Path to new file.
     * @return BufferedWriter object to new log file or null.
     */
    private synchronized BufferedWriter openFile(String path){
        BufferedWriter bw = null;
        try{           
            FileWriter fw = new FileWriter(path);
            bw = new BufferedWriter(fw);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return bw;
    }
    
    public boolean isListening() {
        return listening;
    }
    
    public synchronized void attachCustomListener(PropertyChangeSupport s) {
        if (pcs != null) {
            return;
        }
        pcs = s;
        pcs.addPropertyChangeListener(li);
        listening = true;
    }
    
    public synchronized void removeCustomListener(PropertyChangeSupport pcs) {
        pcs.removePropertyChangeListener(li);
        listening = false;
    }
    
    protected synchronized void finalize() {
        if (outsrc == OutputSource.FILE){
            try {
                outstream.close();
            } catch (IOException ex) {
                Logger.getLogger(OutputHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
