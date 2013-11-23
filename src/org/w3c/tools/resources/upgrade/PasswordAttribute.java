package org.w3c.tools.resources.upgrade;

public class PasswordAttribute extends StringAttribute {

    public PasswordAttribute(String name, String password, Integer flags) {
        super(name, password, flags);
        this.type = "java.lang.String";
    }
}
