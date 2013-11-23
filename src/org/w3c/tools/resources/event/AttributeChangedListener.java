package org.w3c.tools.resources.event;

import java.util.EventListener;

public interface AttributeChangedListener extends EventListener {

    /**
   * Gets called when a property changes.
   * @param evt The AttributeChangeEvent describing the change.
   */
    public void attributeChanged(AttributeChangedEvent evt);
}
