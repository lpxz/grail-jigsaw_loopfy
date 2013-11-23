package org.w3c.jigsaw.resources;

import org.w3c.tools.resources.FramedResource;

public class PasswordEditor extends FramedResource {

    public void initialize(Object values[]) {
        super.initialize(values);
        try {
            registerFrameIfNone("org.w3c.jigsaw.resources.PasswordEditorFrame", "passwd-frame");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
