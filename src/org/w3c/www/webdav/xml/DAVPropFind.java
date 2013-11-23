package org.w3c.www.webdav.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class DAVPropFind extends DAVNode {

    DAVProperties dp = null;

    public boolean findPropNames() {
        return (getDAVNode(PROPNAME_NODE) != null);
    }

    public boolean findAllProps() {
        return (getDAVNode(ALLPROP_NODE) != null);
    }

    public DAVProperties getProperties() {
        if (dp == null) {
            Element e = (Element) getDAVNode(PROP_NODE);
            if (e != null) {
                dp = new DAVProperties(e);
            }
        }
        return dp;
    }

    DAVPropFind(Element element) {
        super(element);
    }
}
