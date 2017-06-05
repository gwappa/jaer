/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cco.jaer.eval;


import java.awt.geom.Point2D;
import java.util.LinkedList;
import net.sf.jaer.eventprocessing.tracking.RectangularClusterTracker;

/**
 *
 * @author viktor
 */
public class EvaluateResults{
    
    public enum Mode {
    MEDIAN, LINE, RECT
    }
    
    Mode mode;
    
    // median tracker parameters
    Point2D medianPoint = new Point2D.Float(), stdPoint = new Point2D.Float(), meanPoint = new Point2D.Float();
    
    // line tracker parameters
    float lineRho, lineTheta;
    
    // rectangular cluster tracker
    LinkedList<RectangularClusterTracker.Cluster> clusters;
    
    /**
     * Creates a new instance of ResultEvaluator
     */
    public EvaluateResults( Mode m ) {
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
    
    public void eval( Point2D med, Point2D std, Point2D mean ) {
        if (mode == Mode.MEDIAN){
            medianPoint = med;
            stdPoint = std;
            meanPoint = mean;
            System.out.println("Centroid: " + meanPoint.toString());
        }
    }   
   
    public void eval( float rho, float theta) {
        if (mode == Mode.LINE){
            lineRho = rho;
            lineTheta = theta;
            System.out.println("Rho: " + rho + "px");
            System.out.println("Theta: " + theta + "deg");
        }
    }
    
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
