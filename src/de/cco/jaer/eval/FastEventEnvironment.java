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
public class FastEventEnvironment {
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
     * @return String, new file name. 
     */
    public static Path genFileName(String baseName) {
        if (!Files.exists(BASE_DIR)) {
            // create base directory
            try {
                Files.createDirectory(BASE_DIR);
            } catch (IOException e) {
                Logger.getLogger(FastEventEnvironment.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        Date date = new Date();
        return Paths.get(BASE_DIR.toString(), String.format("%s_%s.log", baseName, DATE_FORMAT.format(date)));
    }
}
