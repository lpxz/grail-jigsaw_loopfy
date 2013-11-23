package org.w3c.www.webdav.xml;

import java.util.Vector;
import org.w3c.dom.Element;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class DAVLink extends DAVNode {

    public String[] getSources() {
        return getMultipleTextChildValue(SRC_NODE);
    }

    public String[] getDestinations() {
        return getMultipleTextChildValue(DST_NODE);
    }

    DAVLink(Element element) {
        super(element);
    }
}
