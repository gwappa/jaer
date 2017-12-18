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

/**
 * Handle start/stop of jAER data logging. Store timestamps, write them to
 * file/console and to FastEventServer.
 *
 * @author viktor
 * @see OutputHandler
 * @see FastEventServer
 */
public class SyncEventHandler {

    /**
     * Singleton instance
     */
    private static volatile SyncEventHandler instance = null;

    /**
     * Handle output to CSV file / stdout
     */
    OutputHandler out;

    /**
     * TCP connection to FastEventServer
     */
    FastEventClient client;

    /**
     * Private class constructor, called from getInstance()
     */
    private SyncEventHandler() {
        client = FastEventClient.getInstance();
        out = new OutputHandler(OutputHandler.OutputSource.FILE,
                "SyncEvents",
                "sync,system");
    }

    /**
     * Get singelton instance, initialise in case unitialised
     *
     * @return Singelton instance
     */
    public static SyncEventHandler getInstance() {
        SyncEventHandler tmp = instance;
        if (tmp == null) {
            synchronized (SyncEventHandler.class) {
                tmp = instance;
                if (tmp == null) {
                    instance = tmp = new SyncEventHandler();
                }
            }
        }
        return tmp;
    }

    /**
     * jAER data logging started, log current time to file/stdout and TCP
     */
    public void on() {
        long system = System.currentTimeMillis();
        out.write("1," + system);
        client.send(client.SNYC_ON);
    }

    /**
     * jAER data logging stopped, log current time to file/stdout and TCP
     */
    public void off() {
        long system = System.currentTimeMillis();
        out.write("0," + system);
        client.send(client.SYNC_OFF);
    }

    /**
     * Close open logfile
     */
    public void close() {
        out.close();
    }

}
