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

/*
     *MeanTracker.java
     *
     * Created on August 14, 2017, 11:23 PM
 */
package net.sf.jaer.eventprocessing.tracking;

import java.awt.Dimension;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GL2;
import de.cco.jaer.eval.CoordinateType;
import de.cco.jaer.eval.TrackingDelegate;
import de.cco.jaer.eval.TrackingParameter;
import de.cco.jaer.eval.FastEventWriter;
import de.cco.jaer.eval.EvaluationTarget;
import de.cco.jaer.eval.FastEventManager;
import de.cco.jaer.eval.CanvasManager;
import de.cco.jaer.eval.EventTriggerManager;

import java.util.ArrayList;
import net.sf.jaer.Description;
import net.sf.jaer.DevelopmentStatus;
import net.sf.jaer.chip.AEChip;
import net.sf.jaer.event.EventPacket;
import net.sf.jaer.event.PolarityEvent;
import net.sf.jaer.eventprocessing.EventFilter2D;
import net.sf.jaer.graphics.FrameAnnotater;

/**
 * Decay based tracking of mean event location.
 *
 * @author viktor, gwappa
 */
@Description("Tracks a single object by mean event location, decay-based.")
@DevelopmentStatus(DevelopmentStatus.Status.Experimental)
public class MeanTracker extends EventFilter2D 
    implements FrameAnnotater, TrackingDelegate
{
    private static final    String      TRACKER_NAME        = "MeanTracker";
    private static final    String[]    TRACKER_HEADERS     = new String[] {"X","Y"};

    double[] w1sum = new double[2];
    double[] w2sum = new double[2];
    double w = 0f;
    double kappa = 0f;

    int lastts = 0, dt = 0;
    int prevlastts = Integer.MIN_VALUE;
    int tau = getInt("tau", 10);
    private boolean offonly = getBoolean("OFFOnly", false);
    private boolean ononly = getBoolean("ONOnly", false);
    private float numStdDevsForBoundingBox = getFloat("numStdDevsForBoundingBox", 1f);

    TrackingParameter   param;

    FastEventWriter     output = null;

    /**
     * Creates a new instance of MeanTracker
     */
    public MeanTracker(AEChip chip) {
        super(chip);
        param = new TrackingParameter(CoordinateType.POSITION);

        FastEventManager.setTrackingDelegate(this);

        // TODO: probably something like "isLogging" property
        // should be set here in our PropertyChangeSupport object
        // so that FastEventWriter can close itself with its value change...

        setPropertyTooltip("tau", "Time constant in us (microseonds) of mean location lowpass filter, 0 for instantaneous");
        setPropertyTooltip("OFFOnly", "Consider only off events for tracking.");
        setPropertyTooltip("ONOnly", "Consider only on events for tracking.");
        setPropertyTooltip("numStdDevsForBoundingBox", "Multiplier for number of std deviations of x and y distances from median for drawing and returning bounding box");
    }

    @Override
    public void resetFilter() {
        param.coords[0] = chip.getSizeX() / 2;
        param.coords[1] = chip.getSizeY() / 2;
        param.confidence[0] = 1;
        param.confidence[1] = 1;
    }

    public int getTauUs() {
        return this.tau;
    }

    /**
     * @param tauUs the time constant of the 1st order lowpass filter on median
     * location
     */
    public void setTauUs(final int tauUs) {
        this.tau = tauUs;
        putInt("tau", tauUs);
    }

    @Override
    public void initFilter() {
    }

    @Override
    public final EventPacket filterPacket(EventPacket in) {
        int n = in.getSize();
        int nevents = 0;

        lastts = in.getLastTimestamp();
        dt = lastts - prevlastts;
        kappa = Math.pow(Math.E, -dt / tau);
        prevlastts = lastts;

        if (Double.isInfinite(kappa)) {
            kappa = 1;
        }

        // multiply weighted sums with decay time constant
        w        *= kappa;
        w1sum[0] *= kappa;
        w1sum[1] *= kappa;
        w2sum[0] *= kappa;
        w2sum[1] *= kappa;

        // update weighted sums by iterating with events
        for (Object o : in) {
            PolarityEvent p = (PolarityEvent) o;
            // if filter is enabled, consider only off events
            if (offonly && p.getPolaritySignum() == 1) {
                continue;
            }
            else if (ononly && p.getPolaritySignum() == -1) {
                continue;
            }
            if (p.isSpecial()) {
                continue;
            }
            // process this event object
            // by merging to nevents, w1sum, w2sum
            nevents++;
            w1sum[0] += p.x;
            w1sum[1] += p.y;
            w2sum[0] += (p.x)*(p.x);
            w2sum[1] += (p.y)*(p.y);
        }
        // update the weight according to `nevents`
        w += nevents;

        // compute AVG values
        final double xmean = w1sum[0] / w;
        final double ymean = w1sum[1] / w;

        // compute STD values
        final double xstd = Math.sqrt(w2sum[0]/w - (xmean*xmean));
        final double ystd = Math.sqrt(w2sum[1]/w - (ymean*ymean));


        // update the parameter set
        param.nevents       = nevents;
        param.firstts       = in.getFirstTimestamp();
        param.lastts        = lastts;
        param.coords[0]     = xmean;
        param.coords[1]     = ymean;
        param.confidence[0] = xstd * numStdDevsForBoundingBox;
        param.confidence[1] = ystd * numStdDevsForBoundingBox;

        if (nevents > 0) {
            // update evaluation & position status
            // (and thus triggers output, if applicable)
            FastEventManager.trackingParameterUpdated(param);
        }

        // check whether jAER is currently logging
        // test by examining output against null
        if (output != null) {
            // let the output handler handle the logging
            output.write(param.toString());
        }

        return in; // xs and ys will now be sorted, output will be bs because time will not be sorted like addresses
    }

    /**
     * JOGL annotation
     */
    @Override
    public void annotate(GLAutoDrawable drawable) {
        // not used here.
        // for rendering, check CanvasManager.render() method.
    }

    /**
     * returns the mode of this Tracker.
     */
    @Override
    public final CoordinateType getCoordinateType() {
        return CoordinateType.POSITION;
    }

    @Override
    public final Dimension getSensorDimension() {
        return new Dimension(chip.getSizeX(), chip.getSizeY());
    }

    /**
     * callback for start of logging
     *
     * @param referenceAER  the AER data log file to be used as a reference for
     *                      time stamp.
     */
    @Override
    public final void startLogging(String referenceAER) {
        if (output == null) {
            // prepare output
            output = FastEventWriter.fromBaseName(TRACKER_NAME, referenceAER,
                                            param.generateHeaders(TRACKER_HEADERS));
            FastEventManager.logMessage(MSG_TYPE_TRACKER, String.format("Mean(%d)",tau));
            // TODO: set PropertyChangeSupport
        }
    }

    /**
     * callback for end of logging
     */
    @Override
    public final void endLogging() {
        if (output != null) {
            // close output
            // (if there were no lines, it should be nicely handled by FastEventWriter)
            output.close();
            output = null;
        }
    }

    /**
     * @return the numStdDevsForBoundingBox
     */
    public float getNumStdDevsForBoundingBox() {
        return numStdDevsForBoundingBox;
    }

    /**
     * @param numStdDevsForBoundingBox the numStdDevsForBoundingBox to set
     */
    public void setNumStdDevsForBoundingBox(float numStdDevsForBoundingBox) {
        this.numStdDevsForBoundingBox = numStdDevsForBoundingBox;
        putFloat("numStdDevsForBoundingBox", numStdDevsForBoundingBox);
    }
    
    public boolean getOffOnly() {
        return offonly;
    }
    
    public boolean getOnOnly() {
        return ononly;
    }
    
    public void setOffOnly(boolean offonly) {
        boolean old = this.offonly;
        this.offonly = offonly;
        putBoolean("OFFOnly", offonly);
        putBoolean("ONOnly", !offonly);
        // support.firePropertyChange("OFFOnly", old, offonly);
    }
    
    public void setOnOnly(boolean ononly) {
        boolean old = this.ononly;
        this.ononly = ononly;
        putBoolean("ONOnly", ononly);
        putBoolean("OFFOnly", !ononly);
        // support.firePropertyChange("OFFOnly", old, offonly);
    }
}
