package org.w3c.tools.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public abstract class ClosableFrame extends Frame {

    class WindowCloser extends WindowAdapter {

        ClosableFrame frame = null;

        public void windowClosing(WindowEvent e) {
            if (e.getWindow() == frame) frame.close();
        }

        WindowCloser(ClosableFrame frame) {
            this.frame = frame;
        }
    }

    protected abstract void close();

    public ClosableFrame() {
        super();
        build();
    }

    public ClosableFrame(String title) {
        super(title);
        build();
    }

    private void build() {
        addWindowListener(new WindowCloser(this));
        setBackground(Color.lightGray);
    }
}
