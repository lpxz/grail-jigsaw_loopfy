package org.w3c.jigadmin.editors;

import javax.swing.plaf.basic.BasicTreeUI;
import java.awt.event.MouseEvent;

/**
 * The UI of the ResourceTreeBrowser
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class ResourceTreeUI extends BasicTreeUI {

    /**
     * return always false to disable expands on double click.
     */
    protected boolean isToggleEvent(MouseEvent event) {
        return false;
    }
}
