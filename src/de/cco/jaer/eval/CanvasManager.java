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
import java.awt.Font;

import javax.swing.event.MouseInputListener;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.awt.GLCanvas;

import net.sf.jaer.graphics.ChipCanvas;
import net.sf.jaer.util.TextRendererScale;

/**
 * the singleton manager that handles drawing on the 'canvas' of jAER viewer.
 * Normally, only EvaluationTarget's interact with this Manager.
 *
 * <p>
 * Each time a `net.sf.jaer.graphics.AEViewer` is initialized, CanvasManager gets reset
 * by the Viewer's `net.sf.jaer.graphics.ChipCanvas` object (i.e. thereby linked to the 'current screen').
 * </p>
 *
 * <h3>Canvas annotator</h3>
 * <p>
 * CanvasManager normally works with a `CanvasAnnotator` object in order to render the FastEvent-related
 * output on the screen.
 * </p>
 * <p>
 * Note that only one object can become the annotator at any given time.
 * Failure in doing so may cause unexpected problems.
 * </p>
 * <p>
 * For the time being, the default `EvaluationTarget` objects are themselves `CanvasAnnotator`s, as they
 * are capable (and responsible) of evaluating the tracked position and how to display the target information.
 * An EvaluationTarget becomes the annotator for CanvasManager as soon as it becomes the active target
 * (this procedure is done in the corresponding methods in FastEventManager).
 * </p>
 *
 * <h3>Canvas input handler</h3>
 * <p>
 * In order for an object to work directly with cursor positions in the screen (such as the 'drawing' functionality
 * of RectangularTarget), it can register itself as the "input handler" for the CanvasManager.
 * CanvasManager, in turn, registers the object as the MouseInputListener of the screen, and the object can thereby
 * receive events such as mousePressed(), mouseDragged() etc. generated from the screen.
 *
 * (TODO: update the mouse listener when AEViewer is reset!)
 * </p>
 * <p>
 * Similarly to the case of the annotator, only one object can become the input handler at a time.
 * </p>
 *
 * @author gwappa
 *
 * @see EvaluationTarget
 * @see CanvasAnnotator
 * @see CanvasInputHandler
 */
public class CanvasManager
    implements CanvasConstants
{
    /**
     * dimensions of the current canvas
     */
    private static int          width_          = 0;
    private static int          height_         = 0;
    private static ChipCanvas   canvas_         = null;

    /**
     * current tracking parameter
     * @see TrackingParameter
     */
    static TrackingParameter    param_         = null;

    /**
     * the delegate to render the position and target objects.
     */
    static CanvasAnnotator      annotator_     = null;

    /**
     * the listener interface to be notified of mouse actions.
     */
    static CanvasInputHandler   handler_       = null;

    /**
     * the configuration about whether or not to draw tracking parameter info.
     */
    static boolean              displaysInfo_   = false;

    /**
     * used to draw parameter set information on screen
     */
    static TextRenderer         evalRenderer = null;

    /**
     * method to adjust the Manager with the new canvas size.
     * called typically when a new AEViewer is initialized.
     *
     * @see net.sf.jaer.graphics.AEViewer
     */
    public static final void reset(int width, int height, ChipCanvas canvas) {
        width_  = width;
        height_ = height;
        canvas_ = canvas;
        evalRenderer = null;
    }

    /**
     * sets how to render the position and target objects.
     */
    public static final void setAnnotator(CanvasAnnotator annotator) {
        annotator_ = annotator;
    }

    /**
     * sets CanvasListener to the manager.
     * the manager calls the listener's startReceivingFromCanvas() method in turn.
     * the listener will keep notified of mouse events until the manager calls
     * stopReceivingFromCanvas() method.
     *
     * if the registration fails (e.g. if canvas is not available), it may call
     * stopReceivingFromCanvas() method.
     *
     * @param handler  the CanvasInputHandler object
     */
    public static void setInputHandler(CanvasInputHandler handler) {
        clearInputHandler();

        if (canvas_ != null) {
            handler_ = handler;
            registerHandlerToCanvas();
        } else {
            handler.stopReceivingFromCanvas();
        }
    }

    /**
     * explicitly de-registers the canvas listener.
     * the manager will call stopReceivingFromCanvas() method of the listener.
     */
    public static void clearInputHandler() {
        if (handler_ != null) {
            removeHandlerToCanvas();
            handler_ = null;
        }
    }

    /**
     * a private subroutine to add the CanvasListener to the Canvas.
     */
    private static void registerHandlerToCanvas() {
        GLCanvas glCanvas = (GLCanvas)canvas_.getCanvas();
        glCanvas.addMouseListener(handler_);
        glCanvas.addMouseMotionListener(handler_);
        handler_.startReceivingFromCanvas(canvas_, width_, height_);
    }

    /**
     * a private subroutine to remove the CanvasListener from the Canvas.
     */
    private static void removeHandlerToCanvas() {
        GLCanvas glCanvas = (GLCanvas)canvas_.getCanvas();
        glCanvas.removeMouseListener(handler_);
        glCanvas.removeMouseMotionListener(handler_);
        handler_.stopReceivingFromCanvas();
    }

    /**
     * update the tracking parameter set.
     * called typically from the filterPacket() method of e.g. MeanTracker.
     * @see TrackingParameter
     */
    public static final void setTrackingParameter(TrackingParameter param) {
        param_ = param;
    }

    /**
     * returns the current setting on whether or not to display
     * tracking parameter info as lines of text.
     */
    public static final boolean getDisplaysInformation() {
        return displaysInfo_;
    }

    /**
     * configures whether or not to display tracking parameter info as lines of text.
     */
    public static final void setDisplaysInformation(boolean value) {
        displaysInfo_ = value;
    }

    /**
     * draws tracked position, target, and (optionally) information on screen.
     */
    public static final void render(final GLAutoDrawable drawable) {
        if (annotator_ != null) {
            // draw the position and the target info on the screen
            annotator_.render(param_, drawable);
        } else {
            // use CanvasManager's default procedure
            if (param_ != null) {
                if (param_.type == CoordinateType.POSITION) { 
                    renderPositionDefault(drawable, COLOR_DEFAULT);
                } else {
                    renderAngleDefault(drawable, COLOR_DEFAULT);
                }
            }
        }

        if (displaysInfo_) {
            // write packet info on the screen
            final Color color = ((param_ == null)||(!param_.eval))? COLOR_DEFAULT:COLOR_ACTIVE;
            renderInformationDefault(drawable, color);
        }
    }

    /**
     * draws the parameter set information on screen (default fallback method)
     */
    public static final void renderInformationDefault(final GLAutoDrawable drawable,
            final java.awt.Color color)
    {
        final GL2    gl     = drawable.getGL().getGL2();

        // obtain parameter info from ResultEvaluator
        String[] cols = param_.getHeadersAsString();
        String[] data = param_.getDataAsString();

        // settings on where to draw texts
        int x = -85;        // the horizontal position relative to the camera's field of view
        int y = 140;        // the vertical position relative to the camera's field of view
        final int offset = 10;    // the vertical increment for line feeds

        // we re-use evalRenderer throughout the session
        if (evalRenderer == null) {
            evalRenderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 42));
        }

        // the first parameter info is the target name
        String name = "Target type: " + ((annotator_ == null)? "unknown":annotator_.getSpecifier());
        // generate the size settings for the name to be drawn
        final float textScale = TextRendererScale.draw3dScale(evalRenderer, name, canvas_.getScale(), width_, .17f);

        // draw lines of text
        // parameter name is drawn at the beginning
        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glTranslatef(x, y, 0);
        evalRenderer.setColor(1, 1, 1, 1);
        evalRenderer.begin3DRendering();
        evalRenderer.draw3D(name, 0, 0, 0, textScale);
        y -= offset;    // vertical position gets decremented to point the next line
                        // (note that Y is the largest at the top, and smaller towards the bottom)
        evalRenderer.end3DRendering();
        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        gl.glPopMatrix();

        // other parameter information is drawn afterwards
        for (int i = 0; i < cols.length; i++) {
            gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
            gl.glPushMatrix();
            gl.glTranslatef(x, y, 0);

            // highlight the parameter that is the current evaluation criterion
            // if (cols[i].equalsIgnoreCase(reval.getThreshold().getTarget().toString())) {
            //     if (param_.eval) {
            //         // on triggered case, turn to GREEN
            //         evalRenderer.setColor(0, 1, 0, 1);
            //     } else {
            //         // otherwise turn to RED
            //         evalRenderer.setColor(1, 0, 0, 1);
            //     }
            // }
            // else {
            //     // if the parameter is not the criterion of evaluation,
            //     // use the default WHITE color for drawing the line
            //     evalRenderer.setColor(1, 1, 1, 1);
            // }
            evalRenderer.setColor(COLOR_DEFAULT);

            // actual drawing
            // (calls to begin/end have bad performance; use xx3DRendering() methods)
            evalRenderer.begin3DRendering();
            String s = String.format("%s: %s", cols[i], data[i]);
            evalRenderer.draw3D(s, 0, 0, 0, textScale);
            y -= offset; // go to the next line
            evalRenderer.end3DRendering();
            gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
            gl.glPopMatrix();
        }
    }

    private static float [] colorComponents = new float[3];

    /**
     * draws the tracked position (default fallback method)
     */
    public static final void renderPositionDefault(final GLAutoDrawable drawable,
            final java.awt.Color color)
    {
        if (param_ != null) {
            // draw the position

            final double x      = param_.coords[0];
            final double y      = param_.coords[1];
            final double stdx   = param_.confidence[0];
            final double stdy   = param_.confidence[1];
            final GL2    gl     = drawable.getGL().getGL2();
            // NOTE: `gl` is of chip pixel context with lower-left corner = (0,0)

            gl.glPushMatrix();

            // draw confidence rectangle
            colorComponents = color.getRGBColorComponents(colorComponents);
            gl.glColor3f(colorComponents[0], colorComponents[1], colorComponents[2]);
            gl.glLineWidth(LINE_WIDTH_SHAPES);
            gl.glBegin(GL2.GL_LINE_LOOP);
            gl.glVertex2d(x - stdx, y - stdy);
            gl.glVertex2d(x + stdx, y - stdy);
            gl.glVertex2d(x + stdx, y + stdy);
            gl.glVertex2d(x - stdx, y + stdy);
            gl.glEnd();

            // draw cross at mean 
            gl.glLineWidth(LINE_WIDTH_CURSOR);
            gl.glBegin(GL2.GL_LINES);
            gl.glVertex2d(x, y + 2);
            gl.glVertex2d(x, y - 2);
            gl.glVertex2d(x + 2, y);
            gl.glVertex2d(x - 2, y);
            gl.glEnd();

            gl.glPopMatrix();
        }
    }

    /**
     * draws the tracked angle (default fallback method)
     */
    public static final void renderAngleDefault(final GLAutoDrawable drawable,
            final java.awt.Color color)
    {
        // TODO
    }
}
