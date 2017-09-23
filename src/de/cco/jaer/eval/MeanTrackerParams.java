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
 * Class that acts as container for jAER MeanTracker parameters and results.
 * Extends the abstract TrackerParamsTemplate base class which itself implements
 * TrackerParams interface.
 * Internally, MeanTracker stores X-Y positions for the 
 * <ul>
 * <li> object mean position
 * <li> object position standard deviation
 * </ul>
 * which is provided to the MeanTrackerParams object via the <i>update()</i> method.
 * @author viktor
 * @see TrackerParamsTemplate
 * @see TrackerParams
 */
public class MeanTrackerParams extends TrackerParamsBase {
    
    // median tracker parameters
    private double medianx, mediany;
    private double stdx, stdy; 
    private double meanx, meany;
    private double prevx, prevy;
    
    public MeanTrackerParams(){ 
        setName("MeanTracker"); 
    }
    
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
     */
    public void update(int n, int firstts, int lastts, double p1x, double p1y, double p2x, double p2y) {
        setNumEvents(n);
        setFirstTS(firstts);
        setLastTS(lastts);
        prevx = meanx;
        prevy = meany;
        stdx = p1x;
        stdy = p1y;
        meanx = p2x; 
        meany = p2y;
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
        return getEventRate() + "," + getFirstTS() + "," + getLastTS() + "," + meanx + "," + meany + "," + getDist() + "," + getSpeed();
    }
    
    @Override
    public String printHeader() {
        return "eventrate,firstts,lastts,meanx,meany,distance,speed";
    }

    @Override
    public Boolean eval(EvaluatorThreshold thresh) {
        switch (thresh.getTarget()) {
            case EVENTRATE:
                return (getEventRate() > (double) thresh.getValue());
            case POSITION:
                int[] t = (int[]) thresh.getValue();
                int sx1 = Integer.signum(t[0] - (int) meanx);
                int sy1 = Integer.signum(t[1] - (int) meany);
                int sx2 = Integer.signum(t[2] - (int) meanx);
                int sy2 = Integer.signum(t[3] - (int) meany);
                int psx1 = Integer.signum(t[0] - (int) prevx);
                int psy1 = Integer.signum(t[1] - (int) prevy);
                int psx2 = Integer.signum(t[2] - (int) prevx);
                int psy2 = Integer.signum(t[3] - (int) prevy);
                return sx1 != psx1 || sy1 != psy1 || sx2 != psx2 || sy2 != psy2;
            case SPEED:
                return (getSpeed() > (double) thresh.getValue());
            case DISTANCE:
                return (getDist() > (double) thresh.getValue());
        }
        return false;
    }
}
