/*
 * Copyright (C) 2017 Viktor Bahr, Keisuke Sehara
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * the static interface for obtaining FastEvent-specific settings.
 *
 * @author gwappa
 */
public class FastEventSettings {
    private static final String HOSTNAME        = "localhost";
    private static final int    SERVICE_PORT    = 11666;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("ddMMyyyyHHmmss");
    private static final Path   BASE_DIR        = Paths.get(System.getProperty("user.dir"), "FastEvent");

    static String getServiceHost(){
        return HOSTNAME;
    }

    static int getServicePort(){
        return SERVICE_PORT;
    }


    /**
     * Generate new filename from current date and working directory. In case it
     * doesn't exist. Create data directory inside local packet.
     *
     * @param baseName      the base name, without a timestamp
     * @param referenceAER  the name of the AER data file that works as a reference
     *                      for the time stamp. can be null.
     *
     * @return String, new file name.
     */
    public static String generateFileName(String baseName, String referenceAER) {
        if (!Files.exists(BASE_DIR)) {
            // create base directory
            try {
                Files.createDirectory(BASE_DIR);
            } catch (IOException e) {
                Logger.getLogger(FastEventSettings.class.getName()).log(Level.SEVERE, null, e);
            }
        }

        // get time stamp
        String timestamp = null;
        if (referenceAER == null) {
            // create String timestamp by formatting the current time
            Date date = new Date();
            timestamp = DATE_FORMAT.format(date);
        } else {
            // extract timestamp fraction from the existing ".aedat" file.
            timestamp = referenceAER.substring(referenceAER.indexOf("-"),
                                                referenceAER.length() - 6);
        }
        return Paths.get(BASE_DIR.toString(), 
                        String.format("%s_%s.log", baseName, timestamp))
                    .toString();
    }

    /**
     * Generate new Writer object from current date and working directory.
     * In case it doesn't exist. Create data directory inside local packet.
     *
     * @param baseName      the base name, without a timestamp
     * @param referenceAER  the name of the AER data file that works as a reference
     *                      for the time stamp. can be null.
     *
     * @return the Writer object that points to the created file.
     *          returns null if it fails to open a Writer.
     */
    public static Writer generateWriter(String baseName, String referenceAER) {
        try {
            return new FileWriter(generateFileName(baseName, referenceAER));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }
}
