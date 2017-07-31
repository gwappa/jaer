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

/**
 * Handle start/stop of jAER data logging.
 * Store timestamps, write them to file/console and communicate with Arduino. 
 * @author viktor
 */
public class SyncEventHandler {
    
    // singleton instance
    private static volatile SyncEventHandler instance = null;
    
    OutputHandler out;
    ArduinoConnector con;
    
    private SyncEventHandler() {
        con = ArduinoConnector.getInstance();
        out = new OutputHandler(OutputHandler.OutputSource.FILE, 
                "SyncEvents", 
                "sync,system,chip");
    }
    
    public static SyncEventHandler getInstance() {
        SyncEventHandler tmp = instance;
        if (tmp == null) {
            synchronized(SyncEventHandler.class) {
                tmp = instance;
                if (tmp == null) {
                    instance = tmp = new SyncEventHandler();
                }
            }
        }
        return tmp;
    }
    
    
    public void on() {
        long system = System.currentTimeMillis();
        out.write("1," + system);
        con.send(con.SNYC_ON);
    }
    
    public void off() {
        long system = System.currentTimeMillis();
        out.write("0," + system);
        con.send(con.SYNC_OFF);
    }
    
    public void close() {
        out.close();
    }
    
}
