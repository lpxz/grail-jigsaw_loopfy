package org.w3c.jigadm.events;

import java.util.EventListener;

public interface AttributeListener extends EventListener {

    /**
     * Invoked when the value of the Attribute has changed
     */
    public void attributeChanged(AttributeChangeEvent e);
}
