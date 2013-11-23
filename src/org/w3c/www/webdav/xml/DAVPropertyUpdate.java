package org.w3c.www.webdav.xml;

import java.util.Vector;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.www.webdav.WEBDAV;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class DAVPropertyUpdate extends DAVNode {

    public DAVPropAction[] getActions() {
        Vector v = new Vector();
        Node current = element.getFirstChild();
        edu.hkust.clap.monitor.Monitor.loopBegin(972);
while (current != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(972);
{
            if ((current.getNodeType() == current.ELEMENT_NODE) && (current.getLocalName().equals(SET_NODE) || current.getLocalName().equals(REMOVE_NODE)) && (current.getNamespaceURI() != null) && (current.getNamespaceURI().equals(WEBDAV.NAMESPACE_URI))) {
                v.addElement(new DAVPropAction((Element) current));
            }
            current = current.getNextSibling();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(972);

        DAVPropAction dpa[] = new DAVPropAction[v.size()];
        v.copyInto(dpa);
        return dpa;
    }

    public void setActions(DAVPropAction actions[]) {
        int len = actions.length;
        edu.hkust.clap.monitor.Monitor.loopBegin(973);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(973);
{
            element.appendChild(actions[i].getNode());
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(973);

    }

    DAVPropertyUpdate(Element element) {
        super(element);
    }
}
