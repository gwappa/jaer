/*
 *
 * Created on October 5, 2005, 11:36 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package ch.unizh.ini.caviar.chip.cochlea;

import ch.unizh.ini.caviar.chip.AEChip;
import ch.unizh.ini.caviar.chip.TypedEventExtractor;


/**
 * For Shih-Chii's AMS cochlea with binaraul 64 stage cochlea each tap with 8 ganglion cells, 4 of LPF type and 4 of BPF type.
 *The bits in the raw address and the histogram display are arranged as shown in the following sketch. This class doesn't define the bias generator
 so that it can be used when another besides the on-chip source supplies the bias voltages or inputs to the on-chip bias generator.
 *<p>
 *<img src="doc-files/cochleaAMSSketch.jpg" />
 *</p>
 * @author tobi
 */
public class CochleaAMSNoBiasgen extends CochleaChip  {
    
    /** Creates a new instance of Tmpdiff128 */
    public CochleaAMSNoBiasgen() {
        setName("CochleaAMSNoBiasgen");
        setSizeX(64);
        setSizeY(16); // 4+4 cells/channel * 2 ears
//        setNumCellTypes(2); // right,left cochlea
        setNumCellTypes(4); // right,left cochlea plus lpf,bpf type of ganglion cell
        setEventExtractor(new Extractor(this));
        setBiasgen(null);
        setEventClass(CochleaAMSEvent.class);
    }
    
    /** Extract cochlea events. The event class returned by the extractor is CochleaAMSEvent.
     * The address are mapped as follows
     * <pre>
     * TX0 - AE0 
     * TX1 - AE1
     * ...
     * TX7 - AE7
     * TY0 - AE8
     * TY1 - AE9
     * AE15:10 are unused and are unconnected - they should be masked out in software.
     * </pre>
     * 
     * <table>
     * <tr><td>15<td>14<td>13<td>12<td>11<td>10<td>9<td>8<td>7<td>6<td>5<td>4<td>3<td>2<td>1<td>0
     * <tr><td>x<td>x<td>x<td>x<td>x<td>x<td>TH1<td>TH0<td>CH5<td>CH4<td>CH3<td>CH2<td>CH1<td>CH0<td>EAR<td>LPFBPF
     * </table>
     * <ul>
     * <li>
     * TH1:0 are the ganglion cell. TH1:0=00 is the one biased with Vth1, TH1:0=01 is biased with Vth2, etc. TH1:0=11 is biased with Vth4. Vth1 and Vth4 are external voltage biaes.
     * <li>
     * CH5:0 are the channel address. 0 is the base (input) responsive to high frequencies. 63 is the apex responding to low frequencies.
     * <li>
     * EAR is the binaruaral ear. EAR=0 is left ear, EAR=1 is right ear.
     * <li>
     * LPFBPF is the ganglion cell type. LPFBPF=1 is a lowpass neuron, LPFBPF=1 is a bandpass neuron.
     * </ul>
     */
    public class Extractor extends TypedEventExtractor implements java.io.Serializable{
        public Extractor(AEChip chip){
            super(chip);
            setEventClass(CochleaAMSEvent.class);
//            setXmask((short)(63<<2)); // tap bits are bits 2-7
//            setXshift((byte)2); // shift them right to get back to 0-63 after AND masking
//            setYmask((short)0x300); // we don't need y or type because these are overridden below
//            setYshift((byte)8);
//            setTypemask((short)2);
//            setTypeshift((byte)0);
//            setFliptype(true); // no 'type' so make all events have type 1=on type
        }
        
        /** Overrides default extractor so that cochlea channels are returned, 
         * numbered from x=0 (base, high frequencies, input end) to x=63 (apex, low frequencies).
         * 
         * @param addr raw address.
         * @return channel, from 0 to 63.
         */
        @Override public short getXFromAddress(int addr){
            short tap=(short)(((addr&0xfc)>>>2)); // addr&(1111 1100) >>2, 6 bits, max 63, min 0
            return tap;
        }
        
        /** Overrides default extract to define type of event as the left or right cochlea and the LPF/BPF type.
         LPF/BPF is the lsb (bit 0) and left/right is bit 1. Therefore for example 
         0=left+lpf, 1=left+bpf, 2=right+lpf, 3=right+bpf.
         *@param addr the raw address.
         *@return the type, where 0=left+lpf, 1=left+bpf, 2=right+lpf, 3=right+bpf
         */
        @Override public byte getTypeFromAddress(int addr){
//            return (byte)((addr&0x02)>>>1);
            return (byte)((addr&0x03)); // left/right and lpf/bpf mapped to 0-3 cell type
        }
        
        /** Overrides default extractor to spread all outputs from a tap (left/right, ganglion cell, LPF/HPF) into a
         *single y address that can be displayed in the 2d histogram.
         * The y returned goes like this from 0-15: left LPF(4)/BPF(4), right LPF(4)/BPF(4).
         *@param addr the raw address
         *@return the Y address
         */
        @Override public short getYFromAddress(int addr){
//            int gangCell=(addr&0x300)>>>8; // each tap has 8 ganglion cells, 4 of each of LPF/BPF type
//            int lpfBpf=(addr&0x01)<<2; // lowpass/bandpass ganglion cell type
//            int leftRight=(addr&0x02)<<2; // left/right cochlea. see javadoc jpg scan for layout
//            short v=(short)(gangCell+lpfBpf+leftRight);
            int lpfBpf=(addr&0x01); // 1=LPF 0=BPF ganglion cell type
            int rightLeft=(addr&0x02); // right=1 left=0 cochlea
            int thr=(0x300&addr)>>6; // puts threshold variety of ganglion cell starting at bit 2
            short v=(short)(lpfBpf+rightLeft+thr);
            return v;
        }
        
    }
}
