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

import com.jogamp.opengl.GLAutoDrawable;

/**
 * the interface that specifies the responsibility of a class
 * that delegates the behavior of CanvasManager for drawing of
 * tracked position and the target objects.
 *
 * Normally, this functionality is implemented by an EvaluationTarget object.
 *
 * @author gwappa
 * @see CanvasManager
 */
public interface CanvasAnnotator
    extends CanvasConstants
{
    /**
     * returns the name of this type.
     */
    String getSpecifier();

    /**
     * renders the parameter information `param` on `drawable`.
     *
     * You may want to use colors that are specified in CanvasColors (which this interface is derived from).
     *
     * @param param     the position information. can be null.
     * @param drawable  the container of the canvas.
     * @see CanvasColors
     */
    public void render(final TrackingParameter param, final GLAutoDrawable drawable);
}

