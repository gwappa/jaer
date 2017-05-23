/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cco.jaer.eval;

import com.jogamp.opengl.GLAutoDrawable;
import net.sf.jaer.chip.AEChip;
import net.sf.jaer.event.EventPacket;
import net.sf.jaer.eventprocessing.EventFilter2D;
import net.sf.jaer.graphics.FrameAnnotater;


/**
 *
 * @author viktor
 */
public class EvaluateResults extends EventFilter2D implements FrameAnnotater {
    
    
    /**
     * Creates a new instance of ResultEvaluator
     */
    public EvaluateResults(AEChip chip) {
        super(chip);        
    }
    
    @Override
    public EventPacket filterPacket(EventPacket in) {
        return in;
    }
    
    @Override
    public void initFilter() {
    }
    
    @Override
    public void resetFilter() {
    }
    
    @Override
    public void annotate(GLAutoDrawable drawable) {
        
    }
}
