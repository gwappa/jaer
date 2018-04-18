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
package de.cco.jaer.eval.control;

import de.cco.jaer.eval.FastEventManager;
import de.cco.jaer.eval.EvaluationMode;

import java.util.HashMap;

import javax.swing.JToggleButton;
import javax.swing.ButtonGroup;
import javax.swing.BoxLayout;
import javax.swing.Box;

/**
 * a thin GUI for selecting the evaluation mode.
 * the selected mode is notified to FastEventManager.
 *
 * @author gwappa
 */
public class EvaluationModeSelector
    extends javax.swing.JPanel
    implements java.awt.event.ActionListener
{

    private static final EvaluationMode DEFAULT_MODE = EvaluationMode.ESTIMATE;

    /**
     * label texts for options
     */
    private static final String     LAB_ESTIMATE    = "Estimate";
    private static final String     LAB_EVALUATE    = "Evaluate";
    private static final String     LAB_TRIGGER     = "Trigger";

    /**
     * tooltip texts
     */
    private static final String     TOOLTIP_ESTIMATE    = "Estimate the position, but not evaluate against the target.";
    private static final String     TOOLTIP_EVALUATE    = "Estimate and evaluate the position against the target, but not trigger output.";
    private static final String     TOOLTIP_TRIGGER     = "Enable trigger output based on evaluation of the position against the target.";

    HashMap<String,EvaluationMode>          modemap_     = new HashMap<>();
    HashMap<EvaluationMode,JToggleButton>   buttonmap_   = new HashMap<>();
    ButtonGroup     group_          = new ButtonGroup();

    boolean valueChanging_ = false;

    public EvaluationModeSelector() {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        
        // add options
        addEvaluationMode(LAB_ESTIMATE, EvaluationMode.ESTIMATE, TOOLTIP_ESTIMATE);
        addEvaluationMode(LAB_EVALUATE, EvaluationMode.EVALUATE, TOOLTIP_EVALUATE);
        addEvaluationMode(LAB_TRIGGER, EvaluationMode.TRIGGER, TOOLTIP_TRIGGER);
        add(Box.createHorizontalGlue());

        // set default
        setCurrentMode(DEFAULT_MODE);
    }

    /**
     * a private subroutine used to add EvaluationMode-related buttons.
     */
    private void addEvaluationMode(String label, EvaluationMode mode, String tooltip) {
        JToggleButton button = new JToggleButton(label);
        button.setActionCommand(label);
        button.addActionListener(this);
        if (tooltip != null) {
            button.setToolTipText(tooltip);
        }

        modemap_.put(label, mode);
        buttonmap_.put(mode, button);
        group_.add(button);
        add(button);
    }

    /**
     * a setter method for the current mode.
     */
    public void setCurrentMode(EvaluationMode mode) {
        if (valueChanging_) {
            // the mode change is triggered from inside this instance
            return;
        }

        if (mode == null) {
            mode = EvaluationMode.NONE;
        } else {
            JToggleButton button = buttonmap_.get(mode);
            if (button != null) {
                button.setSelected(true);
            } else {
                mode = EvaluationMode.NONE;
            }
        }

        valueChanging_ = true;
        FastEventManager.setEvaluationMode(mode);
        valueChanging_ = false;
    }

    /**
     * a getter method for the current mode.
     */
    public EvaluationMode getCurrentMode() {
        javax.swing.ButtonModel model = group_.getSelection();
        if (model == null) {
            return EvaluationMode.NONE;
        } else {
            return modemap_.get(model.getActionCommand());
        }
    }

    /**
     * the callback method for receiving UI updates.
     */
    @Override
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        final String            cmd     = evt.getActionCommand();
        final EvaluationMode    mode    = modemap_.get(cmd);
        final JToggleButton     button  = buttonmap_.get(mode);
        final EvaluationMode    value   = (button.isSelected())? mode : EvaluationMode.NONE;

        valueChanging_ = true;
        FastEventManager.setEvaluationMode(value);
        valueChanging_ = false;
    }
}
