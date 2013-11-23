package org.w3c.tools.resources;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class DuplicateNameException extends RuntimeException {

    public DuplicateNameException(String name) {
        super(name);
    }

    public String getName() {
        return getMessage();
    }
}
