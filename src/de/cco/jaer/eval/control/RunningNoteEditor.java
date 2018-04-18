
/*
 * Copyright (C) 2017 Viktor Bahr, Keisuke Sehara
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
 * the UI class for adding "running notes" to the logging data file.
 *
 * @author gwappa
 */
public class RunningNoteEditor
    extends JPanel 
    implements java.awt.event.ActionListener
{
    private static final String     MSG_TYPE_COMMENT = "comment";

    private static final String     LAB_SET     = "Set";
    private static final String     LAB_WRITE   = "Write";

    private static final String     EMPTY_TEXT  = "";

    /**
     * the text that is to be displayed.
     */
    private static final String     LAB_NOCONTENT   = "<no note content>";

    /**
     * the message texts to be shown in the status bar.
     */
    private static final String     MSG_INST_UPDATE = "Write the note and click on the button to update";
    private static final String     MSG_TOBE_APPLIED = "The note will be applied once logging is on";
    private static final String     MSG_HAS_APPLIED  = "The note has been applied";

    String      currentNote = null;
    boolean     isLogging   = false;

    JLabel      display     = new JLabel(LAB_NOCONTENT);
    JTextField  editor      = new JTextField();
    JButton     applyButton = new JButton(LAB_SET);
    JLabel      status      = new JLabel(MSG_INST_UPDATE);

    public RunningNoteEditor() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        add(display);

        editor.addActionListener(this);
        applyButton.addActionListener(this);
        JPanel editPanel = new JPanel();
        editPanel.setLayout(new BoxLayout(editPanel, BoxLayout.LINE_AXIS));
        editPanel.add(editor);
        editPanel.add(applyButton);
        add(editPanel);

        add(status);
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent ae) {
        // no matter the source, apply the note
        setNote(parseNote(editor.getText()));
    }

    /**
     * parses the parameter String object for a new note.
     * if the note has no contents, it sets the current note to be null.
     * otherwise, it sets the content of the parameter to be the current note.
     *
     * @param source the String object to be parsed.
     * @return the String object to be set as a new note.
     */
    private String parseNote(String source) {
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
    public void setNote(String note) {
        // update the internal buffer
        currentNote = note;
        // clear the content of the editor
        editor.setText(EMPTY_TEXT);
        // update the status text accordingly
        if (currentNote == null) {
            display.setText(LAB_NOCONTENT);
            status.setText(MSG_INST_UPDATE);
        } else {
            display.setText(note);
            if (isLogging) {
                applyNote();
            } else {
                status.setText(MSG_TOBE_APPLIED);
            }
        }
    }

    private void applyNote() {
        FastEventManager.logMessage(MSG_TYPE_COMMENT, "\"" + currentNote + "\"");
        status.setText(MSG_HAS_APPLIED);
    }

    /**
     * the callback method that FastEventManager notifies the start of logging.
     */
    public void startLogging() {
        applyButton.setText(LAB_WRITE);
        isLogging = true;
        if (currentNote != null) {
            applyNote();
        }
    }

    /**
     * the callback method that FastEventManager notifies the end of logging.
     */
    public void endLogging() {
        isLogging = false;
        applyButton.setText(LAB_SET);
        if (currentNote == null) {
            status.setText(MSG_INST_UPDATE);
        } else {
            status.setText(MSG_TOBE_APPLIED);
        }
    }
}

