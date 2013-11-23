package org.w3c.tools.forms;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;

public class FormPanel extends Panel {

    /**
     * Our associated form manager.
     */
    FormManager manager = null;

    /**
     * Our layout manager.
     */
    GridBagLayout gb = null;

    /**
     * Field's title constraints.
     */
    GridBagConstraints ct = null;

    /**
     * Field's editor constraints.
     */
    GridBagConstraints cv = null;

    /**
     * Add a field editor.
     * @param title The title for the field.
     * @param editor Its editor component.
     */
    protected void addField(String title, Component editor) {
        Label label = new Label(title, Label.LEFT);
        gb.setConstraints(label, ct);
        add(label);
        gb.setConstraints(editor, cv);
        add(editor);
    }

    /**
     * Some insets for the form panel. 
     */
    public Insets insets() {
        return new Insets(5, 5, 5, 5);
    }

    /**
     * Darw a rectangle around the form panel.
     */
    public void paint(Graphics g) {
        Dimension d = size();
        g.drawRect(1, 1, d.width - 3, d.height - 3);
    }

    /**
     * Create a new form panel for the given form manager.
     */
    FormPanel(FormManager manager) {
        super();
        gb = new GridBagLayout();
        setLayout(gb);
        ct = new GridBagConstraints();
        ct.gridx = GridBagConstraints.RELATIVE;
        ct.anchor = GridBagConstraints.EAST;
        ct.weighty = 1.0;
        cv = new GridBagConstraints();
        cv.gridx = GridBagConstraints.RELATIVE;
        cv.gridwidth = GridBagConstraints.REMAINDER;
        cv.fill = GridBagConstraints.HORIZONTAL;
        cv.anchor = GridBagConstraints.WEST;
        cv.weightx = 1.0;
        cv.weighty = 1.0;
    }
}
