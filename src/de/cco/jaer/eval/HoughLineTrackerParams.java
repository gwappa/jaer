/*
 * Copyright (C) 2017 viktor
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

import java.awt.geom.Line2D;

/**
 *
 * @author viktor
 */
public class HoughLineTrackerParams extends TrackerParamsBase {
    
    // line tracker parameters
    private float lineRho, lineTheta;
    private float prevRho, prevTheta;
    private float rhoRes, thetaRes;
    
    
    public HoughLineTrackerParams() {
        setName("HoughLineTracker");
        setRhoRes(6);
        setThetaRes(10);
    }
    
    public void update(int n, int firstts, int lastts, float rho, float theta) {
        setNumEvents(n);
        setFirstTS(firstts);
        setLastTS(lastts);
        prevRho = lineRho;
        prevTheta = lineTheta;
        lineRho = rho;
        lineTheta = theta;
    }
    
    // set offset resolution from lower-left corner for hough-line tracker
    public void setRhoRes(float res) {
        rhoRes = res;
    }
    
    // set rotation angle resolution for hough line tracker
    public void setThetaRes(float res) {
        thetaRes = res;
    }
    
    public double getDist() {
        double dRho = Math.abs((lineRho - prevRho) / rhoRes);
        double dTheta = Math.abs((lineTheta - prevTheta) / thetaRes);
        return Math.sqrt(dRho * dRho + dTheta * dTheta);
    }

    @Override
    public String print() {
        return getEventRate() + "," + getFirstTS() + "," + getLastTS() + "," + lineRho + "," + lineTheta + "," + getDist();
    }
    
    @Override
    public String printHeader() {
        return "eventrate,firstts,lastts,rho,theta,distance";
    }

    @Override
    public Boolean eval(EvaluatorThreshold thresh) {
        // TODO: Implement more thresholds for theta and rho?
        switch (thresh.getTarget()) {
            case EVENTRATE:
                return (getEventRate() > (double) thresh.getValue());
            case POSITION:
                // TODO: true, when any part of the line crosses this point
                return false;
            case SPEED:
                // Line2D line = new Line2D.Float();
                return false;
            case DISTANCE:
                return (getDist() > (double) thresh.getValue());
        }
        return false;
    }
}
