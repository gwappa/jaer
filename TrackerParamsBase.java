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
    
    // chip size
    private int sx, sy;
    
    // time stamp vars
    private int lastts, prevlastts;    

    @Override
    public void setSize(AEChip chip){
        sx = chip.getSizeX();
        sy = chip.getSizeY();
    }

    @Override
    public void setSize(int x, int y){
        sx = x;
        sy = y;
    }
    
    @Override
    public void setLastTS(int ts){
        prevlastts = lastts;
        lastts = ts;
    }

    @Override
    public int[] getSize() {
        int[] sz = new int[2];
        sz[0] = sx;
        sz[1] = sy;
        return sz;
    }

    @Override
    public int getDt(){
        return lastts - prevlastts;
    }

    @Override
    public int getEventRate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
