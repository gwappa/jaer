/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cco.jaer.eval;


import net.sf.jaer.chip.AEChip;
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
    
    private Mode mode;
    
    // chip size
    private int sx, sy;
    
    // time stamp vars
    private int lastts = 0, prevlastts = 0;
    
    // median tracker parameters
    private float medianx, mediany;
    private float stdx, stdy; 
    private float meanx, meany;
    private float prevx, prevy;
    
    // line tracker parameters
    private float lineRho, lineTheta;
    private float prevRho, prevTheta;
    private float rhoRes, thetaRes;
    
    // rectangular cluster tracker
    private LinkedList<RectangularClusterTracker.Cluster> clusters;
    
    /**
     * Creates a new instance of ResultEvaluator
     * @param m
     */
    public ResultEvaluator( Mode m ) throws Exception {
        mode = m;
        String modeStr = new String();
        switch (m) {
            case MEDIAN:
                modeStr = "MedianTracker";
                break;
            case LINE:
                modeStr = "HoughLineTracker";
                break;
            case RECT:
                modeStr = "RectangleTracker";
                break;
        }
        System.out.println("Starting evaluation");
        System.out.println("Using '" + modeStr + "' modus");
        connect(); // try to connect to Arduino
    }
    
    public void setSize(AEChip chip){
        sx = chip.getSizeX();
        sy = chip.getSizeY();
    }
    
    public void setSize(int x, int y){
        sx = x;
        sy = y;
    }
    
    public void setRhoRes(float res){
        rhoRes = res;
    }
    
    public void setThetaRes(float res){
        thetaRes = res;
    }
    
    public int getDt(){
        return lastts - prevlastts;
    }
    
    public double getDist() {
        double d = 0.0;
        switch (mode) {
            case MEDIAN:
                double dx = Math.abs(meanx - prevx);
                double dy = Math.abs(meany - prevy);
                d = Math.sqrt(dx * dx + dy * dy);
                break;
            case LINE:
                double dRho = Math.abs((lineRho - prevRho) / rhoRes);
                double dTheta = Math.abs((lineTheta - prevTheta) / thetaRes);
                d = Math.sqrt(dRho * dRho + dTheta * dTheta);
                break;
            case RECT:
                break;
        }
        return d;
    }
    
    public double getSpeed() {
        return getDist() / getDt();
    }
    
    // median tracker evaluation method
    public void eval( int ts, float p1x, float p1y, float p2x, float p2y, float p3x, float p3y) {
        if (mode == Mode.MEDIAN){
            
            // set variables
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
            System.out.println("");
        }
    }   
   
    // Hough line tracker evaluation method
    public void eval( int ts, float rho, float theta ) {
        if (mode == Mode.LINE){
            prevlastts = lastts;
            lastts = ts;
            prevRho = lineRho;
            prevTheta = lineTheta;
            lineRho = rho;
            lineTheta = theta;
            System.out.println("Rho: " + rho + "px");
            System.out.println("Theta: " + theta + "°");
            System.out.println("Speed: " + getSpeed());
            System.out.println("");
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
                if (!c.isVisible()) {
                    continue;
                }
                double locx = c.getLocation().getX();
                double locy = c.getLocation().getY();
                double velox = c.getVelocityPPS().getX();
                double veloy = c.getVelocityPPS().getY();
                double velo = Math.sqrt(Math.abs(velox * velox) + Math.abs(veloy * veloy));
                int num = c.getClusterNumber();
                System.out.println("Cluster " + num + ": [" + locx + "," + locy + "] " + "@ " + velo);
            }
        }
    }
    
    private Arduino connect() throws Exception{
        Arduino dev = new Arduino();
        dev.initialize();
        Thread.sleep(2000);
        System.out.println("Trying to connect to Arduino and sending 'Hello' packet.");
        dev.send("Java says 'Hello'");
        return dev;
    }
}
