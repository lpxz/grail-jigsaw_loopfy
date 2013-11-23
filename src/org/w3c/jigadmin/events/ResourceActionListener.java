package org.w3c.jigadmin.events;

/**
 * The ResourceAction listener class.
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public interface ResourceActionListener {

    /**
     * A resource action occured.
     * @param e the ResourceActionEvent
     */
    public void resourceActionPerformed(ResourceActionEvent e);
}
