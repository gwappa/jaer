/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cco.jaer.eval;


import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jaer.eventprocessing.tracking.RectangularClusterTracker;

/**
 *
 * @author viktor
 * @param <T>
 */
public class ResultEvaluator<T extends TrackerParams>{
    
    OutputHandler out;
    Arduino dev;
    T type;
    
    final String ON = "y";
    final String OFF = "n";
    
    /**
     * Creates a new instance of ResultEvaluator
     * @param t
     */
    public ResultEvaluator(T t) {
        type = t;
        try {
            dev = connect();
        } catch (Exception ex) {
            Logger.getLogger(ResultEvaluator.class.getName()).log(Level.SEVERE, null, ex);
        }
        out = new OutputHandler();
    }
    
    public ResultEvaluator(T t, OutputHandler.OutputSource src){
        type = t;
        try {
            dev = connect();
        } catch (Exception ex) {
            Logger.getLogger(ResultEvaluator.class.getName()).log(Level.SEVERE, null, ex);
        }
        out = new OutputHandler(src);
    }
    
    public ResultEvaluator(T t, String path){
        type = t;
        try {
            dev = connect();
        } catch (Exception ex) {
            Logger.getLogger(ResultEvaluator.class.getName()).log(Level.SEVERE, null, ex);
        }
        out = new OutputHandler(path);
    }
    
    public void eval() throws Exception{
        if (type.eval()){
            dev.send(ON);
        }
        else{
            dev.send(OFF);
        }
    }
    
    protected void finalize(){
        dev.close();
    }
    
    private Arduino connect() throws Exception{
        dev = new Arduino();
        dev.initialize();
        Thread.sleep(2000);
        System.out.println("Trying to connect to Arduino and sending 'Hello' packet.");
        dev.send("Java says 'Hello'");
        return dev;
    }
}
