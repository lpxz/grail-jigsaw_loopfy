package org.w3c.jigadm.editors;

import java.awt.Component;
import java.util.EventObject;
import java.util.Properties;
import java.util.Vector;
import org.w3c.jigsaw.admin.RemoteResource;
import org.w3c.tools.resources.Attribute;
import org.w3c.jigadm.events.AttributeChangeEvent;
import org.w3c.jigadm.events.AttributeListener;

public abstract class AttributeEditor implements AttributeEditorInterface {

    protected Vector als = null;

    public synchronized void addAttributeListener(AttributeListener al) {
        if (als == null) als = new Vector(2);
        als.addElement(al);
    }

    public synchronized void removeAttributeListener(AttributeListener al) {
        if (als != null) als.removeElement(al);
    }

    protected void processEvent(EventObject eo) {
        Vector als = null;
        AttributeListener al;
        synchronized (this) {
            if ((this.als != null) && (eo instanceof AttributeChangeEvent)) {
                als = (Vector) this.als.clone();
            } else {
                return;
            }
        }
        edu.hkust.clap.monitor.Monitor.loopBegin(361);
for (int i = 0; i < als.size(); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(361);
{
            al = (AttributeListener) als.elementAt(i);
            al.attributeChanged((AttributeChangeEvent) eo);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(361);

    }

    /**
     * get the Component created by the editor.
     * @return a Component
     */
    public abstract Component getComponent();
}
