package org.w3c.tools.resources;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public abstract class SimpleAttribute extends Attribute {

    /**
     * Unpickle an attribute from a string.
     * @param array the String.
     * @return a Object.
     */
    public abstract Object unpickle(String value);

    /**
     * Pickle an attribute into a String.
     * @param array the attribute
     * @return a String.
     */
    public abstract String pickle(Object obj);

    public String stringify(Object value) {
        return pickle(value);
    }

    public SimpleAttribute(String name, Object def, int flags) {
        super(name, def, flags);
    }

    public SimpleAttribute() {
        super();
    }
}
