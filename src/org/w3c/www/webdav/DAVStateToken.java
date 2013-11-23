package org.w3c.www.webdav;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class DAVStateToken {

    boolean isnot = false;

    String token = null;

    public boolean isNot() {
        return isnot;
    }

    public String getStateToken() {
        return token;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        if (isnot) {
            buffer.append("Not ");
        }
        buffer.append("<").append(token).append(">");
        return buffer.toString();
    }

    public DAVStateToken(String token, boolean isnot) {
        this.token = token;
        this.isnot = isnot;
    }
}
