package org.w3c.tools.resources;

/**
 * The generic description of a FilenameAttribute.
 * A file name is a String, augmented with the fact that it should be a valid 
 * file name.
 */
public class FilenameAttribute extends StringAttribute {

    public FilenameAttribute(String name, Object def, int flags) {
        super(name, (String) def, flags);
        this.type = "java.lang.String".intern();
    }

    public FilenameAttribute() {
        super();
    }
}
