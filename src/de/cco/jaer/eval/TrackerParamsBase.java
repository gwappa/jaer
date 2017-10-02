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
 * Abstract base class for custom TrackerParams classes.
 * Implements all methods defined in TrackerParams interface.
 * Upstream object implement update(), log() and getDist() methods.
 * 
 * @author viktor
 * @see TrackerParams
 */
public abstract class TrackerParamsBase implements TrackerParams{
    
    // Tracker name
    private String name;
    
    // AEChip instance
    private AEChip chip;
    
    // chip size
    private int sx, sy;
    
    // package size
    private int nevents, prevnevents;
    
    // time stamp vars
    private int firstts, lastts, prevfirstts, prevlastts;    

    @Override
    public void setChip(AEChip ch) {
        chip = ch;
        sx = chip.getSizeX();
        sy = chip.getSizeY();
    }
    
    @Override
    public void setFirstTS(int ts) {
        prevfirstts = firstts;
        firstts = ts;
    }
    
    @Override
    public void setLastTS(int ts) {
        prevlastts = lastts;
        lastts = ts;
    }
    
    @Override
    public void setName(String n) {
        name = n;
    }
    
    @Override
    public void setNumEvents(int n) {
        prevnevents = nevents;
        nevents = n;
    }
    
    @Override
    public AEChip getChip() {
        return chip;
    }
    
    @Override
    public int getFirstTS() {
        return firstts;
    }

    @Override
    public int getLastTS() {
        return lastts;
    }

    @Override
    public int[] getSize() {
        int[] sz = new int[2];
        sz[0] = sx;
        sz[1] = sy;
        return sz;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getNumEvents() {
        return nevents;
    }
    
    @Override
    public int getDt(){
        return lastts - prevlastts;
    }

    @Override
    public int getDuration(){
        return (lastts > 0) ? lastts - firstts : 1;
    }
    
    @Override
    public double getEventRate() {
        return (double) getNumEvents() / getDuration();
    }
}
