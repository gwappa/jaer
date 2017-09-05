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
    
    // singleton instance
    private static volatile ResultEvaluator instance = null;
    
    private ResultEvaluator() {}
    
    OutputHandler out;
    ArduinoConnector con;
    TrackerParams param;
    EvaluatorThreshold thresh;
    
    private boolean armed;
    private boolean drawing;
    
    public static ResultEvaluator getInstance() {
        ResultEvaluator tmp = instance;
        if (tmp == null) {
            synchronized(ResultEvaluator.class) {
                tmp = instance;
                if (tmp == null) {
                    instance = tmp = new ResultEvaluator();
                    tmp.con = null;
                    tmp.param = null;
                    tmp.out = null;
                    tmp.thresh = null;
                }
            }
        }
        return tmp;
    }
    
    /**
     * Ininitalise ResultEvaluator, no logging
     */
    public synchronized void initialize(TrackerParams param, EvaluatorThreshold thresh){
        this.param = param;
        con = ArduinoConnector.getInstance();
        if (out != null) {
            out.close();
        }
        out = new OutputHandler(OutputHandler.OutputSource.CONSOLE, param.getName(), param.printHeader());
        this.thresh = thresh;
    }
    
    /**
     * Initialise ResultEvaluator, use specified OutputSource
     * @param t Template object extends ParameterTracker interface
     * @param src OutputSource enum, if FILE -> create new filename
     */
    public synchronized void initialize(TrackerParams param, EvaluatorThreshold thresh, OutputHandler.OutputSource src) {
        this.param = param;
        con = ArduinoConnector.getInstance();
        if (out != null) {
            out.close();
        }
        out = new OutputHandler(src, param.getName(), param.printHeader());
        this.thresh = thresh;
    }
    
    /**
     * Inintialise ResultEvaluator, log to specified path
     * @param t Template object extends ParameterTracker interface
     * @param path Path to log file 
     */
    public synchronized void initialize(TrackerParams param, EvaluatorThreshold thresh, String path) {
        this.param = param;
        con = ArduinoConnector.getInstance();
        if (out != null) {
            out.close();
        }
        out = new OutputHandler(path);
        out.write(param.printHeader());
        this.thresh = thresh;
    }

    /**
     * Evaluate result and send signal to Arduino.
     */
    public void eval() {
        if (!isArmed()) {return;}
        out.write(param.print());

        if (param.eval(getThreshold())){
            con.send(con.LASER_ON);
        }
        else{
            con.send(con.LASER_OFF);
        }
    }
    
    public synchronized void arm(boolean b) {
        armed = b;
    }
    
    public boolean isArmed() {
        return armed;
    }
    
    public synchronized void draw(boolean b) {
        drawing = b;
    }
    
    public boolean isDrawing() {
        return drawing;
    }
    
    /**
     * Set evaluator threshold
     * @param thresh
     */
    public synchronized void setThreshold (EvaluatorThreshold thresh) {
        this.thresh = thresh;
    }
    
    public OutputHandler getOutputHandler() {
        return out;
    }
    
    public TrackerParams getParams() {
        return param;
    }
    
    public EvaluatorThreshold getThreshold() {
        return thresh;
    }
    
    public boolean isListening() {
        if (!isArmed()) {
            return false;
        }
        else {
            return getOutputHandler().isListening();
        }
    }
    
}
