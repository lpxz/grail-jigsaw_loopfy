package org.w3c.tools.resources.event;

import java.util.EventObject;

public class ResourceEvent extends EventObject {

    protected int id = -1;

    public int getID() {
        return id;
    }

    public ResourceEvent(Object source, int id) {
        super(source);
        this.id = id;
    }
}
