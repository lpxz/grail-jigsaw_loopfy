package org.w3c.jigadmin.editors;

import java.util.Properties;
import java.awt.Component;
import org.w3c.jigadmin.RemoteResourceWrapper;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public interface EditorInterface {

    /**
     * Initialize this editor.
     * @param name the editor name
     * @param rrw the RemoteResourceWrapper wrapping the editor node.
     * @param p the editor properties
     */
    public void initialize(String name, RemoteResourceWrapper rrw, Properties p);

    /**
     * Get the Component.
     * @return a Component instance
     */
    public Component getComponent();
}
