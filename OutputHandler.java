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
     * Create new instance of OutputHandler.
     * Default is no output.
     */
    public OutputHandler(){
        setOutput(OutputSource.NONE);
    }
    
    /**
     * Create new instance of OutputHandler
     * @param str String with path to logfile
     */
    public OutputHandler(String str){
        setOutput(str);
    }
    
    /**
     * Create new instance of OutputHandler
     * @param p Path to new logfile
     */
    public OutputHandler(Path p){
        setOutput(p.toString());
    }
    
    /**
     * Create new instance of OutputHandler
     * @param src OutputSource enum
     */
    public OutputHandler(OutputSource src){
        setOutput(src);
    }
    
    public final void setOutput(OutputSource src) {
        outsrc = src;
        if (src == OutputSource.FILE){
            String path = genFileName();
            System.out.println("Saving data to '" + path + "'");
            outstream = openFile(path);
        }
    }

    public final void setOutput(String str) {
        System.out.println("Saving data to '" + str + "'");
        outstream = openFile(str);
    }
    
    public void write(String str) {
        switch (outsrc){
            case CONSOLE:
                System.out.println(str);
                break;
            case FILE:
                try {
                    outstream.write(str + "\n");
                    outstream.flush();
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
    private String genFileName(){
        Path current = Paths.get(System.getProperty("user.dir"));
        Date d = new Date();
        DateFormat dformat = new SimpleDateFormat("ddMMyyyy-HHmmss");
        Path path = Paths.get(current.toString(), dformat.format(d) + ".log");
        return path.toString();
    }
    
    /**
     * Open new log file.
     * 
     * @param path Path to new file.
     * @return BufferedWriter object to new log file or null.
     */
    private BufferedWriter openFile(String path){
        BufferedWriter bw = null;
        try{
            File file = new File(path);
            if (!file.exists()){
                file.createNewFile();
            }
            
            FileWriter fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return bw;
    }
    
    protected void finalize() {
        if (outsrc == OutputSource.FILE){
            try {
                outstream.close();
            } catch (IOException ex) {
                Logger.getLogger(OutputHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
