package org.w3c.jigsaw.resources;

import org.w3c.tools.resources.FramedResource;

public class DirectoryLister extends FramedResource {

    public void initialize(Object values[]) {
        super.initialize(values);
        try {
            registerFrameIfNone("org.w3c.jigsaw.resources.DirectoryListerFrame", "lister-frame");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
