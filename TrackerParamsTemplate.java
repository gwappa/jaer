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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import net.sf.jaer.chip.AEChip;

/**
 * Abstract base class for custom TrackerParams classes.
 * Implements all methods defined in TrackerParams interface.
 * Upstream object implement update(), log() and getDist() methods.
 * 
 * @author viktor
 * @see TrackerParams
 */
public abstract class TrackerParamsTemplate implements TrackerParams{
    
    // output source
    OutputSource outsrc;
    
    // output file object
    BufferedWriter outstream;
    
    // chip size
    private int sx, sy;
    
    // time stamp vars
    private int lastts, prevlastts;    
    
    @Override
    public void setOutputSource(OutputSource src) {
        outsrc = src;
        if (src == OutputSource.CONSOLE){
            String path = genFileName();
            outstream = openFile(path);
        }
    }

    @Override
    public void setOutputSource(String str) {
        outstream = openFile(str);
    }

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
    public double getSpeed() {
        return getDist() / getDt();
    }

    @Override
    public int getEventRate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Generate new filename from current date and working directory.
     * @return String, new file name.
     */
    private String genFileName(){
        Path current = Paths.get(System.getProperty("user.dir"));
        Date d = new Date();
        DateFormat dformat = new SimpleDateFormat("ddMMyyyy-HHmmss");
        Path path = Paths.get(current.toString(), dformat.format(d), ".log");
        return path.toString();
    }
    
    /**
     * Open new log file.
     * 
     * @param path Path to new file.
     * @return BufferedWriter object to new log file or null.
     */
    private BufferedWriter openFile(String path){
        BufferedWriter bw = null;
        try{
            File file = new File(path);
            if (!file.exists()){
                file.createNewFile();
            }
            
            FileWriter fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if ( bw != null )
                try { bw.close(); } catch ( IOException e ) { e.printStackTrace(); }

        }
        return bw;
    }

}
