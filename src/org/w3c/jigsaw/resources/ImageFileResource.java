package org.w3c.jigsaw.resources;

import org.w3c.tools.resources.FileResource;
import java.io.InputStream;
import java.io.IOException;

public abstract class ImageFileResource extends FileResource {

    /**
     * Save the given stream as the underlying image comment content.
     * This method preserve the old file version in a <code>~</code> file.
     * @param in The input stream to use as the resource entity.
     * @return A boolean, <strong>true</strong> if the resource was just
     * created, <strong>false</strong> otherwise.
     * @exception IOException If dumping the content failed.
     */
    public abstract boolean newMetadataContent(InputStream in) throws IOException;
}
