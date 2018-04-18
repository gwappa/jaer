/*
 * Copyright (C) 2017-2018 Viktor Bahr, Keisuke Sehara
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

import java.util.HashMap;

/**
 * the mode of evaluation, depending on which changes the behavior of:
 * (i) whether or not to estimate the position,
 * (ii) whether or not to evaluate the position for the target, and
 * (iii) whether or not to trigger output to FastEventManager.
 *
 * <p>
 * The three levels can be extracted using the object's `isEstimating()`, `isEvaluating()`
 * and `isTriggering()` methods.
 * The return values are as follows:
 *
 * <table>
 * <tr><th>Mode</th><th>isEstimating()</th><th>isEvaluating()</th><th>isTriggering()</th></tr>
 * <tr><td>EvaluationMode.NONE</td><td>false</td><td>false</td><td>false</td></tr>
 * <tr><td>EvaluationMode.ESTIMATE</td><td>true</td><td>false</td><td>false</td></tr>
 * <tr><td>EvaluationMode.EVALUATE</td><td>true</td><td>true</td><td>false</td></tr>
 * <tr><td>EvaluationMode.TRIGGER</td><td>true</td><td>true</td><td>true</td></tr>
 * </table>
 *
 * Note that the logging is primarily managed by FastEventManager, and is not represented by this class.
 * </p>
 *
 * <p>
 * For the time being, the only classes that actually use this information is those that implements the
 * `EvaluationTarget` interface.
 * </p>
 *
 * <p>
 * If you want to change the evaluation mode programmatically, use `FastEventManager.setEvaluationMode()`
 * method (that, in turn, calls `EvaluationTarget.setEvaluationMode()`).
 * </p>
 *
 * @author gwappa
 *
 * @see FastEventManager
 * @see EvaluationTarget
 */
public enum EvaluationMode 
    implements EvaluationModeConstants
{
    NONE(FLAG_NONE),
    ESTIMATE(FLAG_ESTIMATE),
    EVALUATE(FLAG_ESTIMATE | FLAG_EVALUATE),
    TRIGGER(FLAG_ESTIMATE | FLAG_EVALUATE | FLAG_TRIGGER);

    private final int  flag;

    private EvaluationMode(int flag) {
        this.flag = flag;
    }

    /**
     * returns whether this mode implies estimation of coordinates.
     */
    public final boolean isEstimating() {
        return (this.flag & FLAG_ESTIMATE) != 0x00;
    }

    /**
     * returns whether this mode implies evaluation against a threshold.
     */
    public final boolean isEvaluating() {
        return (this.flag & FLAG_EVALUATE) != 0x00;
    }

    /**
     * returns whether this mode implies trigger generation according to the 
     * result of evaluation.
     */
    public final boolean isTriggering() {
        return (this.flag & FLAG_TRIGGER) != 0x00;
    }

    /**
     * returns its string representation.
     */
    public final String toString() {
        switch(this) {
        case ESTIMATE:
            return STR_ESTIMATE;
        case EVALUATE:
            return STR_EVALUATE;
        case TRIGGER:
            return STR_TRIGGER;
        case NONE:
        default:
            return STR_NONE;
        }
    }

    /**
     * returns the EvaluationMode value that corresponds to given String.
     * @param value the String value (one of "NONE", "ESTIMATE", "EVALUATE", "TRIGGER")
     * @throws IllegalArgumentException if the String value does not correspond to any of the enum values.
     */
    static final EvaluationMode parseMode(final String value) 
        throws IllegalArgumentException
    {
        // convert to what STR_XXX value should be
        String repr = value.toUpperCase();

        if (repr.equals(NONE.toString())) {
            return NONE;
        } else if (repr.equals(ESTIMATE.toString())) {
            return ESTIMATE;
        } else if (repr.equals(EVALUATE.toString())) {
            return EVALUATE;
        } else if (repr.equals(TRIGGER.toString())) {
            return TRIGGER;
        } else {
            throw new IllegalArgumentException(repr);
        }
    }
}

