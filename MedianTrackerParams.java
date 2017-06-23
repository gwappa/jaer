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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class MedianTrackerParams extends TrackerParamsTemplate{
    
    // median tracker parameters
    private float medianx, mediany;
    private float stdx, stdy; 
    private float meanx, meany;
    private float prevx, prevy;

    /**
     * Update internal result representation with values from MedianTracker object.
     * 
     * @param ts Timestamp of detection, Integer
     * @param p1x Median size of object in X direction
     * @param p1y Median size of object in Y direction
     * @param p2x Standard deviation size in X direction
     * @param p2y Standard deviation size in Y direction
     * @param p3x Mean object X position 
     * @param p3y Mean object Y position
     */
    public void update(int ts, float p1x, float p1y, float p2x, float p2y, float p3x, float p3y) {
        setLastTS(ts);
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
     * Write data to OutputSource.
     */
    public void log() {
        switch (outsrc){
            case CONSOLE:
                System.out.println("Dt: " + Integer.toString(getDt()));
                System.out.println("Distance: " + Double.toString(getDist()));
                System.out.println("Speed: " + Double.toString(getSpeed()));
                System.out.println("");
                break;
            case FILE:
                String data = "";
                try {
                    outstream.write(data);
                } catch (IOException ex) {
                    Logger.getLogger(MedianTrackerParams.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
        }
                
    }

    /**
     * Calculate euclidian distance between current and last object position.
     * 
     * @return Euclidian distance, double
     */
    @Override
    public double getDist() {
        double dx = Math.abs(meanx - prevx);
        double dy = Math.abs(meany - prevy);
        return Math.sqrt(dx * dx + dy * dy);
    }
}
