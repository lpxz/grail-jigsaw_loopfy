package org.w3c.www.webdav.xml;

import org.w3c.dom.Element;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class DAVLockType extends DAVNode {

    public static final short UNDEFINED = 0;

    public static final short WRITE = 1;

    public short getType() {
        if (getDAVNode(WRITE_NODE) != null) {
            return WRITE;
        } else {
            return UNDEFINED;
        }
    }

    DAVLockType(Element element) {
        super(element);
    }
}
