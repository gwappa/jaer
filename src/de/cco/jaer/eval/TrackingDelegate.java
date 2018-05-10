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
 * an interface that enables the filter object to be used as a "tracker"
 * from FastEventManager.
 *
 * <h3>Property call</h3>
 * <p>
 * One important method is `getCoordinateType()`. Any implementing object
 * must return "how" the tracking tracks the object (i.e. by tracking positions,
 * or by tracking angles/polar coordinates).
 * Depending on the return value, FastEvent system will activate the most suitable
 * (the current one, in most cases) evaluation target.
 * </p>
 *
 * <h3>Coordination with FastEventManager</h3>
 * <p>
 * The first trigger should be from the object that implements TrackingDelegate.
 * Upon activation (most likely during init()), it must call `FastEventManager.setTrackingDelegate(this)`.
 * </p>
 * <p>
 * Then FastEventManager, in turn, activates the suitable evaluation target based on 
 * the return value of the `getCoordinateType()` method  (as described above),
 * and adjusts the target's property based on the return value of `getSensorDimension()`.
 * </p>
 *
 * <h3>Evaluation and rendering</h3>
 * <p>
 * Currently, any implementing object must call `EvaluationTarget.evaluateParameter(TrackingParameter)`
 * in order for evaluation to occur.
 * In doing so,
 *
 * <ul>
 * <li>The object may hold the TrackingParameter object to hold the current position, and update it during each
 * `filterPacket` call.</li>
 * <li>The object may call `FastEventManager.trackingParameterUpdated(TrackingParameter)` when the current position has
 * been updated (and the new position is ready to be evaluated).</li>
 * <li>`FastEventManager.trackingParameterUpdated(TrackingParameter)` will also handle updates for rendering via
 * CanvasManager.</li>
 * </ul>
 * </p>
 *
 * <h3>Logging of tracked positions</h3>
 * <p>
 * For the time being, the logging of tracked positions is completely up to each implementing objects.
 * FastEventManager dispatches `startLogging(String)` and `endLogging()` calls, and each implementing object
 * is supposed to respond to it by e.g. creating or closing files.
 * </p>
 * <p>
 * Note that one can use FastEventWriter object (especially by calling `FastEventWriter.fromBaseName()` method),
 * which FastEventManager and MeanTracker use by default.
 * </p>
 *
 * @author gwappa
 * 
 * @see net.sf.jaer.eventprocessing.tracking.MeanTracker
 * @see CoordinateType
 * @see FastEventWriter
 * @see FastEventManager
 * @see CanvasManager
 */
public interface TrackingDelegate {
    /**
     * the "message type" string used for FastEventManager.logMessage()
     * when the tracker object changed during logging.
     */
    public static final String MSG_TYPE_TRACKER = "tracker";

    /**
     * should return what type of coordinates this delegate handles.
     * @see CoordinateType
     */
    public CoordinateType getCoordinateType();

    /**
     * should return the dimension of the sensor array this delegate handles.
     * this method may be used to adjust the evaluation target with the current sensor dimension.
     *
     * @see EvaluationTarget.resetDimension
     */
    public java.awt.Dimension getSensorDimension();

     /**
     * callback for start of logging
     *
     * @param baseName      the name that are supposed to be prepended in the 
     *                      log file (usually the experiment name).
     * @param referenceAER  the AER data log file to be used as a reference for
     *                      time stamp.
     */
    public void startLogging(String baseName, String referenceAER);

    /**
     * callback for end of logging
     */
    public void endLogging();
}

