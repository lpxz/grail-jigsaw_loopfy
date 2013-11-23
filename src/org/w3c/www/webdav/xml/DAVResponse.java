package org.w3c.www.webdav.xml;

import java.util.Date;
import java.util.Vector;
import org.w3c.dom.Element;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class DAVResponse extends DAVNode {

    public String getHref() {
        return getTextChildValue(HREF_NODE);
    }

    public String[] getHrefs() {
        return getMultipleTextChildValue(HREF_NODE);
    }

    public String getStatus() {
        return getTextChildValue(STATUS_NODE);
    }

    public String getDescription() {
        return getTextChildValue(RESPONSEDESC_NODE);
    }

    public void setDescription(String descr) {
        addDAVNode(RESPONSEDESC_NODE, descr);
    }

    public DAVPropStat[] getPropStats() {
        Vector v = getDAVElementsByTagName(PROPSTAT_NODE);
        DAVPropStat dps[] = new DAVPropStat[v.size()];
        edu.hkust.clap.monitor.Monitor.loopBegin(1137);
for (int i = 0; i < v.size(); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1137);
{
            dps[i] = new DAVPropStat((Element) v.elementAt(i));
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1137);

        return dps;
    }

    DAVResponse(Element element) {
        super(element);
    }
}
