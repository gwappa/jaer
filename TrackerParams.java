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

import net.sf.jaer.chip.AEChip;

/**
 * This interface provides methods and enum implemented by the 
 * TrackerParameterTemplate class. For all feasable jAER tracking methods a 
 * custom class is set up, extending the abstract TrackerParamsTemplate class.
 *
 * @author viktor
 * @see TrackerParamsTemplate
 */
public interface TrackerParams {
    /**
     * OutputSource enum is used to identify what and if data stored in the
     * custom object that extends the TrackerParamsTemplate is send to a output source.
     * 
     */    
    public enum OutputSource{

        /**
         * Log data to file.
         */
        FILE,

        /**
         * Log data to STDOUT
         */
        CONSOLE,

        /**
         * Log data to STDOUT
         */
        NONE
    }
   
   
    /**
     * Setter for chip size.
     * @param chip AEChip object, containing information about chipsize.
     */
    public void setSize(AEChip chip);
    
    /**
     * Setter for chip size.
     * @param x Number of pixels in X-Direction.
     * @param y Number of pixels in Y-Direction.
     */
    public void setSize(int x, int y);
    
    /**
     * Setter for last timestamp.
     * @param ts Integer, last timestamp.
     */
    public void setLastTS(int ts);
    
    /**
     * Getter for chip size.
     * @return 1x2 size array of Intergers, X & Y chip size.
     */
    public int[] getSize();
    
    /**
     * Getter for time difference betweens events.
     * @return Difference in time, Interger.
     */
    public int getDt();
    
    /**
     * Getter for event rate.
     * @return Event rat in Hz.
     */
    public int getEventRate();
    
    public String print();
    
    public Boolean eval();
}
