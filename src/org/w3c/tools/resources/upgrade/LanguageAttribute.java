package org.w3c.tools.resources.upgrade;

public class LanguageAttribute extends StringAttribute {

    public LanguageAttribute(String name, String def, Integer flags) {
        super(name, def, flags);
        this.type = "java.lang.String";
    }
}
