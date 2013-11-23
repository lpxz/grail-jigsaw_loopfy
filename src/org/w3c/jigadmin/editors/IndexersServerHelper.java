package org.w3c.jigadmin.editors;

import javax.swing.JPanel;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.util.Properties;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import org.w3c.jigadmin.PropertyManager;
import org.w3c.jigadmin.RemoteResourceWrapper;
import org.w3c.jigadmin.widgets.DraggableList;
import org.w3c.tools.sorter.Sorter;

/**
 * The server helper dedicated to the indexers.
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class IndexersServerHelper extends SpaceServerHelper {

    /**
     * Return a new ResourceTreeBrowser.
     * @return a ResourceTreeBrowser instance
     */
    protected ResourceTreeBrowser getTreeBrowser() {
        return ResourceTreeBrowser.getResourceTreeBrowser(root, "Indexers");
    }

    /**
     * Get the Internal Frame containing frame & resource lists
     * @return A JInternalFrame instance
     */
    protected JInternalFrame getInternalFrame() {
        JInternalFrame fr = super.getInternalFrame();
        PropertyManager pm = PropertyManager.getPropertyManager();
        Hashtable indexers = pm.getIndexers();
        Enumeration e = (Sorter.sortStringEnumeration(indexers.keys())).elements();
        Vector cells = new Vector(10);
        edu.hkust.clap.monitor.Monitor.loopBegin(1139);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(1139);
{
            String name = (String) e.nextElement();
            ResourceCell cell = new ResourceCell(name, (String) indexers.get(name));
            cells.addElement(cell);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1139);

        DraggableList list = getResourceList(cells);
        JScrollPane scroll = new JScrollPane(list);
        tabbedPane.addTab("Indexers", null, scroll, "Indexers available");
        tabbedPane.setSelectedComponent(scroll);
        return fr;
    }

    /**
     * Constructor.
     */
    public IndexersServerHelper() {
        super();
    }
}
