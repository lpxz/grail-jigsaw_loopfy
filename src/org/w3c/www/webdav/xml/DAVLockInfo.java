package org.w3c.www.webdav.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class DAVLockInfo extends DAVLockEntry {

    public Node getOwner() {
        return getDAVNode(OWNER_NODE);
    }

    DAVLockInfo(Element element) {
        super(element);
    }
}
