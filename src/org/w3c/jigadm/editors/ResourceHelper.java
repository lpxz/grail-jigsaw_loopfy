package org.w3c.jigadm.editors;

import java.awt.Component;
import java.util.EventObject;
import java.util.Properties;
import java.util.Vector;
import org.w3c.jigsaw.admin.RemoteResource;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.widgets.MessagePopup;
import org.w3c.jigadm.RemoteResourceWrapper;
import org.w3c.jigadm.events.ResourceChangeEvent;
import org.w3c.jigadm.events.ResourceListener;

public abstract class ResourceHelper implements ResourceHelperInterface {

    protected Vector rls = null;

    public abstract String getTitle();

    public abstract Component getComponent();

    protected void errorPopup(String name, Exception ex) {
        (new MessagePopup(name + " : " + ex.getMessage())).show();
    }

    protected void msgPopup(String name) {
        (new MessagePopup(name)).show();
    }

    public synchronized void addResourceListener(ResourceListener rl) {
        if (rls == null) rls = new Vector(2);
        rls.addElement(rl);
    }

    public RemoteResource getValue() {
        return null;
    }

    public synchronized void removeResourceListener(ResourceListener rl) {
        if (rls != null) rls.removeElement(rl);
    }

    protected void processEvent(EventObject eo) {
        Vector rls = null;
        ResourceListener rl;
        synchronized (this) {
            if ((this.rls != null) && (eo instanceof ResourceChangeEvent)) {
                rls = (Vector) this.rls.clone();
            } else {
                return;
            }
        }
        edu.hkust.clap.monitor.Monitor.loopBegin(328);
for (int i = 0; i < rls.size(); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(328);
{
            rl = (ResourceListener) rls.elementAt(i);
            rl.resourceChanged((ResourceChangeEvent) eo);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(328);

    }
}
