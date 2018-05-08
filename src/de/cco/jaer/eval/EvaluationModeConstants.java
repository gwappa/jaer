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

/**
 * the constants used primarily within EvaluationMode enum.
 *
 * @author gwappa
 */
public interface EvaluationModeConstants {
    static final int  FLAG_NONE      = 0x00;
    static final int  FLAG_ESTIMATE  = 0x01;
    static final int  FLAG_EVALUATE  = 0x02;
    static final int  FLAG_TRIGGER   = 0x04;

    static final String STR_NONE     = "NONE";
    static final String STR_ESTIMATE = "ESTIMATE";
    static final String STR_EVALUATE = "EVALUATE";
    static final String STR_TRIGGER  = "TRIGGER";
}
