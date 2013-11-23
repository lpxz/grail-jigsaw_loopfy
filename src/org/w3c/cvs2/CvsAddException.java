package org.w3c.cvs2;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class CvsAddException extends CvsException {

    CvsAddException(String filename) {
        super(filename);
    }
}
