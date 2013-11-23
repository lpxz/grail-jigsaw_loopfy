package org.w3c.jigadm.gui;

import org.w3c.jigadm.RemoteResourceWrapper;
import org.w3c.jigadm.events.ResourceChangeEvent;
import org.w3c.jigadm.events.ResourceListener;

public class ResourceEditorListener implements ResourceListener {

    ServerBrowser sb = null;

    public void resourceChanged(ResourceChangeEvent e) {
        if (e.getNewValue() == null) {
            if (e.getOldValue() != null) {
                sb.removeNode((RemoteResourceWrapper) e.getOldValue());
            }
        }
    }

    public ResourceEditorListener(ServerBrowser sb) {
        this.sb = sb;
    }
}
