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


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author viktor
 * @param <T>
 */
public class ResultEvaluator<T extends TrackerParams>{
    
    OutputHandler out;
    Arduino dev;
    T type;
    
    final String ON = "y";
    final String OFF = "n";
    
    /**
     * Creates a new instance of ResultEvaluator
     * @param t
     * @throws java.io.IOException
     */
    public ResultEvaluator(T t) throws IOException, Exception {
        type = t;
        dev = connect();
        out = new OutputHandler();
        out.write(type.printHeader());
    }
    
    public ResultEvaluator(T t, OutputHandler.OutputSource src) throws IOException, Exception {
        type = t;
        dev = connect();
        out = new OutputHandler(src);
        out.write(type.printHeader());
    }
    
    public ResultEvaluator(T t, String path) throws IOException, Exception {
        type = t;
        dev = connect();
        out = new OutputHandler(path);
        out.write(type.printHeader());
    }
    
    public void eval() throws Exception {
        out.write(type.print());
        
        if (type.eval()){
            dev.send(ON);
        }
        else{
            dev.send(OFF);
        }
    }
    
    protected void finalize() {
        dev.close();
    }
    
    private Arduino connect() throws Exception {
        dev = new Arduino();
        dev.initialize();
        Thread.sleep(2000);
        System.out.println("Trying to connect to Arduino and sending 'Hello' packet.");
        dev.send("Java says 'Hello'");
        return dev;
    }
}
