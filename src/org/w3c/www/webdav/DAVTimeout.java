package org.w3c.www.webdav;

import org.w3c.www.http.BasicValue;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class DAVTimeout extends BasicValue {

    protected void parse() {
    }

    protected void updateByteValue() {
    }

    public Object getValue() {
        return this;
    }

    public DAVTimeout() {
        this.isValid = false;
    }
}
