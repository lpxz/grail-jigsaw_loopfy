package org.w3c.tools.resources;

/**
 * The generic description of an StringArrayAttribute.
 */
public class StringArrayAttribute extends ArrayAttribute {

    /**
     * Is the given object a valid StringArrayAttribute value ?
     * @param obj The object to test.
     * @return A boolean <strong>true</strong> if okay.
     */
    public boolean checkValue(Object obj) {
        if (obj != null) {
            return (obj instanceof String[]);
        }
        return true;
    }

    public String[] pickle(Object array) {
        return (String[]) array;
    }

    public Object unpickle(String array[]) {
        if (array.length < 1) return null;
        return array;
    }

    public StringArrayAttribute(String name, String def[], int flags) {
        super(name, def, flags);
        this.type = "[Ljava.lang.String;".intern();
    }

    public StringArrayAttribute() {
        super();
        this.type = "[Ljava.lang.String;".intern();
    }
}
