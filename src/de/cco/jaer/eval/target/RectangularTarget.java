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
package de.cco.jaer.eval.target;

import de.cco.jaer.eval.CoordinateType;
import de.cco.jaer.eval.EvaluationMode;
import de.cco.jaer.eval.TrackingParameter;
import de.cco.jaer.eval.CanvasManager;
import de.cco.jaer.eval.FastEventManager;
import de.cco.jaer.eval.EventTriggerManager;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.event.MouseEvent;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import net.sf.jaer.graphics.ChipCanvas;

/**
 * the target type that checks if the position is within some rectangular region.
 * TODO: add PropertyChangeSupport?
 *
 * @author viktor, gwappa
 */
public class RectangularTarget
    implements de.cco.jaer.eval.EvaluationTarget,
               de.cco.jaer.eval.CanvasInputHandler,
               java.awt.event.ActionListener
{

    private static final String TARGET_NAME         = "Rectangle";
    private static final String LABEL_APPLY_CMD     = "Apply";
    private static final String LABEL_DRAW_CMD      = "Draw";
    private static final String LABEL_FIELD_X       = "Origin X";
    private static final String LABEL_FIELD_Y       = "Origin Y";
    private static final String LABEL_FIELD_W       = "Width";
    private static final String LABEL_FIELD_H       = "Height";

    private static final int    GRID_COLS           = 2;
    private static final int    GRID_ROWS         = 5;

    Dimension sensor    = new Dimension();
    Rectangle target    = new Rectangle();
    EvaluationMode mode = EvaluationMode.NONE;

    // the canvas to be drawn during drawing
    ChipCanvas  canvas    = null;
    // the origin of selection during drawing
    Point       selectionOrigin = new Point();

    JPanel control      = new JPanel();

    JTextField xfield   = new JTextField();
    JTextField yfield   = new JTextField();
    JTextField wfield   = new JTextField();
    JTextField hfield   = new JTextField();

    JButton applyButton = new JButton(LABEL_APPLY_CMD);
    JButton drawButton  = new JButton(LABEL_DRAW_CMD);

    public RectangularTarget() {
        // set up GUI
        control.setLayout(new GridLayout(GRID_ROWS,GRID_COLS));
        control.add(new JLabel(LABEL_FIELD_X));
        control.add(xfield);
        control.add(new JLabel(LABEL_FIELD_Y));
        control.add(yfield);
        control.add(new JLabel(LABEL_FIELD_W));
        control.add(wfield);
        control.add(new JLabel(LABEL_FIELD_H));
        control.add(hfield);
        control.add(drawButton);
        control.add(applyButton);

        xfield.setText(String.valueOf(target.x));
        yfield.setText(String.valueOf(target.y));
        wfield.setText(String.valueOf(target.width));
        hfield.setText(String.valueOf(target.height));
        updateWithTarget();

        xfield.addActionListener(this);
        yfield.addActionListener(this);
        wfield.addActionListener(this);
        hfield.addActionListener(this);
        applyButton.addActionListener(this);
        drawButton.addActionListener(this);
    }

    @Override
    public String toString() {
        return String.format("%s(%d;%d;%d;%d)", TARGET_NAME, target.x, target.y, target.width, target.height);
    }

    @Override
    public void resetDimension(Dimension dim) {
        sensor = dim;
        clipTargetToSensorDimension();
    }

    @Override
    public final void setEvaluationMode(final EvaluationMode value) {
        if (mode != value) {
            mode = value;
        }
    }

    @Override
    public final void startLogging() {
        updateWithTarget();
    }

    @Override
    public final void endLogging() {
        // do nothing
    }

    /**
     * called whenever there is a change to the target.
     * intended for the hook to log the update, and updating the text fields.
     */
    private void updateWithTarget() {
        // update text fields (should not fire actionPerformed())
        xfield.setText(String.valueOf(target.x));
        yfield.setText(String.valueOf(target.y));
        wfield.setText(String.valueOf(target.width));
        hfield.setText(String.valueOf(target.height));

        // log the change (if during logging)
        FastEventManager.logMessage(MSG_TYPE_TARGET, toString());
    }

    /**
     * dispatched when an update on the target value is being applied.
     */
    @Override
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        Object src = evt.getSource();
        if (src.equals(xfield)) {
            setX(xfield.getText());
        } else if (src.equals(yfield)) {
            setY(yfield.getText());
        } else if (src.equals(wfield)) {
            setWidth(wfield.getText());
        } else if (src.equals(hfield)) {
            setHeight(hfield.getText());
        } else if (src.equals(applyButton)) {
            setTarget();
        } else if (src.equals(drawButton)) {
            drawButton.setSelected(!drawButton.isSelected());
            setSelectionMode(drawButton.isSelected());
        }
    }

    @Override
    public void mouseClicked(MouseEvent evt) {
        // do nothing
    }

    @Override
    public void mouseEntered(MouseEvent evt) {
        // do nothing
    }

    @Override
    public void mouseExited(MouseEvent evt) {
        // do nothing for now
        // do not reset selection mode for the time being
    }

    /**
     * starts selecting the ROI from the clicked point.
     */
    @Override
    public void mousePressed(MouseEvent evt) {
        // get the chip-based coordinates
        selectionOrigin = canvas.getPixelFromMouseEvent(evt);
        // update ROI
        target.x = selectionOrigin.x;
        target.y = selectionOrigin.y;
        target.width = 0;
        target.height = 0;
    }

    @Override
    public void mouseReleased(MouseEvent evt) {
        updateSelection(evt);
        // CanvasManager.clearCanvasListener(); // disable auto-clearing for the time being
        updateWithTarget();
    }

    @Override
    public void mouseDragged(MouseEvent evt) {
        updateSelection(evt);
    }

    @Override
    public void mouseMoved(MouseEvent evt) {
        // do nothing
    }

    @Override
    public void startReceivingFromCanvas(ChipCanvas canvas, int width, int height) {
        drawButton.setSelected(true);
        this.canvas     = canvas;
        sensor.width    = width;
        sensor.height   = height;
    }

    @Override
    public void stopReceivingFromCanvas() {
        drawButton.setSelected(false);
        this.canvas = null;
    }

    /**
     * update the ROI selection depending on the current mouse position.
     */
    private void updateSelection(MouseEvent evt) {
        Point current = canvas.getPixelFromMouseEvent(evt);
        target.x      = Math.min(selectionOrigin.x, current.x);
        target.y      = Math.min(selectionOrigin.y, current.y);
        target.width  = Math.max(selectionOrigin.x, current.x) - target.x;
        target.height = Math.max(selectionOrigin.y, current.y) - target.y;
        clipTargetToSensorDimension();
    }

    /**
     * clips the target region based on the current sensor dimensions.
     * @returns if the target region has been updated by clipping
     */
    private boolean clipTargetToSensorDimension() {
        boolean changed = false;

        if (target.x > sensor.width) {
            target.x = sensor.width;
            target.width = 0;
            changed = true;
        } else if ((target.x + target.width) > sensor.width) {
            target.width = sensor.width - target.x;
            changed = true;
        }

        if (target.y > sensor.height) {
            target.y = sensor.height;
            target.height = 0;
            changed = true;
        } else if ((target.y + target.height) > sensor.height) {
            target.height = sensor.height - target.y;
            changed = true;
        }
        return changed;
    }

    /**
     * a support function to parse a String number value.
     */
    private int parseInt(String label, String text, int orig) {
        int newvalue = orig;

        try {
            newvalue = Integer.parseInt(text);
        } catch (NumberFormatException nfe) {
            // TODO: display error dialog %%displayError
        }
        return newvalue;
    }

    /**
     * parses the String as a number and applies it to X.
     */
    public void setX(String text) {
        setX(parseInt("X", text, target.x));
    }
    public void setX(int value) {
        if (value != target.x) {
            target.x = value;
            updateWithTarget();
        } else {
            xfield.setText(String.valueOf(value));
        }
    }

    /**
     * parses the String as a number and applies it to Y.
     */
    public void setY(String text) {
        setY(parseInt("Y", text, target.y));
    }
    public void setY(int value) {
        if (value != target.y) {
            target.y = value;
            updateWithTarget();
        } else {
            yfield.setText(String.valueOf(value));
        }
    }

    /**
     * parses the String as a number and applies it to W.
     */
    public void setWidth(String text) {
        setWidth(parseInt("Width", text, target.width));
    }
    public void setWidth(int value) {
        if (value != target.width) {
            target.width = value;
            updateWithTarget();
        } else {
            wfield.setText(String.valueOf(value));
        }
    }

    /**
     * parses the String as a number and applies it to H.
     */
    public void setHeight(String text) {
        setHeight(parseInt("Height", text, target.height));
    }
    public void setHeight(int value) {
        if (value != target.height) {
            target.height = value;
            updateWithTarget();
        } else {
            hfield.setText(String.valueOf(value));
        }
    }

    /**
     * parses the textfield values as numbers and applies it to the target.
     */
    public void setTarget() {
        int value = parseInt("X", xfield.getText(), target.x);
        boolean hasChange = false;

        if (value != target.x) {
            target.x = value;
            hasChange = true;
        } else {
            xfield.setText(String.valueOf(value));
        }
        
        if ((value = parseInt("Y", yfield.getText(), target.y))
                != target.y) {
            target.y = value;
            hasChange = true;
        } else {
            yfield.setText(String.valueOf(value));
        }

        if ((value = parseInt("Width", wfield.getText(), target.width))
                != target.width) {
            target.width = value;
            hasChange = true;
        } else {
            wfield.setText(String.valueOf(value));
        }
 
        if ((value = parseInt("Height", hfield.getText(), target.height))
                != target.height) {
            target.height = value;
            hasChange = true;
        } else {
            hfield.setText(String.valueOf(value));
        }

        if (hasChange) {
            updateWithTarget();
        }
    }

    /**
     * sets ROI-selection mode.
     */
    private void setSelectionMode(boolean on) {
        if (on) {
            CanvasManager.setInputHandler(this);
        } else {
            CanvasManager.clearInputHandler();
        }
    }

    /**
     * returns the name of this target type.
     */
    @Override
    public String getSpecifier() {
        return TARGET_NAME;
    }

    /**
     * returns if this target type can be used with given coordinate type.
     *
     * @see CoordinateType
     */
    @Override
    public boolean acceptsCoordinateType(CoordinateType type) {
        switch (type) {
        case POSITION:
            return true;
        case ANGLE:
        default:
            return false;
        }
    }

    /**
     * returns the controller JPanel object for this target.
     */
    @Override
    public javax.swing.JPanel getTargetControl() {
        return control;
    }

    /**
     * evaluates the given parameter set.
     * evaluation status `eval` of the given parameter is updated.
     * the evaluation status is returned at the same time.
     *
     * @param param only processes POSITION-type parameter.
     * @return status of evaluation
     */
    @Override
    public final void evaluateParameter(final TrackingParameter param) {
        if (mode.isEvaluating() && (param.type == CoordinateType.POSITION)) {
            // evaluate tracker output
            // updates param.eval and returns it
            final double xdistance = param.coords[0] - target.getX();
            final double ydistance = param.coords[1] - target.getY();
            param.eval      = ((xdistance >= 0) && (ydistance >= 0) && 
                               (xdistance <= target.getWidth()) && 
                               (ydistance <= target.getHeight()));
            
            if (mode.isTriggering()) {
                EventTriggerManager.setEvent(param.eval);
            }
        } else {
            // if not in evaluation, `eval` field is `false` by default.
            param.eval = false;
        }
    }

    float [] colorComponents = new float[3];
    @Override
    public final void render(final TrackingParameter param, final GLAutoDrawable drawable) {
        final Color color   = (param != null && param.eval)? COLOR_ACTIVE : COLOR_DEFAULT;
        
        // assume that param is of type POSITION, since we have already filtered it
        CanvasManager.renderPositionDefault(drawable, color);
        
        // draw the target rectangle using corresponding color
        final GL2   gl      = drawable.getGL().getGL2();

        // get color components
        colorComponents = color.getRGBColorComponents(colorComponents);

        gl.glPushMatrix();

        // initialize position and color
        gl.glTranslatef(-.5f, -.5f, 0);
        gl.glColor4f(colorComponents[0], colorComponents[1], colorComponents[2], 0.3f);

        // fill the target region
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glRectf(target.x, target.y, target.x + target.width, target.y + target.height);
        gl.glDisable(GL.GL_BLEND);

        // draw the border
        gl.glColor4f(colorComponents[0], colorComponents[1], colorComponents[2], 1.0f);
        gl.glLineWidth(LINE_WIDTH_SHAPES);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2i(target.x, target.y);
        gl.glVertex2i(target.x + target.width, target.y);
        gl.glVertex2i(target.x + target.width, target.y + target.height);
        gl.glVertex2i(target.x, target.y + target.height);
        gl.glEnd();

        gl.glPopMatrix();
    }

    /**
     * deprecated: retained just for compatibility.
     */
    public final void renderLineTarget(final TrackingParameter param, final GLAutoDrawable drawable)
    {
        final Color color = param.eval? COLOR_ACTIVE: COLOR_DEFAULT;
        final GL2    gl   = drawable.getGL().getGL2();

        // assume that param is of type POSITION, since we have already filtered it
        CanvasManager.renderPositionDefault(drawable, color);

        // NOTE: originally this should be of two points (x1,y1,x2,y2)
        int[] positions = new int[] {(int)(target.x), (int)(target.y), (int)(target.width), (int)(target.height)};

        gl.glPushMatrix();
        gl.glLineWidth(2f);
        gl.glTranslatef(-.5f, -.5f, 0);
        gl.glBegin(GL.GL_LINES);
        gl.glVertex2i(positions[0], positions[1]);
        gl.glVertex2i(positions[2], positions[3]);
        gl.glEnd();
        gl.glPopMatrix();
    }
}

