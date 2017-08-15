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
 * Class that acts as container for jAER MedianTracker parameters and results.
 * Extends the abstract TrackerParamsTemplate base class which itself implements
 * TrackerParams interface.
 * Internally, MedianTracker stores X-Y positions for the 
 * <ul>
 * <li> object mean position
 * <li> object median position
 * <li> object position standard deviation
 * </ul>
 * which is provided to the MedianTrackerParams object via the <i>update()</i> method.
 * @author viktor
 * @see TrackerParamsTemplate
 * @see TrackerParams
 */
public class MedianTrackerParams extends TrackerParamsBase{
    
    // median tracker parameters
    private float medianx, mediany;
    private float stdx, stdy; 
    private float meanx, meany;
    private float prevx, prevy;
    
    public MedianTrackerParams(){ name = "MedianTracker";}
    
    /**
     * Update internal result representation with values from MedianTracker object.
     * 
     * @param n Number of events per package
     * @param firstts First timestamp in event package
     * @param lastts Last timestamp in event package
     * @param p1x Median size of object in X direction
     * @param p1y Median size of object in Y direction
     * @param p2x Standard deviation size in X direction
     * @param p2y Standard deviation size in Y direction
     * @param p3x Mean object X position 
     * @param p3y Mean object Y position
     */
    public void update(int n, int firstts, int lastts, float p1x, float p1y, float p2x, float p2y, float p3x, float p3y) {
        setNumEvents(n);
        setFirstTS(firstts);
        setLastTS(lastts);
        prevx = meanx;
        prevy = meany;
        medianx = p1x;
        mediany = p1y;
        stdx = p2x;
        stdy = p2y; 
        meanx = p3x; 
        meany = p3y;
    }

    /**
     * Calculate euclidian distance between current and last object position.
     * 
     * @return Euclidian distance, double
     */
    public double getDist() {
        double dx = Math.abs(meanx - prevx);
        double dy = Math.abs(meany - prevy);
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    public double getSpeed() {
        return getDist() / getDt();
    }
    
    @Override
    public String print() {
        return getEventRate() + "," + getFirstTS() + "," + getLastTS() + "," + medianx + "," + mediany + "," + stdx + "," +  stdy + ","  + getDist() + "," + getSpeed();
    }
    
    @Override
    public String printHeader() {
        return "eventrate,firstts,lastts,medianx,mediany,stdx,sty,distance,speed";
    }

    @Override
    public Boolean eval() {
        return (getSpeed() >= 4e-4);
    }
}
