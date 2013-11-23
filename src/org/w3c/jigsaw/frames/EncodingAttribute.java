package org.w3c.jigsaw.frames;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.StringAttribute;

public class EncodingAttribute extends StringAttribute {

    public EncodingAttribute(String name, String def, int flags) {
        super(name, def, flags);
        this.type = "java.lang.String".intern();
    }

    public EncodingAttribute() {
        super();
    }
}
