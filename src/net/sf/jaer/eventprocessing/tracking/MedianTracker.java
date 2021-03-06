/*
 * MedianTracker.java
 *
 * Created on December 4, 2005, 11:04 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package net.sf.jaer.eventprocessing.tracking;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
// import de.cco.jaer.eval.EvaluatorThreshold; %%fastevent-related
// import de.cco.jaer.eval.MedianTrackerParams; %%fastevent-related
// import de.cco.jaer.eval.OutputHandler; %%fastevent-related
// import de.cco.jaer.eval.ResultEvaluator; %%fastevent-related
import java.awt.geom.Point2D;
import java.util.Arrays;

import net.sf.jaer.Description;
import net.sf.jaer.DevelopmentStatus;
import net.sf.jaer.chip.AEChip;
import net.sf.jaer.event.BasicEvent;
import net.sf.jaer.event.EventPacket;
import net.sf.jaer.event.PolarityEvent;
import net.sf.jaer.eventprocessing.EventFilter2D;
import net.sf.jaer.graphics.FrameAnnotater;
import net.sf.jaer.util.filter.LowpassFilter;

/**
 * Tracks median event location.
 *
 * @author tobi
 */
@Description("Tracks a single object by median event location, and computes std deviations of object event cloud")
@DevelopmentStatus(DevelopmentStatus.Status.Stable)
public class MedianTracker extends EventFilter2D implements FrameAnnotater {

    
    Point2D medianPoint = new Point2D.Float(), stdPoint = new Point2D.Float(), meanPoint = new Point2D.Float();
    float xmedian = 0f;
    float ymedian = 0f;
    float xstd = 0f;
    float ystd = 0f;
    float xmean = 0, ymean = 0;
    int lastts = 0, dt = 0;
    int prevlastts = 0;
    LowpassFilter xFilter = new LowpassFilter(), yFilter = new LowpassFilter();
    LowpassFilter xStdFilter = new LowpassFilter(), yStdFilter = new LowpassFilter();
    LowpassFilter xMeanFilter = new LowpassFilter(), yMeanFilter = new LowpassFilter();
    int tauUs =getInt("tauUs", 1000);
    private boolean offonly = getBoolean("OFFOnly", false);
    private float numStdDevsForBoundingBox =getFloat("numStdDevsForBoundingBox", 1f);
    float alpha = 1, beta = 0; // alpha is current weighting, beta is past value weighting
    
    // MedianTrackerParams params; %%fastevent-related
    // ResultEvaluator reval; %%fastevent-related
    // EvaluatorThreshold thresh; %%fastevent-related
    
    /**
     * Creates a new instance of MedianTracker
     */
    public MedianTracker(AEChip chip) {
        super(chip);
        // params = new MedianTrackerParams(); %%fastevent-related
        // params.setChip(chip); %%fastevent-related
        // reval = ResultEvaluator.getInstance(); %%fastevent-related
        // thresh = new EvaluatorThreshold(EvaluatorThreshold.Parameter.SPEED, 4e-4); %%fastevent-related
        // reval.initialize(params, thresh, OutputHandler.OutputSource.FILE); %%fastevent-related
        // reval.attachFilterStateListener(support); %%fastevent-related
        
        xFilter.setTauMs(tauUs / 1000f);
        yFilter.setTauMs(tauUs / 1000f);
        xStdFilter.setTauMs(tauUs / 1000f);
        yStdFilter.setTauMs(tauUs / 1000f);
        xMeanFilter.setTauMs(tauUs / 1000f);
        yMeanFilter.setTauMs(tauUs / 1000f);
        setPropertyTooltip("tauUs", "Time constant in us (microseonds) of median location lowpass filter, 0 for instantaneous");
        setPropertyTooltip("OFFOnly", "Consider only off events for tracking.");
        setPropertyTooltip("numStdDevsForBoundingBox", "Multiplier for number of std deviations of x and y distances from median for drawing and returning bounding box");
    }

    @Override
    public void resetFilter() {
        medianPoint.setLocation(chip.getSizeX() / 2, chip.getSizeY() / 2);
        meanPoint.setLocation(chip.getSizeX() / 2, chip.getSizeY() / 2);
        stdPoint.setLocation(1, 1);
    }

    public Point2D getMedianPoint() {
        return this.medianPoint;
    }

    /** Returns a 2D point defining the x and y std deviations times the numStdDevsForBoundingBox
     * 
     * @return the 2D value
     */
    public Point2D getStdPoint() {
        return this.stdPoint;
    }

    public Point2D getMeanPoint() {
        return this.meanPoint;
    }

    public int getTauUs() {
        return this.tauUs;
    }

    /**
     * @param tauUs the time constant of the 1st order lowpass filter on median
     * location
     */
    public void setTauUs(final int tauUs) {
        this.tauUs = tauUs;
        putInt("tauUs", tauUs);
        xFilter.setTauMs(tauUs / 1000f);
        yFilter.setTauMs(tauUs / 1000f);
        xStdFilter.setTauMs(tauUs / 1000f);
        yStdFilter.setTauMs(tauUs / 1000f);
        xMeanFilter.setTauMs(tauUs / 1000f);
        yMeanFilter.setTauMs(tauUs / 1000f);
    }

    @Override
    public void initFilter() {
    }

    @Override
    public EventPacket filterPacket(EventPacket in) {
        int n = in.getSize();
        
        lastts = in.getLastTimestamp();
        dt = lastts - prevlastts;
        prevlastts = lastts;

        int[] xs = new int[n], ys = new int[n];// big enough for all events, including IMU and APS events if there are those too
        int index = 0;
        for (Object o : in) {
            PolarityEvent p = (PolarityEvent) o;
            // if filter is enabled, consider only off events
            if (offonly && p.getPolaritySignum() == 1) {
                continue;
            }
            if (p.isSpecial()) {
                continue;
            }
            xs[index] = p.x;
            ys[index] = p.y;
            index++;
        }
        if(index==0)  { // got no actual events
            return in;
        }
        Arrays.sort(xs, 0, index); // only sort up to index because that's all we saved
        Arrays.sort(ys, 0, index);
        float x, y;
        if (index % 2 != 0) { // odd number points, take middle one, e.g. n=3, take element 1
            x = xs[index / 2];
            y = ys[index / 2];
        } else { // even num events, take avg around middle one, eg n=4, take avg of elements 1,2
            x = (float) (((float) xs[index / 2 - 1] + xs[index / 2]) / 2f);
            y = (float) (((float) ys[index / 2 - 1] + ys[index / 2]) / 2f);
        }
        xmedian = xFilter.filter(x, lastts);
        ymedian = yFilter.filter(y, lastts);
        int xsum = 0, ysum = 0;
        for (int i = 0; i < index; i++) {
            xsum += xs[i];
            ysum += ys[i];
        }
        xmean = xMeanFilter.filter(xsum / index, lastts);
        ymean = yMeanFilter.filter(ysum / index, lastts);

        float xvar = 0, yvar = 0;
        float tmp;
        for (int i = 0; i < n; i++) {
            tmp = xs[i] - xmean;
            tmp *= tmp;
            xvar += tmp;

            tmp = ys[i] - ymean;
            tmp *= tmp;
            yvar += tmp;
        }
        xvar /= index;
        yvar /= index;

        xstd = xStdFilter.filter((float) Math.sqrt(xvar), lastts);
        ystd = yStdFilter.filter((float) Math.sqrt(yvar), lastts);


        medianPoint.setLocation(xmedian, ymedian);
        meanPoint.setLocation(xmean, ymean);
        stdPoint.setLocation(xstd*numStdDevsForBoundingBox, ystd*numStdDevsForBoundingBox);

        // if (n>0) { %%fastevent-related
        //     // evaluate tracker output %%fastevent-related
        //     params.update(index,  %%fastevent-related
        //             in.getFirstTimestamp(),  %%fastevent-related
        //             lastts,  %%fastevent-related
        //             xmedian, ymedian,  %%fastevent-related
        //             xstd, ystd,  %%fastevent-related
        //             xmedian, ymedian); %%fastevent-related
        //     reval.eval(); %%fastevent-related
        // } %%fastevent-related
        
        return in; // xs and ys will now be sorted, output will be bs because time will not be sorted like addresses
    }

    /**
     * JOGL annotation
     */
    @Override
    public void annotate(GLAutoDrawable drawable) {
        if (!isFilterEnabled()) {
            return;
        }
        Point2D p = medianPoint;
        Point2D s = stdPoint;
        GL2 gl = drawable.getGL().getGL2();
        // already in chip pixel context with LL corner =0,0
        gl.glPushMatrix();
        gl.glColor3f(0, 0, 1);
        gl.glLineWidth(4);
        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex2d(p.getX() - s.getX(), p.getY() - s.getY());
        gl.glVertex2d(p.getX() + s.getX(), p.getY() - s.getY());
        gl.glVertex2d(p.getX() + s.getX(), p.getY() + s.getY());
        gl.glVertex2d(p.getX() - s.getX(), p.getY() + s.getY());
        gl.glEnd();
        // draw cross at median 
        gl.glColor3f(1, 0, 0);
        gl.glLineWidth(2);
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex2d(p.getX(), p.getY() + 2);
        gl.glVertex2d(p.getX(), p.getY() - 2);
        gl.glVertex2d(p.getX() + 2, p.getY());
        gl.glVertex2d(p.getX() - 2, p.getY());
        gl.glEnd();
        gl.glPopMatrix();
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
        putFloat("numStdDevsForBoundingBox",numStdDevsForBoundingBox);
    }
    
    public boolean getOffOnly() {
        return offonly;
    }
    
    public void setOffOnly(boolean offonly) {
        boolean old = this.offonly;
        this.offonly = offonly;
        putBoolean("OFFOnly", offonly);
        // support.firePropertyChange("OFFOnly", old, offonly);
    }
}
