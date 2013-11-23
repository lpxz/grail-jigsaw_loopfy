package org.w3c.tools.resources;

public class FrameArrayAttribute extends Attribute {

    public boolean checkValue(Object value) {
        return value instanceof ResourceFrame[];
    }

    public String stringify(Object value) {
        throw new RuntimeException("Can't stringify FrameArrayAttribute");
    }

    public FrameArrayAttribute(String name, ResourceFrame def[], int flags) {
        super(name, def, flags);
        this.type = "[Lorg.w3c.tools.resources.ResourceFrame;".intern();
    }

    public FrameArrayAttribute() {
        super();
        this.type = "[Lorg.w3c.tools.resources.ResourceFrame;".intern();
    }
}
