package org.w3c.jigadmin.widgets;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.BorderFactory;

/**
 * ListEditor display a JList and an "Edit" button.
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */
public abstract class ListEditor extends JPanel {

    protected JList list = null;

    protected JButton editButton = null;

    ActionListener al = new ActionListener() {

        public void actionPerformed(ActionEvent evt) {
            String command = evt.getActionCommand();
            if (command.equals("edit")) {
                edit();
            }
        }
    };

    protected abstract void edit();

    public ListEditor() {
        this(5, true);
    }

    public ListEditor(int nbVisible, boolean multiple) {
        super(new BorderLayout());
        editButton = new JButton("Edit");
        editButton.setActionCommand("edit");
        editButton.addActionListener(al);
        list = new JList();
        list.setVisibleRowCount(nbVisible);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane spane = new JScrollPane(list);
        spane.setBorder(BorderFactory.createLoweredBevelBorder());
        add(spane, BorderLayout.CENTER);
        add(editButton, BorderLayout.EAST);
    }
}
