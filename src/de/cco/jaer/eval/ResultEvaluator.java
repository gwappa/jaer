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

import java.beans.PropertyChangeSupport;

/**
 * Generic class, handles TrackerParams, ArduinoConnector and OutputHandle
 * objects
 *
 * @author viktor
 */
public class ResultEvaluator {

    // singleton instance
    private static volatile ResultEvaluator instance = null;

    private ResultEvaluator() {
    }

    private OutputHandler out;
    private FastEventClient client;
    private TrackerParams param;
    private EvaluatorThreshold thresh;
    private EvaluatorFrame frame;

    private boolean armed;
    private boolean drawing;
    private boolean event;

    public static ResultEvaluator getInstance() {
        ResultEvaluator tmp = instance;
        if (tmp == null) {
            synchronized (ResultEvaluator.class) {
                tmp = instance;
                if (tmp == null) {
                    instance = tmp = new ResultEvaluator();
                    tmp.client = FastEventClient.getInstance();
                    tmp.param = null;
                    tmp.out = null;
                    tmp.thresh = null;
                    tmp.event = false;
                    tmp.frame = null;
                }
            }
        }
        return tmp;
    }

    /**
     * Ininitalise ResultEvaluator, no logging
     */
    public synchronized void initialize(TrackerParams param, EvaluatorThreshold thresh) {
        this.param = param;
        client = FastEventClient.getInstance();
        
        if (out != null) {
            out.close();
        }
        out = new OutputHandler(OutputHandler.OutputSource.CONSOLE, param.getName(), param.printHeader());
        this.thresh = thresh;
    }

    /**
     * Initialise ResultEvaluator, use specified OutputSource
     *
     * @param param Template object extends ParameterTracker interface
     * @param src OutputSource enum, if FILE -> create new filename
     */
    public synchronized void initialize(TrackerParams param, EvaluatorThreshold thresh, OutputHandler.OutputSource src) {
        this.param = param;
        client = FastEventClient.getInstance();
        if (out != null) {
            out.close();
        }
        out = new OutputHandler(src, param.getName(), param.printHeader());
        this.thresh = thresh;
    }

    /**
     * Inintialise ResultEvaluator, log to specified path
     *
     * @param param Template object extends ParameterTracker interface
     * @param path Path to log file
     */
    public synchronized void initialize(TrackerParams param, EvaluatorThreshold thresh, String path) {
        this.param = param;
        client = FastEventClient.getInstance();
        if (out != null) {
            out.close();
        }
        out = new OutputHandler(path);
        out.write(param.printHeader());
        this.thresh = thresh;
    }
    
    public void attachFilterStateListener(PropertyChangeSupport pcs) {
        if (out != null) {
            out.attachFilterStateListener(pcs);
        }
        if (frame != null) {
            frame.attachFilterStateListener(pcs);
        }
    }

    /**
     * Evaluate result and send signal to Arduino.
     */
    public void eval() {
        if (!isArmed()) {
            return;
        }

        if (param.eval(getThreshold())) {
            event = true;
            client.send(client.LASER_ON);
            out.write(param.print() + ",1");
        } else {
            event = false;
            client.send(client.LASER_OFF);
            out.write(param.print() + ",0");
        }
    }
    
    public synchronized void draw(boolean b) {
        drawing = b;
    }

    public synchronized void arm(boolean b) {
        armed = b;
    }
    
    public FastEventClient getFastEventClient() {
        return client;
    }
    
    public EvaluatorFrame getEvaluatorFrame() {
        return frame;
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

    public boolean isArmed() {
        return armed;
    }
    
    public boolean isListening() {
        if (frame == null || out == null) {
            return false;
        } else {
            return frame.isListening() && out.isListening();
        }
    }

    public boolean isDrawing() {
        return drawing;
    }

    public boolean isEvent() {
        return event;
    }
    
    /**
     * Set evaluator threshold
     *
     * @param thresh
     */
    public synchronized void setThreshold(EvaluatorThreshold thresh) {
        this.thresh = thresh;
    }
    
    /**
     * Set EvaluatorFrame GUI instance
     * @param frame 
     */
    public synchronized void setEvaluatorFrame(EvaluatorFrame frame) {
        this.frame = frame;
    }
    
}
