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
 * Handle output of tracker results. Log to CSV file, console or do nothing.
 *
 * @author viktor
 */
public class OutputHandler {

    /**
     * Output source type
     *
     * FILE Log data to (CSV) file CONSOLE Log data to STDOUT NONE Log data to
     * STDOUT
     */
    public enum OutputSource {
        FILE,
        CONSOLE,
        NONE
    }

    OutputSource outsrc;

    /**
     * Buffered output stream object
     */
    BufferedWriter outstream;

    /**
     * jAER Filter property change support object
     */
    PropertyChangeSupport pcs;

    /**
     * PropertyChangeListener method attached to jAER filter
     * PropertyChangeSupport object, listening for filter on/off selection,
     * closing OutputHanlder when filter is disabled
     */
    PropertyChangeListener filterStateListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            if (pce.getPropertyName().equals("filterEnabled")) {
                boolean val = (boolean) pce.getNewValue();
                if (val == false) {
                    System.out.println("Filter disabled.");
                    System.out.println("Closing OutputObject.");
                    OutputHandler.this.close();
                } else if (val == true) {
                    System.out.println("Filter enabled.");
                    OutputHandler.this.setOutput(OutputSource.FILE);
                }
            }
        }
    };

    /**
     * Path of current output file
     */
    private Path current;

    /**
     * File name prefix and CSV header
     */
    private String name, header;

    /**
     * Number of lines written
     */
    private int linecount = 0;

    /**
     * Is PropertyChangeListener attached?
     */
    private boolean listening = false;

    /**
     * Create new instance of OutputHandler
     *
     * @param str String with path to logfile
     */
    public OutputHandler(String str) {
        setName("Unknown");
        setHeader("");
        setOutput(str);
    }

    /**
     * Create new instance of OutputHandler
     *
     * @param src OutputSource enum
     */
    public OutputHandler(OutputSource src) {
        setName("Unknown");
        setHeader("");
        setOutput(src);
    }

    /**
     * Create new instance of OutputHandler
     *
     * @param src OutputSource enum
     * @param name File name prefix, filter name
     * @param header CSV file header, "column names"
     */
    public OutputHandler(OutputSource src, String name, String header) {
        setName(name);
        setHeader(header);
        setOutput(src);
    }

    /**
     * Create new instance of OutputHandler
     *
     * @param path Path of output file
     * @param name File name prefix, filter name
     * @param header CSV file header, "column names"
     */
    public OutputHandler(String path, String name, String header) {
        setName(name);
        setHeader(header);
        setOutput(path);
    }

    /**
     * Setter for output source type if FILE, generate new file
     *
     * @param src
     */
    public synchronized final void setOutput(OutputSource src) {
        this.outsrc = src;
        if (src == OutputSource.FILE) {
            Path path = genFileName();
            System.out.println("Saving data to '" + path.toString() + "'");
            outstream = openFile(path);
            write(getHeader());
        }
    }

    /**
     * Setter for output source of type FILE
     *
     * @param str Desired path of output file
     */
    public synchronized final void setOutput(String str) {
        System.out.println("Saving data to '" + str + "'");
        outsrc = OutputSource.FILE;
        outstream = openFile(Paths.get(str));
        write(getHeader());
    }

    /**
     * Setter for file name prefix, filter name
     *
     * @param n File name prefix, filter name
     */
    public synchronized final void setName(String n) {
        name = n;
    }

    /**
     * Setter for (CSV) file header
     *
     * @param h Comma seperated column names
     */
    public synchronized final void setHeader(String h) {
        header = h;
    }

    /**
     * Getter for output file path
     *
     * @return Current path of the output file
     */
    public Path getPath() {
        return current;
    }

    /**
     * Getter for file name prefix, filter name
     *
     * @return File name prefix, filter name
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for (CSV) file header
     *
     * @return Comma seperated column names
     */
    public String getHeader() {
        return header;
    }

    /**
     * Write row to desired output source
     *
     * @param str CSV row, should match attributes defined in header
     */
    public synchronized void write(String str) {
        switch (outsrc) {
            case CONSOLE:
                System.out.println(str);
                linecount++;
                break;
            case FILE:
                try {
                    outstream.write(str + "\n");
                    outstream.flush(); // flush after each row
                    linecount++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case NONE:
                break;
        }

    }

    /**
     * Generate new filename from current date and working directory. In case it
     * doesn't exist. Create data directory inside local packet.
     *
     * @return String, new file name.
     */
    private Path genFileName() {
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
     * Open a new log file.
     *
     * @param path Path to new file.
     * @return BufferedWriter object to new log file or null.
     */
    private synchronized BufferedWriter openFile(Path path) {
        BufferedWriter bw = null;
        try {
            FileWriter fw = new FileWriter(path.toString());
            bw = new BufferedWriter(fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
        current = path;
        return bw;
    }

    /**
     * @return Is PropertyChangeListener attached to jAER filter?
     */
    public boolean isListening() {
        return listening;
    }

    /**
     * Attach PropertyChangeListener to jAER filter PropertyChangeSupport object
     *
     * @param s jAER filter support object, signaling change in filter state to
     * change listener
     */
    public synchronized void attachFilterStateListener(PropertyChangeSupport s) {
        if (pcs != null) {
            removeFilterStateListener(pcs);
        }
        pcs = s;
        pcs.addPropertyChangeListener(filterStateListener);
        listening = true;
    }

    /**
     * Remove PropertyChangeListener from jAER filter PropertyChangeSupport
     * object
     *
     * @param pcs jAER filter support object, signaling change in filter state
     * to change listener
     */
    public synchronized void removeFilterStateListener(PropertyChangeSupport pcs) {
        pcs.removePropertyChangeListener(filterStateListener);
        listening = false;
    }

    /**
     * Close output file stream, delete files with only headers
     */
    public synchronized void close() {
        if (outsrc == OutputSource.FILE) {
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
