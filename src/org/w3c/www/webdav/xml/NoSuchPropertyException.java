package org.w3c.www.webdav.xml;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class NoSuchPropertyException extends Exception {

    public NoSuchPropertyException(String property) {
        super(property);
    }
}
