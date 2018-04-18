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

import javax.swing.event.MouseInputListener;
import com.jogamp.opengl.awt.GLCanvas;
import net.sf.jaer.graphics.ChipCanvas;

/**
 * the mouse input interface used for receiving events that occurs on the screen.
 *
 * <p>
 * By using in combination with `CanvasManager.setInputHandler()` method,
 * you can start receiving events from jAER viewer screen.
 * During the period when CanvasManager calls the handler's `startReceivingFromCanvas()` method
 * and when it calls the `stopReceivingFromCanvas()` method, the handler can expect to receive the 
 * mouse inputs from the screen.
 * </p>
 * <p>
 * More detailed procedures are as follows:
 *
 * <ol>
 * <li>The object calls `CanvasManager.setInputHandler(CanvasInputHandler)`.</li>
 * <li>CanvasManager tries to register the object as the MouseInputListener of the
 * current screen.</li>
 * <li>If registration is successful, CanvasManager calls the handler's `startReceivingFromCanvas(ChipCanvas,int,int)`
 * method. Otherwise, it calls the handler's `stopReceivingFromCanvas()`</li>
 * <li>When another handler object tries to get registered as the input handler,
 * CanvasManager calls `stopReceivingFromCanvas()` of the current handler to notify of the switch.</li>
 * <li>Alternatively, the handler object can always call `CanvasManager.clearInputHandler()` method,
 * which, in turn, ends up calling the handler's ``stopReceivingFromCanvas()` method.</li>
 * </ol>
 * </p>
 *
 * @author gwappa
 * @see CanvasManager
 * @see net.sf.jaer.graphics.ChipCanvas
 */
public interface CanvasInputHandler
    extends MouseInputListener
{
    /**
     * notified from CanvasManager when CanvasManager.setInputHandler() is successful.
     *
     * @param object a ChipCanvas object which this listener gets registered.
     */
    void startReceivingFromCanvas(ChipCanvas object, int width, int height);

    /**
     * notified from CanvasManager when CanvasManager.clearInputHandler() is acknowledged.
     */
    void stopReceivingFromCanvas();
}

