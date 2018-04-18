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

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.BoxLayout;

import com.jogamp.opengl.GLAutoDrawable;

/**
 * the target type that uses the rate of events as the criterion.
 * TODO: add PropertyChangeSupport?
 *
 * @author viktor, gwappa
 */
public class EventRateTarget
    implements de.cco.jaer.eval.EvaluationTarget, java.awt.event.ActionListener
{
    private static final String TARGET_NAME         = "Event rate";
    private static final String LABEL_EVTS_PER_MS   = "events/ms";
    private static final String LABEL_APPLY_CMD     = "Apply";
    private static final int    DEFAULT_COLUMNS     = 20;

    double      target      = 1.0;
    EvaluationMode mode     = EvaluationMode.NONE;

    JPanel      control     = new JPanel();
    JTextField  field       = new JTextField(DEFAULT_COLUMNS);
    JButton     applyButton = new JButton(LABEL_APPLY_CMD);

    public EventRateTarget() {
        // set up GUI
        field.setText(String.valueOf(target));

        control.add(field);
        control.add(new JLabel(LABEL_EVTS_PER_MS));
        control.add(applyButton);

        updateWithTarget();
        field.addActionListener(this);
        applyButton.addActionListener(this);
    }

    @Override
    public String toString() {
        return String.format("EventRate(%f)", target);
    }

    public final void resetDimension(Dimension dim) {
        // do nothing
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

    private void updateWithTarget() {
        FastEventManager.logMessage(MSG_TYPE_TARGET, toString());
    }

    /**
     * dispatched when an update on the target value is being applied.
     */
    @Override
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        double newvalue = target;

        // try parsing the value in the field
        try {
            newvalue = Double.parseDouble(field.getText());    
        } catch (NumberFormatException nfe) {
            // TODO: display error dialog %%displayError

            // revert the content of the field
            field.setText(String.valueOf(target));
        }
        if (target != newvalue) {
            target = newvalue;
            updateWithTarget();
        }
    }

    @Override
    public String getSpecifier() {
        return TARGET_NAME;
    }

    @Override
    public boolean acceptsCoordinateType(CoordinateType type) {
        return true; // does not depend on the coordinate type
    }

    @Override
    public JPanel getTargetControl() {
        return control;
    }

    @Override
    public final void evaluateParameter(TrackingParameter param) {
        if (mode.isEvaluating()) {
            // evaluate tracker output
            // updates param.eval and returns it
            final double width_ms = ((double)(param.lastts - param.firstts))/1000;
            param.eval            = ((param.nevents/width_ms) >= target);
            
            if (mode.isTriggering()) {
                EventTriggerManager.setEvent(param.eval);
            }

        } else {
            // if not in evaluation, `eval` field is `false` by default.
            param.eval = false;
        }
    }

    @Override
    public final void render(final TrackingParameter param, final GLAutoDrawable drawable) {
        if (param == null) {
            return;
        }

        if (param.type == CoordinateType.POSITION) {
            CanvasManager.renderPositionDefault(drawable, param.eval? COLOR_ACTIVE: COLOR_DEFAULT);
        } else if (param.type == CoordinateType.ANGLE) {
            CanvasManager.renderAngleDefault(drawable, param.eval? COLOR_ACTIVE: COLOR_DEFAULT);
        } else {
            // do nothing
        }
    }
}

