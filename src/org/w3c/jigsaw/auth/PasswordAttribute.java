package org.w3c.jigsaw.auth;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.StringAttribute;

public class PasswordAttribute extends StringAttribute {

    public PasswordAttribute(String name, String password, int flags) {
        super(name, password, flags);
        this.type = "java.lang.String".intern();
    }

    public PasswordAttribute() {
        super();
    }
}
