package org.w3c.jigsaw.resources;

public class HttpDirectoryResource extends DirectoryResource {

    public void initialize(Object values[]) {
        super.initialize(values);
        try {
            registerFrameIfNone("org.w3c.jigsaw.frames.HTTPFrame", "http-frame");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
