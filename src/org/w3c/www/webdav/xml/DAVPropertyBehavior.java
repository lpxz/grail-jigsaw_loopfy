package org.w3c.www.webdav.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class DAVPropertyBehavior extends DAVNode {

    public boolean omit() {
        return (getDAVNode(OMIT_NODE) != null);
    }

    public boolean keepaliveAll() {
        String star = getTextChildValue(KEEPALIVE_NODE).trim();
        return ((star != null) && (star.equals("*")));
    }

    public String[] getHrefs() {
        Node keepalive = getDAVNode(KEEPALIVE_NODE);
        if (keepalive != null) {
            return getMultipleTextChildValue(keepalive, HREF_NODE);
        }
        return null;
    }

    DAVPropertyBehavior(Element element) {
        super(element);
    }
}
