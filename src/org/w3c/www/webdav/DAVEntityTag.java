package org.w3c.www.webdav;

import org.w3c.www.http.HttpEntityTag;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class DAVEntityTag extends HttpEntityTag {

    boolean isnot = false;

    public boolean isNot() {
        return isnot;
    }

    public String toString() {
        if (isnot) {
            return "Not " + toExternalForm();
        } else {
            return toExternalForm();
        }
    }

    public DAVEntityTag(byte raw[], int off, int len, boolean isnot) {
        super();
        addBytes(raw, off, len);
        this.isnot = isnot;
    }
}
