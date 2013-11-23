package org.w3c.jigadmin.widgets;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

/**
 * A Frame that handles windowClosing event.
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public abstract class ClosableFrame extends JFrame {

    /**
     * Our internal WindowAdapter
     */
    WindowAdapter wl = new WindowAdapter() {

        public void windowClosing(WindowEvent e) {
            if (e.getWindow() == ClosableFrame.this) close();
        }
    };

    /**
     * The dialog is about to be closed
     */
    protected abstract void close();

    /**
     * Constructor
     */
    public ClosableFrame() {
        super();
        build();
    }

    /**
     * Constructor
     * @param title The frame title.
     */
    public ClosableFrame(String title) {
        super(title);
        build();
    }

    private void build() {
        addWindowListener(wl);
    }
}
