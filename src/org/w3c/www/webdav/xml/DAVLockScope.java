package org.w3c.www.webdav.xml;

import org.w3c.dom.Element;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class DAVLockScope extends DAVNode {

    public static final short UNDEFINED = 0;

    public static final short EXCLUSIVE = 1;

    public static final short SHARED = 1;

    public short getLockScope() {
        if (getDAVNode(EXCLUSIVE_NODE) != null) {
            return EXCLUSIVE;
        } else if (getDAVNode(SHARED_NODE) != null) {
            return SHARED;
        } else {
            return UNDEFINED;
        }
    }

    DAVLockScope(Element element) {
        super(element);
    }
}
