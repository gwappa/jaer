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

/**
 * the interface that defines an event target class.
 *
 * <p>
 * The two methods, `getSpecifier()` (in the `CanvasAnnotator` superinterface) and `acceptsCoordinateType()`
 * exposes the target's features out to the other classes.
 * </p>
 * <p>
 * Other two methods, `evaluateParameter()` and `render()` (in CanvasAnnotator), define the specific behavior
 * of the target during evaluation and display-rendering, respectively.
 * </p>
 * <p>
 * The other methods such as `setEvaluationMode()`, `startLogging()` or `endLogging()` are the hook methods
 * that other classes (in most cases FastEventManager) notify the target object with external events.
 * You can use these methods to update the target's internal status, or to let FastEventManager log messages
 * that are specific to this target object.
 * </p>
 *
 * @author gwappa
 * @see CanvasManager
 */
public interface EvaluationTarget 
    extends CanvasAnnotator
{
    /**
     * the type specifier that FastEventManager uses to log changes in EvaluationTarget type during logging.
     */
    static final String MSG_TYPE_TARGET = "target";
    
    /**
     * returns if this target type can be used with given coordinate type.
     *
     * @see CoordinateType
     */
    boolean acceptsCoordinateType(CoordinateType type);

    /**
     * sets the internal settings to accommodate the given sensor dimension.
     * normally called from EvaluationTargetSelector.setTrackingDelegate().
     *
     * @see EvaluationTargetSelector.setTrackingDelegate
     */
    void resetDimension(java.awt.Dimension dimension);

    /**
     * updates the evaluation mode.
     * normally called from FastEventManager.setEvaluationMode().
     * For the purpose of changing the evaluation mode, use FastEventManager.setEvaluationMode().
     *
     * @see FastEventManager.setEvaluationMode
     */
    void setEvaluationMode(EvaluationMode value);

    /**
     * a callback for getting updated with the start of logging (works as an opportunity of
     * logging the initial condition of the target).
     * typically called from FastEventManager.startLogging().
     * for the purpose of starting logging in general, use FastEventManager.startLogging().
     *
     * @see FastEventManager.startLogging
     * @see FastEventManager.logMessage
     */
    void startLogging();
    
    /**
     * a callback for getting updated with the end of logging (works as an opportunity of logging the final
     * status of the target).
     * typically called from FastEventManager.endLogging().
     * for the purpose of ending logging in general, use FastEventManager.endLogging().
     *
     * @see FastEventManager.endLogging
     * @see FastEventManager.logMessage
     */
    void endLogging();

    /**
     * returns the controller JPanel object for this target.
     *
     * @see EvaluationTargetSelector
     */
    javax.swing.JPanel getTargetControl();

    /**
     * evaluates the given parameter set if the current evaluation mode has 'evaluating' status.
     * evaluation status `eval` of the given TrackingParameter parameter is updated.
     * if the evaluation mode has 'triggering' status, the EvaluationTarget instance 
     * triggers output event (by calling EventTriggerManager.setEvent()).
     *
     * @see EvaluationMode
     * @see TrackingParameter
     * @see EventTriggerManager.setEvent
     */
    void evaluateParameter(TrackingParameter param);
}


