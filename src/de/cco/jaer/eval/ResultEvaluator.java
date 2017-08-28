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
 * Generic class, handles TrackerParams, ArduinoConnector and OutputHandle objects
 * 
 * @author viktor
 */
public class ResultEvaluator{
    
    OutputHandler out;
    ArduinoConnector con;
    TrackerParams type;
    
    private double thresh;
    /**
     * Creates a new instance of ResultEvaluator, no logging
     * @param t Template object extends ParameterTracker interface
     */
    public ResultEvaluator(TrackerParams t){
        type = t;
        con = ArduinoConnector.getInstance();
        out = new OutputHandler(OutputHandler.OutputSource.CONSOLE, t.getName(), t.printHeader());
    }
    
    /**
     * Create a new instance of ResultEvaluator, use specified OutputSource
     * @param t Template object extends ParameterTracker interface
     * @param src OutputSource enum, if FILE -> create new filename
     */
    public ResultEvaluator(TrackerParams t, OutputHandler.OutputSource src) {
        type = t;
        con = ArduinoConnector.getInstance();
        out = new OutputHandler(src, t.getName(), t.printHeader());
    }
    
    /**
     * Create a new instance of ResultEvaluator, log to specified path
     * @param t Template object extends ParameterTracker interface
     * @param path Path to log file 
     */
    public ResultEvaluator(TrackerParams t, String path) {
        type = t;
        con = ArduinoConnector.getInstance();
        out = new OutputHandler(path);
        out.write(type.printHeader());
    }

    /**
     * Evaluate result and send signal to Arduino.
     */
    public void eval() {
        out.write(type.print());

        if (type.eval()){
            con.send(con.LASER_ON);
        }
        else{
            con.send(con.LASER_OFF);
        }
    }
    
    /**
     * Set evaluator threshold
     * @param t
     */
    public void setThreshold (double t) {
        type.setThreshold(t);
    }
    
    public OutputHandler getOutputHandler() {
        return out;
    }
    
    public double getThreshold() {
        return type.getThreshold();
    }
    
    public boolean isListening() {
        return getOutputHandler().isListening();
    }
    
}
