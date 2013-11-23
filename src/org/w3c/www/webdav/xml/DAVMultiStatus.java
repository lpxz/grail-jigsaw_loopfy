package org.w3c.www.webdav.xml;

import java.util.Vector;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class DAVMultiStatus extends DAVNode {

    public DAVResponse[] getDAVResponses() {
        Vector list = getDAVElementsByTagName(RESPONSE_NODE);
        int len = list.size();
        DAVResponse responses[] = new DAVResponse[len];
        edu.hkust.clap.monitor.Monitor.loopBegin(629);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(629);
{
            responses[i] = new DAVResponse((Element) list.elementAt(i));
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(629);

        return responses;
    }

    public String getResponseDescription() {
        Node node = getDAVNode(RESPONSEDESC_NODE);
        if (node != null) {
            return getTextChildValue(node);
        }
        return null;
    }

    DAVMultiStatus(Element element) {
        super(element);
    }
}
