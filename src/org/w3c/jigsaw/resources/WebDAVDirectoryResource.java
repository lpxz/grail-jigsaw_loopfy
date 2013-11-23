package org.w3c.jigsaw.resources;

public class WebDAVDirectoryResource extends DirectoryResource {

    public void initialize(Object values[]) {
        super.initialize(values);
        try {
            registerFrameIfNone("org.w3c.jigsaw.webdav.DAVFrame", "webdav-frame");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
