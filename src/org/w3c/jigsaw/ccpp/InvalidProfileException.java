package org.w3c.jigsaw.ccpp;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class InvalidProfileException extends Exception {

    public InvalidProfileException(String ref) {
        super(ref);
    }
}
