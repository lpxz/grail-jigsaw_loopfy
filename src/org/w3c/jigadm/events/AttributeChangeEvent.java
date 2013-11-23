package org.w3c.jigadm.events;

import java.beans.PropertyChangeEvent;
import org.w3c.jigadm.RemoteResourceWrapper;

public class AttributeChangeEvent extends PropertyChangeEvent {

    /**
     * Creates an AttributeChangeEvent
     * this correspond to a change in one Attribute of one resource
     * @param source The resource whose attribute has changed.
     * @param name The name of the attribute that has changed.
     * @param oldvalue The old attribute value.
     * @param newvalue The new attribuyte value.
     */
    public AttributeChangeEvent(Object source, String name, Object oldvalue, Object newvalue) {
        super(source, name, oldvalue, newvalue);
    }
}
