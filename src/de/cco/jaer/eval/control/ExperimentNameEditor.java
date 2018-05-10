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

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.BoxLayout;

import de.cco.jaer.eval.FastEventManager;

/**
 * the UI class for setting the "experiment name" to the logging data file.
 *
 * @author gwappa
 */
public class ExperimentNameEditor
    extends JPanel 
    implements java.awt.event.ActionListener
{
    private static final String     MSG_TYPE_EXPNAME = "experiment";
    private static final String     LAB_SET     = "Set";
    private static final String     DEFAULT_NAME  = "";

    String      currentName = DEFAULT_NAME;
    boolean     valueChanging = false;

    JTextField  editor      = new JTextField();
    JButton     applyButton = new JButton(LAB_SET);

    public ExperimentNameEditor() {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        editor.addActionListener(this);
        applyButton.addActionListener(this);
        add(editor);
        add(applyButton);
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent ae) {
        // no matter the source, apply it
        setName(parseName(editor.getText()));
    }

    /**
     * parses the parameter String object for a new note.
     * if the note has no contents, it sets the current note to be null.
     * otherwise, it sets the content of the parameter to be the current note.
     *
     * @param source the String object to be parsed.
     * @return the String object to be set as a new note.
     */
    private String parseName(String source) {
        if ((source == null) || (source.trim().length() == 0)) {
            return null;
        } else {
            return source;
        }
    }

    /**
     * applies the new note String as the current note.
     * it updates the internal buffer, updates the status text,
     * and notifies FastEventManager with the update.
     *
     * @param note the new note to be applied.
     */
    public void setName(String note) {
        if (!valueChanging) {
            valueChanging = true;
            // update the internal buffer
            currentName = note;
            // update the status text accordingly
            if (currentName == null) {
                currentName = DEFAULT_NAME;
            }

            FastEventManager.setExperimentName(currentName); // recursion prevented by valueChanging
            valueChanging = false;
        }
    }

    public String getName() {
        return currentName;
    }

    public String formatName() {
        return currentName.replaceAll("[ :\\?!#;\\$]", "_").replaceAll("\\+","&").replaceAll("/","-");
    }

    /**
     * the callback method that FastEventManager notifies the start of logging.
     */
    public void startLogging() {
        setEnabled(false);
        FastEventManager.logMessage(MSG_TYPE_EXPNAME, "\"" + currentName + "\"");
    }

    /**
     * the callback method that FastEventManager notifies the end of logging.
     */
    public void endLogging() {
        setEnabled(true); 
    }
}

