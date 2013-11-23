package org.w3c.jigsaw.resources;

import org.w3c.tools.resources.FileResource;
import org.w3c.tools.resources.FramedResource;

public class HttpFileResource extends FileResource {

    public void initialize(Object values[]) {
        super.initialize(values);
        try {
            registerFrameIfNone("org.w3c.jigsaw.frames.HTTPFrame", "http-frame".intern());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
