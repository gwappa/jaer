/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cco.jaer.eval;


import net.sf.jaer.chip.AEChip;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import net.sf.jaer.eventprocessing.tracking.RectangularClusterTracker;

/**
 *
 * @author viktor
 */
public class ResultEvaluator{
    
    public enum Mode {
    MEDIAN, LINE, RECT
    }
    
    Mode mode;
    
    // chip size
    int sx, sy;
    
    // time stamp vars
    int lastts = 0, prevlastts = 0;
    
    // median tracker parameters
    float medianx, mediany, stdx, stdy, meanx, meany, prevx, prevy;
    
    // line tracker parameters
    float lineRho, lineTheta;
    
    // rectangular cluster tracker
    LinkedList<RectangularClusterTracker.Cluster> clusters;
    
    /**
     * Creates a new instance of ResultEvaluator
     */
    public ResultEvaluator( Mode m ) {
        mode = m;
        String modeStr = new String();
        switch (m) {
            case MEDIAN:
                modeStr = "MedianTracker";
            case LINE:
                modeStr = "HoughLineTracker";
            case RECT:
                modeStr = "RectangleTracker";
        }
        System.out.println("Starting evaluation");
        System.out.println("Using '" + modeStr + "' modus");
    }
    
    public void setSize(AEChip chip){
        sx = chip.getSizeX();
        sy = chip.getSizeY();
    }
    
    public void setSize(int x, int y){
        sx = x;
        sy = y;
    }
    
    public int getDt(){
        return lastts - prevlastts;
    }
    
    public double getDist() {
        double dx = 0.0, dy = 0.0, d = 0.0;
        switch (mode) {
            case MEDIAN: 
               dx = Math.abs(meanx - prevx);
               dy = Math.abs(meany - prevy);
               d = Math.sqrt(dx * dx + dy * dy);
            case LINE:
            case RECT:
        }
        return d;
    }
    
    public double getSpeed() {
        return getDist() / getDt();
    }
    
    // median tracker evaluation method
    public void eval( int ts, float p1x, float p1y, float p2x, float p2y, float p3x, float p3y) {
        if (mode == Mode.MEDIAN){
            prevlastts = lastts;
            lastts = ts;
            prevx = meanx;
            prevy = meany;
            medianx = p1x;
            mediany = p1y;
            stdx = p2x;
            stdy = p2y; 
            meanx = p3x; 
            meany = p3y;
                                   
            System.out.println("Dt: " + Integer.toString(getDt()));
            System.out.println("Distance: " + Double.toString(getDist()));
            System.out.println("Speed: " + Double.toString(getSpeed()));
        }
    }   
   
    // Hough line tracker evaluation method
    public void eval( float rho, float theta) {
        if (mode == Mode.LINE){
            lineRho = rho;
            lineTheta = theta;
            System.out.println("Rho: " + rho + "px");
            System.out.println("Theta: " + theta + "deg");
        }
    }
    
    // rectangular cluster tracker evaluation method
    public void eval( LinkedList<RectangularClusterTracker.Cluster> cl ) {
        if (mode == Mode.RECT) {
            if (cl.isEmpty()) {
                return;
            }
            clusters = cl;
            for (RectangularClusterTracker.Cluster c : cl) {
                Point2D.Float loc = c.getLocation();
                Point2D.Float velo = c.getVelocityPPS();
                int num = c.getClusterNumber();
                System.out.println("Cluster " + num + ": " + loc.toString() + " " + velo.toString());
            }
        }
    }
}
