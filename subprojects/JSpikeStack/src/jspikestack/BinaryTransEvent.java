/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jspikestack;

/**
 *
 * @author oconnorp
 */
public class BinaryTransEvent extends Spike {
    
    boolean trans=true;
    
    public BinaryTransEvent(int time,int addr,int layer,boolean transition)
    {
        super(time,addr,layer);
        
        trans=transition;
    }
    
    @Override
    public String toString()
    {
        return super.toString()+(trans?", ON":", OFF");
    }
    
    
}