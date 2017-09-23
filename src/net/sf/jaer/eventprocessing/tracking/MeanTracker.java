/*
     *MeanTracker.java
     *
     * Created on August 14, 2017, 11:23 PM
     *
     * To change this template, choose Tools | Options and locate the template under
     * the Source Creation and Management node. Right-click the template and choose
     * Open. You can then make changes to the template in the Source Editor.
     */
    package net.sf.jaer.eventprocessing.tracking;

    import com.jogamp.opengl.GL2;
    import com.jogamp.opengl.GLAutoDrawable;
    import de.cco.jaer.eval.EvaluatorThreshold;
    import de.cco.jaer.eval.MeanTrackerParams;
    import de.cco.jaer.eval.OutputHandler;
    import java.awt.geom.Point2D;

    import de.cco.jaer.eval.ResultEvaluator;
import java.util.ArrayList;
import java.util.Vector;

    import net.sf.jaer.Description;
    import net.sf.jaer.DevelopmentStatus;
    import net.sf.jaer.chip.AEChip;
    import net.sf.jaer.event.BasicEvent;
    import net.sf.jaer.event.EventPacket;
    import net.sf.jaer.eventprocessing.EventFilter2D;
    import net.sf.jaer.graphics.FrameAnnotater;

    /**
     * Decay based tracking of mean event location.
     *
     * @author viktor
     */
    @Description("Tracks a single object by mean event location, decay-based.")
    @DevelopmentStatus(DevelopmentStatus.Status.Experimental)
    public class MeanTracker extends EventFilter2D implements FrameAnnotater {

        
        double[] wsum = new double[2];
        double w = 0f;
        double kappa = 0f;

        Point2D stdPoint = new Point2D.Double(), meanPoint = new Point2D.Double();
        double xstd = 0f;
        double ystd = 0f;
        double xmean = 0f, ymean = 0f;
        int lastts = 0, dt = 0;
        int prevlastts = Integer.MIN_VALUE;
        int tau = getInt("tau", 10);
        private float numStdDevsForBoundingBox = getFloat("numStdDevsForBoundingBox", 1f);

        MeanTrackerParams params;
        ResultEvaluator reval;
        EvaluatorThreshold thresh;

        /**
         * Creates a new instance of MedianTracker
         */
        public MeanTracker(AEChip chip) {
            super(chip);
            params = new MeanTrackerParams();
            params.setChip(chip);
            thresh = new EvaluatorThreshold(EvaluatorThreshold.Parameter.SPEED, 4e-4);
            reval = ResultEvaluator.getInstance();
            reval.initialize(params, thresh, OutputHandler.OutputSource.FILE);
            reval.attachFilterStateListener(support);
            setPropertyTooltip("tau", "Time constant in us (microseonds) of mean location lowpass filter, 0 for instantaneous");
            setPropertyTooltip("numStdDevsForBoundingBox", "Multiplier for number of std deviations of x and y distances from median for drawing and returning bounding box");
        }

        @Override
        public void resetFilter() {
            meanPoint.setLocation(chip.getSizeX() / 2, chip.getSizeY() / 2);
            stdPoint.setLocation(1, 1);
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
        public EventPacket filterPacket(EventPacket in) {
            int n = in.getSize();

            lastts = in.getLastTimestamp();
            dt = lastts - prevlastts;
            kappa = Math.pow(Math.E, -dt / tau);
            prevlastts = lastts;
            
            if (Double.isInfinite(kappa)) {
                kappa = 1;
            }
            
            ArrayList<Short> xs = new ArrayList<>();
            ArrayList<Short> ys = new ArrayList<>();
            float[] wsum_packet = new float[2];
            int index = 0;
            wsum_packet[0] = wsum_packet[1] = 0f;
            for (Object o : in) {
                BasicEvent e = (BasicEvent) o;
                if (e.isSpecial()) {
                    continue;
                }
                wsum_packet[0] += e.x;
                wsum_packet[1] += e.y;
                xs.add(e.x);
                ys.add(e.y);
                index++;
            }
            if(index==0)  { // got no actual events
                return in;
            }
            
            wsum[0] = kappa * wsum[0] + wsum_packet[0];
            wsum[1] = kappa * wsum[1] + wsum_packet[1];
            w = kappa * w + index;
            
            xmean = wsum[0] / w;
            ymean = wsum[1] / w;

            double xvar = 0, yvar = 0;
            double tmp;
            for (int i = 0; i < index; i++) {
                tmp = xs.get(i) - xmean;
                tmp *= tmp;
                xvar += tmp;

                tmp = ys.get(i) - ymean;
                tmp *= tmp;
                yvar += tmp;
            }
            xvar /= index;
            yvar /= index;
            xstd = Math.sqrt(xvar);
            ystd = Math.sqrt(yvar);

            meanPoint.setLocation(xmean, ymean);
            stdPoint.setLocation(xstd*numStdDevsForBoundingBox, ystd*numStdDevsForBoundingBox);

            if (n>0) {
                // evaluate tracker output
                params.update(index, 
                        in.getFirstTimestamp(), 
                        lastts,
                        xstd, ystd, 
                        xmean, ymean);
                reval.eval();
            }

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
            Point2D p = meanPoint;
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
            // draw cross at mean 
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
    }
