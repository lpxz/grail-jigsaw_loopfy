package org.w3c.jigadm.events;

import java.util.EventListener;

public interface ResourceListener extends EventListener {

    /**
     * Invoked when the value of the Attribute has changed
     */
    public void resourceChanged(ResourceChangeEvent e);
}
