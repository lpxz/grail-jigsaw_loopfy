package org.w3c.jigadmin.widgets;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;

/**
 * A Dialog that handles windowClosing event.
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public abstract class ClosableDialog extends JDialog {

    /**
     * Our internal WindowAdapter
     */
    WindowAdapter wl = new WindowAdapter() {

        public void windowClosing(WindowEvent e) {
            if (e.getWindow() == ClosableDialog.this) close();
        }
    };

    /**
     * The dialog is about to be closed
     */
    protected abstract void close();

    /**
     * Constructor
     */
    public ClosableDialog() {
        super();
        build();
    }

    /**
     * Constructor
     * @param frame the frame from which the dialog is displayed
     * @param title the String to display in the dialog's title bar
     * @param modal true for a modal dialog, false for one that allows 
     * others windows to be active at the same time
     */
    public ClosableDialog(Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        build();
    }

    private void build() {
        addWindowListener(wl);
    }
}
