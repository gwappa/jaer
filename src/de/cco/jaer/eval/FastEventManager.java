/*
 * Copyright (C) 2017-2018 Viktor Bahr, Keisuke Sehara
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

import javax.swing.border.Border;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.BorderFactory;
import java.beans.PropertyChangeSupport;

import de.cco.jaer.eval.control.EvaluationModeSelector;
import de.cco.jaer.eval.control.EvaluationTargetSelector;
import de.cco.jaer.eval.control.RunningNoteEditor;

/*
 * TODO
 * - what shall we do about PropertyChangeSupport??
 */

/**
 * a class that is supposed to work as the interface for FastEvent-related features.
 *
 * every time one component gets updated, FastEventManager tries to synchronize all
 * the other parts.
 *
 * <p>
 * There are roughly three components in total that FastEventManager manages:
 *
 * <table>
 * <tr><th>Component</th><th>Variable class</th><th>static method(s)</th><th>Description</th></tr>
 *
 * <tr><td>Tracking delegate</td><td>de.cco.jaer.eval.TrackingDelegate</td>
 *     <td>setTrackingDelegate(TrackingDelegate)</td><td>the "tracker"-type filter object, which is used in the "filtering cycle".</td></tr>
 *
 * <tr><td>Evaluation mode</td><td>de.cco.jaer.eval.EvaluationMode</td>
 *     <td>setEvaluationMode(EvaluationMode)</td><td>whether or not to evaluate and to trigger output during acquisition</td></tr>
 *
 * <tr><td>Evaluation target</td><td>de.cco.jaer.eval.EvaluationTarget</td>
 *     <td>setEvaluationTarget(EvaluationTarget)</td><td>the target type to evaluate tracked position against, which is used in the "evaluation cycle" and "rendering cycle".</td></tr>
 *
 * <tr><td>Logging</td><td colspan="2">startLogging(), endLogging(), logMessage(String, String)</td>
 *     <td>writes logs out to a file during "logging" of jAER.</td></tr>
 * </table>
 * </p>
 *
 * <h3>Tracking delegate</h3>
 * <p>
 * `TrackingDelegate` is the interface that each "tracker"-type event filter must implement in order to work with the FastEvent system.
 * In addition to implementing the `TrackingDelegate` interface, the filter object must register itself to the Manager via
 * `FastEventManager.setTrackingDelegate()`.
 * </p>
 * <p>
 * Note that only one object can be registered as the tracking delegate at a time.
 * Otherwise unexpected race conditions may occur.
 * </p>
 *
 * <h3>Evaluation Mode</h3>
 * <p>
 * You can hard-code the mode specification via `FastEventManager.setEvaluationMode()`
 * (it should automatically update the UI of EvaluationModeSelector).
 * For the details of what mode you can choose and how you may be able to use it, please refer to EvaluationMode document.
 * </p>
 * <p>
 * Apart from the above basic functionality, FastEventManager provides the UI of EvaluationModeSelector when jAER launches.
 * In doing so, `net.sf.jaer.graphics.AEViewer` calls `FastEventManager.getEvaluationControl()` to retrieve the UI in the
 * form of JFrame.
 * </p>
 *
 * <h3>Evaluation target</h3>
 * <p>
 * Evaluation target is the site where most of FastEvent-related things occur (i.e. evaluation, annotation).
 * it gets notified of changes in tracking delegate and evaluation mode, and reflects them in what it outputs
 * and what it renders on the screen. FastEventManager mediates these coordinations.
 * </p>
 * <p>
 * For what exactly each EvaluationTarget is supposed to do, please refer to EvaluationTarget document.
 * </p>
 * <p>
 * Currently, the target types that one can use are limited to the classes in the `de.cco.jaer.eval.target.*` package
 * (i.e. EventRateTarget and RectangularTarget).
 * They are registered internally inside `de.cco.jaer.eval.control.EvaluationTargetSelector`.
 * You can hard-code the target specification via `FastEventManager.setEvaluationTarget()` as long as the new value is
 * one of those that `EvaluationTargetSelector` recognizes (it should automatically update the view in EvaluationTargetSelector).
 * </p>
 * <p>
 * Note that onlye one target object can be registered as the evaluation target at a time.
 * Otherwise unexpected problems in evaluation and/or rendering can occur.
 * </p>
 * <p>
 * Apart from this basic functionality, FastEventManager provides the UI of EvaluationTargetSelector when jAER launches.
 * In doing so, `net.sf.jaer.graphics.AEViewer` calls `FastEventManager.getEvaluationControl()` to retrieve the UI in the
 * form of JFrame.
 * </p>
 *
 * <h3>Logging features</h3>
 * <p>
 * FastEventManager's `startLogging()` and `endLogging()` methods are hooked up to the logging start/stop methods of the
 * `net.sf.jaer.graphics.AEViewer` class.`
 * It is supposed to open a log file, and accept any `logMessage()` calls from anywhere in the program.
 * Many FastEvent-related classes (such as EvaluationTarget and EvaluationMode) use this feature to log status changes.
 * </p>
 * <p>
 * in addition to programmatically calling `logMessage()`, FastEventManager provides the UI of RunningNoteEditor when jAER launches.
 * You can use this UI to add any type of one-line experimental notes during logging.
 * </p>
 *
 * @author viktor, gwappa
 * @see TrackingDelegate
 * @see EvaluationTarget
 * @see EvaluationMode
 * @see net.sf.jaer.graphics.AEViewer
 */
public class FastEventManager {
    private static final String OUTPUT_NAME         = "FastEventEvaluation";
    private static final String OUTPUT_HEADERS      = "time,type,message";
    private static final String MSG_TYPE_LOGGING    = "logging";
    private static final String MSG_TYPE_MODE       = "evalmode";
    private static final String MSG_LOGGING_STARTED = "started";
    private static final String MSG_LOGGING_ENDED   = "ended";

    // NOTE: EvaluationTarget/EvaluationMode just goes through FastEventManager.
    // FastEventManager itself does not hold a copy of them.


    /**
     * the delegate object that actually does the tracking job.
     */
    private static TrackingDelegate tracker = null;
    private static EvaluationTarget target  = null;

    /**
     * current evaluation mode.
     */
    private static EvaluationMode   mode    = null;

    /**
     * the logger output handler.
     * non-null as long as logging is on.
     */
    private static FastEventWriter  out     = null;

    /**
     * Graphical interface for setting threshold, en/disabling evaluation.
     */
    private static JFrame                   control = null;
    private static EvaluationTargetSelector targets = null;
    private static EvaluationModeSelector   modes   = null;
    private static RunningNoteEditor        notes   = null;

    private static void addTitledBorder(javax.swing.JComponent comp, String title) {
        Border empty = BorderFactory.createEmptyBorder(5, 10, 10, 10);
        if (title == null) {
            comp.setBorder(empty);
        } else {
            Border titled = BorderFactory.createTitledBorder(title);
            comp.setBorder(BorderFactory.createCompoundBorder(titled, empty));
        }
    }

    /**
     * sets up the GUI object.
     */
    private static void setupUI() {
        control = new JFrame("Evaluation Manager");
        targets = new EvaluationTargetSelector();
        addTitledBorder(targets, null);
        modes   = new EvaluationModeSelector();
        addTitledBorder(modes, "Evaluation mode");
        notes   = new RunningNoteEditor();
        addTitledBorder(notes, "Running notes");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(modes);
        panel.add(targets);
        panel.add(notes);
        panel.add(Box.createVerticalStrut(10));
        panel.setBorder(BorderFactory.createEmptyBorder(
                    5, 10, 20, 10));
        panel.add(Box.createVerticalGlue());
        control.setContentPane(panel);

        control.validate();
    }

    /**
     * registers `object` as the delegate of evaluation.
     * `object` is most likely to be a tracker-type filter, which registers
     * itself as the delegate when it is newly activated.
     *
     * @param object the tracker object
     */
    public static void setTrackingDelegate(TrackingDelegate object) {
        tracker = object;

        // notify the target selector of the change
        // to let it update the target
        targets.setTrackingDelegate(object);
    }

    /**
     * the callback function to update the targets with parameter update.
     * internally, it calls the corresponding EvaluationTarget's `evaluateWithParameter()` method
     * or `CanvasManager.setTrackingParameter()`.
     */
    public static final void trackingParameterUpdated(TrackingParameter parameter) {
        if (target != null) {
            target.evaluateParameter(parameter);
        } else {
            CanvasManager.setTrackingParameter(parameter);
        }
    }

    /**
     * updates the evaluation mode.
     * typically called from EvaluatorFrame.
     *
     * @param mode new evaluation mode
     */
    public static final void setEvaluationMode(EvaluationMode value) {
        mode = value;
        if (target != null) {
            target.setEvaluationMode(mode);
        }
        logEvaluationMode();
        if (modes != null) {
            modes.setCurrentMode(mode); // infinite loop is prevented inside
        }
        System.out.println("Evaluation mode set to: " + value.toString()); // %%debug
    }

    public static final EvaluationMode getEvaluationMode() {
        return mode;
    }

    /**
     * starts logging.
     * notifies evaluation target, tracking delegate and EventTriggerManager
     * with the starting event.
     *
     * @param referenceAER the name of the AER data log file (works as a timestamp reference).
     */
    public static final void startLogging(String referenceAER) {
        if (out == null) {
            out = FastEventWriter.fromBaseName(OUTPUT_NAME, referenceAER, OUTPUT_HEADERS);
            EventTriggerManager.setSync(true);

            if (tracker != null) {
                tracker.startLogging(referenceAER);
            }
            if (target != null) {
                target.startLogging();
            }
            notes.startLogging();

            logEvaluationMode();
            logMessage(MSG_TYPE_LOGGING, MSG_LOGGING_STARTED);
        }
    }

    /**
     * ends logging.
     * notifies evaluation target, tracking delegate and EventTriggerManager
     * with the end-logging event.
     */
    public static final void endLogging() {
        if (out != null) {
            EventTriggerManager.setSync(false);

            if (tracker != null) {
                tracker.endLogging();
            }
            if (target != null) {
                target.endLogging();
            }

            logMessage(MSG_TYPE_LOGGING, MSG_LOGGING_ENDED);
            out.close();
            out = null;
            notes.endLogging();
        }
    }

    /**
     * logs the message to the current logger output, with a timestamp.
     * if the logging is not active, this function does nothing.
     *
     * @param type  the type of the log.
     * @param msg   the message to be logged.
     */
    public static final void logMessage(String type, String msg) {
        if (out != null) {
            out.write(String.format("%d,%s,%s", System.nanoTime(), type, msg));
        }
    }

    /**
     * the helper private routine for logging evaluation mode.
     */
    private static final void logEvaluationMode() {
        logMessage(MSG_TYPE_MODE, mode.toString());
    }

    /**
     * shuts down the FastEvent system.
     * this includes closing the connection to the FastEvent server.
     */
    public static final void shutdown() {
        EventTriggerManager.shutdown();
    }



    /**
     * Getter for the GUI
     *
     * @return JFrame instance
     */
    public static final JFrame getEvaluationControl() {
        if (control == null) {
            setupUI();
        }
        return control;
    }

    /**
     * Setter for evaluation target.
     *
     * @param target EvaluationTarget instance
     * @see EvaluationTarget
     */
    public static final void setEvaluationTarget(EvaluationTarget object) {
        target = object;

        if (target != null) {
            target.setEvaluationMode(modes.getCurrentMode());
        }
        CanvasManager.setAnnotator(target);
        targets.setEvaluationTarget(target); // infinite loop will be prevented inside
    }

    /**
     * Getter for evaluation target.
     */
    public static final EvaluationTarget getEvaluationTarget() {
        return target;
    }

}
