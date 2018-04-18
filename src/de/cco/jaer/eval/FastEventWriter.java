/*
 * Copyright (C) 2017-2018 Viktor Bahr, Keisuke Sehara
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

import java.io.Writer;
import java.io.FileWriter;
import java.io.IOException;

/**
 * a class for handling output of tracker results. 
 *
 * <p>
 * FastEventWriter instances handle any IOException's that may occur on the run, for the sake of simplification
 * of the method calls (such as `write()` and `close()`).
 * On the other hand, please beware if the instance actually has its backend (check with `isOpen()`).
 * </p>
 *
 * <p>
 * Methods such as `write()` and `close()` are by default non-synchronized.
 * If you want to make it synchronized between threads, use `synchronizedWrite()` or `synchronizedClose()` instead.
 * </p>
 *
 * <p>
 * Although this class can be used in any line-oriented output patterns,
 * it is priparily intended for output in the form of CSV.
 * This is why the initializers have `headers` parameter.
 * </p>
 *
 * <p>
 * To facilitate the coordination with the logging that is native to jAER, the `FastEventWriter.fromBaseName()` method
 * has the `referenceAER` String parameter.
 * `referenceAER` can take the log file name used in jAER, whose timestamp can then be appended to the name of the log file
 * so that one can easily correspond a set of log files from another.
 * Therefore, it should be useful in combination with the `TrackingDelegate.startLogging(String)` method.
 * </p>
 *
 * @author viktor, gwappa
 */
public class FastEventWriter {
    /**
     * the base name for specifying the output.
     */
    String name_ = null;
    
    /**
     * Output stream object.
     * null, if the output is not open (already closed).
     */
    Writer out_ = null;

    /**
     * creates a new FastEventWriter instance.
     *
     * @param name      the base name of this writer.
     * @param writer    the Writer object for the CSV file.
     * @param header    the headers for the CSV file, in a form of comma-separated String instance
     */
    public FastEventWriter(String name, Writer writer, String header) {
        name_ = name;
        out_ = writer;
        write(header);
    }

    /**
     * generates a new file using the specified base name, the FastEvent-specific directory settings
     * and the name of the AER data file as a reference for the timestamp.
     *
     * `referenceAER` can take the log file name for jAER, whose timestamp can then be appended to the name 
     * of the generated log file so that one can easily correspond a set of log files from the other files.
     *
     * @param baseName      the base name of the CSV file to be generated.
     * @param referenceAER  the name of the reference AER data file. (can be null)
     * @param headers       the comma-separated String instance that represents the headers of the CSV file.
     */
    public static FastEventWriter fromBaseName(String baseName, String referenceAER, String headers) {
        Writer writer = FastEventSettings.generateWriter(baseName, referenceAER);
        if (writer == null) {
            System.out.println("***failed to open writer output to: "+baseName);
        }
        return new FastEventWriter(baseName, writer, headers);
    }

    /**
     * checks if this FastEventWriter instance has the opened file backend.
     */
    public boolean isOpen() {
        return (out_ != null);
    }

    /**
     * Writes a row to the output.
     *
     * @param str a CSV row; the number of its elements should match with those of attributes defined in header
     */
    public void write(String str) {
        // TODO: make 'flush'ing configurable??

        if (out_ != null) {
            try {
                out_.write(str+"\n");
                out_.flush();
            } catch (IOException ioe) {
                System.out.println("***error occurred during writing to: "+name_);
                close();
            }
        } else {
            System.out.println("***output already closed for: "+name_);
        }
    }

    /**
     * Writes a row to the output (synchronized).
     *
     * @param str a CSV row; the number of its elements should match with those of attributes defined in header
     */
    public synchronized void synchronizedWrite(String str) {
        write(str);
    }

    /**
     * Close output file stream.
     */
    public void close() {
        if (out_ != null) {
            try {
                out_.flush();
                out_.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            out_ = null;
        }
    }

    /**
     * Close output file stream (synchronized).
     */
    public synchronized void synchronizedClose() {
        close();
    }
}

