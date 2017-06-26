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
 * Evaluate results, handle ArduinoConnector and OutputHandle objects
 * 
 * @author viktor
 * @param <T> T extends TrackerParams 
 */
public class ResultEvaluator<T extends TrackerParams>{
    
    OutputHandler out;
    ArduinoConnector con;
    T type;
    
    final String ON = "y";
    final String OFF = "n";
    
    /**
     * Creates a new instance of ResultEvaluator
     * @param t Template object extends ParameterTracker interface
     */
    public ResultEvaluator(T t){
        type = t;
        con = new ArduinoConnector();
        out = new OutputHandler();
        out.write(type.printHeader());
    }
    
    /**
     * Create a new instance of ResultEvaluator
     * @param t Template object extends ParameterTracker interface
     * @param src OutputSource enum, if FILE -> create new filename
     */
    public ResultEvaluator(T t, OutputHandler.OutputSource src) {
        type = t;
        con = new ArduinoConnector();
        out = new OutputHandler(src);
        out.write(type.printHeader());
    }
    
    /**
     * Create a new instance of ResultEvaluator
     * @param t Template object extends ParameterTracker interface
     * @param path Path to log file 
     */
    public ResultEvaluator(T t, String path) {
        type = t;
        con = new ArduinoConnector();
        out = new OutputHandler(path);
        out.write(type.printHeader());
    }

    /**
     * Evaluate result and send signal to Arduino.
     */
    public void eval() {
        out.write(type.print());

        if (type.eval()){
            con.send(ON);
        }
        else{
            con.send(OFF);
        }
    }
}
