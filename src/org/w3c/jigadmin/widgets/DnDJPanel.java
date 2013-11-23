package org.w3c.jigadmin.widgets;

import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;
import javax.swing.JPanel;

/**
 * A JPanel for dnd (work arround for a bug in swing)
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class DnDJPanel extends JPanel {

    public DnDJPanel() {
        super();
    }

    public DnDJPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
    }

    public DnDJPanel(LayoutManager layout) {
        super(layout);
    }

    public DnDJPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }

    public Component findComponentAt(int x, int y) {
        if (!contains(x, y)) {
            return null;
        }
        int ncomponents = getComponentCount();
        Component components[] = getComponents();
        edu.hkust.clap.monitor.Monitor.loopBegin(557);
for (int i = 0; i < ncomponents; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(557);
{
            Component comp = components[i];
            if (comp != null) {
                if (comp instanceof Container) {
                    if (comp.isVisible()) comp = ((Container) comp).findComponentAt(x - comp.getX(), y - comp.getY());
                } else {
                    comp = comp.getComponentAt(x - comp.getX(), y - comp.getY());
                }
                if (comp != null && comp.isVisible()) {
                    return comp;
                }
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(557);

        return this;
    }
}
