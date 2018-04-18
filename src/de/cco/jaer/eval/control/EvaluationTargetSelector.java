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

import java.util.HashMap;
import java.awt.Dimension;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;

import de.cco.jaer.eval.CoordinateType;
import de.cco.jaer.eval.TrackingDelegate;
import de.cco.jaer.eval.FastEventManager;
import de.cco.jaer.eval.EvaluationTarget;
import de.cco.jaer.eval.target.EventRateTarget;
import de.cco.jaer.eval.target.RectangularTarget;

/**
 * a GUI for selecting the evaluation target.
 *
 * @author gwappa
 */
public class EvaluationTargetSelector
    extends javax.swing.JTabbedPane
    implements javax.swing.event.ChangeListener
{
    private static final Class<? extends EvaluationTarget> []
        TARGETS = new Class [] { EventRateTarget.class, RectangularTarget.class };

    HashMap<String,EvaluationTarget> targets_ = new HashMap<>();

    boolean valueChanging_ = false;
    
    public EvaluationTargetSelector() {
        EvaluationTarget instance = null;
        String targetName = null;
        JPanel targetPanel = null;

        // iterate over target classes defined in TARGETS
        for (Class<? extends EvaluationTarget> cls: TARGETS) {
            try {
                instance = cls.newInstance();
                targetName = instance.getSpecifier();
                targetPanel = instance.getTargetControl();

                targets_.put(targetName, instance);
                addTab(targetName, targetPanel);

            } catch (Exception e) {
                System.out.println("***cannot instantiate class: "+cls.getName());
                e.printStackTrace();
            }
        }
        addChangeListener(this);
    }

    /**
     * updates the state of targets depending on the new tracking delegate.
     *
     * @param object the tracking delegate
     */
    public void setTrackingDelegate(TrackingDelegate object) {
        Dimension dim = object.getSensorDimension();
        CoordinateType type = object.getCoordinateType();

        boolean selectionUpdated = false; // whether we have to  change the 
                                          // target selection
        final int size           = getTabCount();
        int     firstAcceptable  = -1;    // the index of the first acceptable target

        valueChanging_ = true;

        EvaluationTarget target = null;
        boolean accepts = false;
        for (int i=0; i<size; i++) {
            // obtain the EvaluationTarget instance
            target = targets_.get(getTitleAt(i));
            // update with the new dimension
            target.resetDimension(dim);

            // check if this target accepts the coordinate type
            // and update the enabled status
            accepts = target.acceptsCoordinateType(type);
            setEnabledAt(i, accepts);

            if (accepts && (firstAcceptable == -1)) {
                // if enabled, (and if firstAcceptable is still -1),
                // update firstAcceptable index
                firstAcceptable = i;
            } else if (getSelectedIndex() == i) {
                // otherwise, if this is the selected index,
                // flag selectionUpdated
                selectionUpdated = true;
            }
        }

        if (selectionUpdated) {
            // if we have to change the selected tab,
            // update it by selecting `firstAcceptable`
            setSelectedIndex(firstAcceptable);

            // and notify FastEventManager of the change of selection
            FastEventManager.setEvaluationTarget(targets_.get(getTitleAt(firstAcceptable)));
        }

        valueChanging_ = false;
    }

    /**
     * notifies FastEventManager of the target change event.
     * normally this follows user-generated events.
     * any other internal changes (e.g. from setEvaluationTarget()) should be ignored.
     */
    @Override
    public void stateChanged(javax.swing.event.ChangeEvent evt) {
        if (valueChanging_) {
            // this means that this state change event
            // is fired from within this instance
            return;
        }
        valueChanging_ = true;
        String targetName = getTitleAt(getSelectedIndex());
        FastEventManager.setEvaluationTarget(targets_.get(targetName));
        valueChanging_ = false;
    }

    /**
     * updates the evaluation target from within the program.
     * typically called from FastEventManager, and used to update the UI part.
     */
    public void setEvaluationTarget(EvaluationTarget target) {
        if (valueChanging_) {
            // this means that this state change event
            // is fired from within this instance
            return;
        }

        valueChanging_ = true;
        String targetName = target.getSpecifier();

        // if the target object is already in our dictionary,
        // just select it and returns.
        if (targets_.containsValue(target)) {
            setSelectedIndex(indexOfTab(targetName));
            valueChanging_ = false;
            return;
        }

        // otherwise, update targets_ dictionary
        targets_.put(targetName, target);

        // update the tab UI.
        // search tabs for target name
        int     tabIndex    = indexOfTab(targetName);
        if (tabIndex == -1) {
            // if there is no name like this, append a new tab
            addTab(targetName, target.getTargetControl());
            // and update the index
            tabIndex = indexOfTab(targetName);
        } else {
            // otherwise replace the tab at the returned index
            removeTabAt(tabIndex);
            insertTab(targetName, null, target.getTargetControl(),
                    targetName, tabIndex);
        }
        // select the updated target
        setSelectedIndex(tabIndex);

        valueChanging_ = false;
    }
}
