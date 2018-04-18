/*
 * Copyright (C) 2017 Viktor Bahr
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

import java.util.EnumMap;

/**
 * Class that handles tresholds applied by ResultEvaluator to tracker output
 * Thresholds are defined in EvaluatorFrame
 *
 * @author viktor
 * @param <T> Threshold value type
 */
public class EvaluatorThreshold<T> {

    /**
     * Possible threshold types, matches Evaluator Frames tabbed pane labels
     */
    public enum Parameter {
        EVENTRATE,
        POSITION,
        REGION,
        DISTANCE,
        SPEED
    }

    /**
     * Map of threshold type and expected value class, used for checking input
     * type
     */
    public EnumMap<Parameter, String> paramType = new EnumMap<>(Parameter.class);

    /**
     * Theshold type
     */
    private Parameter param;

    /**
     * Threshold value, can be of variable type
     */
    private T value;

    /**
     * Class constructor
     *
     * @param param Threshold type
     * @param value Threshold value
     */
    public EvaluatorThreshold(Parameter param, T value) {
        initTypeMap();
        setTarget(param);
        setValue(value);
    }

    /**
     * Getter for threshold value
     *
     * @return Threshold value
     */
    public T getValue() {
        return this.value;
    }

    /**
     * Getter for threshold type
     *
     * @return Threshold type
     */
    public Parameter getTarget() {
        return this.param;
    }

    /**
     * Setter for threshold value, test if value is of correct type
     *
     * @param value Threshold value
     */
    public void setValue(T value) {
        String type = value.getClass().getName();
        if (paramType.get(getTarget()).equals(type)) {
            this.value = null;
        }
        this.value = value;
    }

    /**
     * Setter for threshold type
     *
     * @param param Threshold type
     */
    private void setTarget(Parameter param) {
        this.param = param;
    }

    /**
     * Generate map of threshold type and expected value type
     */
    private void initTypeMap() {
        paramType.put(Parameter.EVENTRATE, "java.lang.Double");
        paramType.put(Parameter.POSITION, "[I");
        paramType.put(Parameter.REGION, "java.awt.Rectangle");
        paramType.put(Parameter.DISTANCE, "java.lang.Double");
        paramType.put(Parameter.SPEED, "java.lang.Double");
    }

}
