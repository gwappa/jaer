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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
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
    PropertyChangeListener filterStateListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            if (pce.getPropertyName().equals("filterEnabled")) {
                boolean val = (boolean) pce.getNewValue();
                if (val == false) {
                    System.out.println("Filter disabled.");
                    System.out.println("Closing OutputObject.");
                    OutputHandler.this.close();
                }
                else if (val == true) {
                    System.out.println("Filter enabled.");
                    OutputHandler.this.setOutput(OutputSource.FILE);
                }
            }
        }
    };
    
    private Path current;
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
        setName("Unknown");
        setHeader("");
        setOutput(str);
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
            Path path = genFileName();
            System.out.println("Saving data to '" + path.toString() + "'");
            outstream = openFile(path);
            write(getHeader());
        }
    }

    public synchronized final void setOutput(String str) {
        System.out.println("Saving data to '" + str + "'");
        outsrc = OutputSource.FILE;
        outstream = openFile(Paths.get(str));
        write(getHeader());
    }
    
    public synchronized final void setName(String n) {
        name = n;
    }
    
    public synchronized final void setHeader(String h) {
        header = h;
    }
    
    public Path getPath() {
        return current;
    }
    
    public String getName() {
        return name;
    }
    
    public String getHeader() {
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
     * In case it doesn't exist. Create data directory inside local packet.
     * @return String, new file name.
     */
    private Path genFileName(){
        Path current = Paths.get(System.getProperty("user.dir"), "src", "de", "cco", "jaer", "eval", "data");
        if (!Files.exists(current)) {
            try { // create data directory
                Files.createDirectory(current);
            } catch (IOException ex) {
                Logger.getLogger(OutputHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Date d = new Date();
        DateFormat dformat = new SimpleDateFormat("ddMMyyyyHHmmss");
        Path path = Paths.get(current.toString(), getName() + "_" + dformat.format(d) + ".log");
        return path;
    }
    
    /**
     * Open new log file.
     * 
     * @param path Path to new file.
     * @return BufferedWriter object to new log file or null.
     */
    private synchronized BufferedWriter openFile(Path path){
        BufferedWriter bw = null;
        try{           
            FileWriter fw = new FileWriter(path.toString());
            bw = new BufferedWriter(fw);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        current = path;
        return bw;
    }
    
    public boolean isListening() {
        return listening;
    }
    
    public synchronized void attachFilterStateListener(PropertyChangeSupport s) {
        if (pcs != null) {
            return;
        }
        pcs = s;
        pcs.addPropertyChangeListener(filterStateListener);
        listening = true;
    }
    
    public synchronized void removeFilterStateListener(PropertyChangeSupport pcs) {
        pcs.removePropertyChangeListener(filterStateListener);
        listening = false;
    }
    
    public synchronized void close() {
        if (outsrc == OutputSource.FILE){
            try {
                outstream.close();
            } catch (IOException ex) {
                Logger.getLogger(OutputHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (linecount == 1) {
                try {
                    Files.delete(current);
                } catch (IOException ex) {
                    Logger.getLogger(OutputHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
