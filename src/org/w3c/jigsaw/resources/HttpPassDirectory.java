package org.w3c.jigsaw.resources;

public class HttpPassDirectory extends PassDirectory {

    public void initialize(Object values[]) {
        super.initialize(values);
        try {
            registerFrameIfNone("org.w3c.jigsaw.frames.HTTPFrame", "http-frame");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
