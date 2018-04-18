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

import java.awt.Color;

/**
 * the constants used to draw things on screen.
 * Used in CanvasAnnotator or CanvasInputHandler classes.
 *
 * @author gwappa
 * @see CanvasAnnotator
 * @see CanvasInputHandler
 */
public interface CanvasConstants
{
    /**
     * the color used for any non-specific rendering
     */
    static final Color COLOR_DEFAULT    = Color.WHITE;

    /**
     * the color used for active (eg triggered) object
     */
    static final Color COLOR_ACTIVE     = Color.YELLOW; 

    /**
     * the color used for tracked object
     */
    static final Color COLOR_TRACKED    = Color.RED;
    
    /**
     * the line width for drawing shapes.
     */
    static final float  LINE_WIDTH_SHAPES = 2f;
    
    /**
     * the line width for drawing cursor.
     */
    static final float  LINE_WIDTH_CURSOR = 4f;
}

