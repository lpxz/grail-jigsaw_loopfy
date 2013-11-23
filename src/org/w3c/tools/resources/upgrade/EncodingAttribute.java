package org.w3c.tools.resources.upgrade;

public class EncodingAttribute extends StringAttribute {

    public EncodingAttribute(String name, String def, Integer flags) {
        super(name, def, flags);
        this.type = "java.lang.String";
    }
}
