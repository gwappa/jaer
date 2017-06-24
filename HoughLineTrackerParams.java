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

/**
 *
 * @author viktor
 */
public class HoughLineTrackerParams extends TrackerParamsBase{
    
    // line tracker parameters
    private float lineRho, lineTheta;
    private float prevRho, prevTheta;
    private float rhoRes, thetaRes;
    
    
    public HoughLineTrackerParams(){
        rhoRes = 6;
        thetaRes = 10;
    }
    
    public void update(int ts, float rho, float theta){
        setLastTS(ts);
        prevRho = lineRho;
        prevTheta = lineTheta;
        lineRho = rho;
        lineTheta = theta;
    }
    
    // set offset resolution from lower-left corner for hough-line tracker
    public void setRhoRes(float res){
        rhoRes = res;
    }
    
    // set rotation angle resolution for hough line tracker
    public void setThetaRes(float res){
        thetaRes = res;
    }
    
    public double getDist(){
        double dRho = Math.abs((lineRho - prevRho) / rhoRes);
        double dTheta = Math.abs((lineTheta - prevTheta) / thetaRes);
        return Math.sqrt(dRho * dRho + dTheta * dTheta);
    }

    @Override
    public String print() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Boolean eval() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
