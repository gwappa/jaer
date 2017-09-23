/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cco.jaer.eval;

import java.util.EnumMap;

/**
 *
 * @author viktor
 */
public class EvaluatorThreshold<T> {
 
    public enum Parameter {
        EVENTRATE,
        POSITION,
        DISTANCE,
        SPEED
    }
    
    public EnumMap<Parameter,String> paramType = new EnumMap<>(Parameter.class);
    
    private Parameter param;
    private T value;
    
    public EvaluatorThreshold( Parameter param, T value ) {
        initTypeMap();
        setTarget(param);
        setValue(value);
    }
    
    public T getValue() {
        return this.value;
    }
    
    public Parameter getTarget() {
        return this.param;
    }
    
    public void setValue(T value) {
        String type = value.getClass().getName();
        if (paramType.get(getTarget()).equals(type)) { this.value = null; }
        this.value = value;
    }
    
    private void setTarget(Parameter param) {
        this.param = param;
    }
    
    private void initTypeMap() {
        paramType.put(Parameter.EVENTRATE, "java.lang.Double");
        paramType.put(Parameter.POSITION, "[I");
        paramType.put(Parameter.DISTANCE, "java.lang.Double");
        paramType.put(Parameter.SPEED, "java.lang.Double");
    }
    
}
