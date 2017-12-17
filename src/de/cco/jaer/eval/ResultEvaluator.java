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

import java.beans.PropertyChangeSupport;

/**
 * Evaluate tracker output provided by TrackerParams object using
 * EvaluatorThreshold, write result of evaluation to OutputHandler and
 * FastEventServer. EvaluatorThreshold and TrackerParams set from
 * EvaluatorFrame.
 *
 * @author viktor
 * @see TrackerParams
 * @see EvaluatorThreshold
 * @see OutputHandler
 * @see FastEventClient
 * @see EvaluatorFrame
 */
public class ResultEvaluator {

    /**
     * Signelton instance
     */
    private static volatile ResultEvaluator instance = null;

    private ResultEvaluator() {
    }

    /**
     * Logging TrackerParams and evaluation result
     */
    private OutputHandler out;

    /**
     * Connection to FastEventServer, sends evaluation results via TCP, invokes
     * downstream devices via serial connection
     */
    private FastEventClient client;

    /**
     * Holds tracker output
     */
    private TrackerParams param;

    /**
     * Threshold for evaluating tracker output
     */
    private EvaluatorThreshold thresh;

    /**
     * Graphical interface for setting threshold, en/disabling evaluation
     */
    private EvaluatorFrame frame;

    /**
     * Is evaluation activated?
     */
    private boolean armed;

    /**
     * Are tracker information being drawn to canvas?
     */
    private boolean drawing;

    /**
     * Result of current threshold evaluation
     */
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

    /**
     * Attach OutputHandler and EvaluationFrame filter state listeners to
     * PropertyChangeSupport object. Once the filter is disabled, log files is
     * closed and GUI buttons disabled.
     *
     * @param pcs jAER filter PropertyChangeSupport object
     */
    public void attachFilterStateListener(PropertyChangeSupport pcs) {
        if (out != null) {
            out.attachFilterStateListener(pcs);
        }
        if (frame != null) {
            frame.attachFilterStateListener(pcs);
        }
    }

    /**
     * Evaluate result and send signal to FastEventServer
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

    /**
     * Setter for drawing information on tracker output to jAER canvas
     *
     * @param b Information drawn / not drawn
     */
    public synchronized void draw(boolean b) {
        drawing = b;
    }

    /**
     * Setter for result evaluation
     *
     * @param b Evaluation enabled / disabled
     */
    public synchronized void arm(boolean b) {
        armed = b;
    }

    /**
     * Getter for FastEventClient instance
     *
     * @return FastEventClient instance
     * @see FastEventClient
     */
    public FastEventClient getFastEventClient() {
        return client;
    }

    /**
     * Getter for EvaluatorFrame (GUI) instance
     *
     * @return EvaluatorFrame instance
     * @see EvaluatorFrame
     */
    public EvaluatorFrame getEvaluatorFrame() {
        return frame;
    }

    /**
     * Getter for OutputHandler instance
     *
     * @return OutputHandler object
     * @see OutputHandler
     */
    public OutputHandler getOutputHandler() {
        return out;
    }

    /**
     * Getter for TrackerParams instance
     *
     * @return TrackerParams instance
     * @see TrackerParams
     */
    public TrackerParams getParams() {
        return param;
    }

    /**
     * Getter for EvaluatorThreshold instance
     *
     * @return EvaluatorThreshold instance
     * @see EvaluatorThreshold
     */
    public EvaluatorThreshold getThreshold() {
        return thresh;
    }

    /**
     * @return Is evaluation active?
     */
    public boolean isArmed() {
        return armed;
    }

    /**
     * @return Are change listeners attached to jAER filter?
     */
    public boolean isListening() {
        if (frame == null || out == null) {
            return false;
        } else {
            return frame.isListening() && out.isListening();
        }
    }

    /**
     * @return Are information on tracker output being drawn to canvas?
     */
    public boolean isDrawing() {
        return drawing;
    }

    /**
     * @return Does current input exceed given threshold?
     */
    public boolean isEvent() {
        return event;
    }

    /**
     * Setter for evaluator threshold
     *
     * @param thresh EvaluatorThreshold instance
     * @see EvaluatorThreshold
     */
    public synchronized void setThreshold(EvaluatorThreshold thresh) {
        this.thresh = thresh;
    }

    /**
     * Setter for EvaluatorFrame GUI instance
     *
     * @param frame EvaluatorFrame instance
     * @see EvaluatorFrame
     */
    public synchronized void setEvaluatorFrame(EvaluatorFrame frame) {
        this.frame = frame;
    }

}
