package org.w3c.jigadmin.widgets;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JTabbedPane;

/**
 * A JTabbedPane for dnd (work arround for a bug in swing)
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class DnDTabbedPane extends JTabbedPane {

    public DnDTabbedPane() {
        super();
    }

    public Component findComponentAt(int x, int y) {
        if (!contains(x, y)) {
            return null;
        }
        int ncomponents = getComponentCount();
        edu.hkust.clap.monitor.Monitor.loopBegin(476);
for (int i = 0; i < ncomponents; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(476);
{
            Component comp = getComponentAt(i);
            if (comp != null) {
                if (comp instanceof Container) {
                    if (comp.isVisible()) {
                        comp = ((Container) comp).findComponentAt(x - comp.getX(), y - comp.getY());
                    }
                } else {
                    comp = comp.getComponentAt(x - comp.getX(), y - comp.getY());
                }
                if (comp != null && comp.isVisible()) {
                    return comp;
                }
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(476);

        return this;
    }
}
