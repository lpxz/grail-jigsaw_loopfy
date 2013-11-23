package org.w3c.jigsaw.webdav;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class DAVPropertyException extends Exception {

    private Object reason = null;

    public Object getReason() {
        return reason;
    }

    public DAVPropertyException(String msg, Object reason) {
        super(msg);
        this.reason = reason;
    }
}
