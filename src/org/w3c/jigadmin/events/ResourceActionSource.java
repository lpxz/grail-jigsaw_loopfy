package org.w3c.jigadmin.events;

/**
 * Object that implements this interface cand fire ResourceActionEvent.
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public interface ResourceActionSource {

    /**
     * Add a ResourceActionListener.
     * @param listener the ResourceActionListener to add
     */
    public void addResourceActionListener(ResourceActionListener listener);

    /**
     * Remove a ResourceActionListener.
     * @param listener the ResourceActionListener to remove
     */
    public void removeResourceActionListener(ResourceActionListener listener);
}
