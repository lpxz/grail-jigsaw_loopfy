package org.w3c.jigedit.filters;

import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.FileResource;
import org.w3c.jigsaw.http.Request;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class DeletedEntry extends PutedEntry {

    private boolean confirmed = false;

    public void confirm() {
        confirmed = true;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    protected String getKey() {
        return getURL();
    }

    static PutedEntry makeEntry(Request request) {
        ResourceReference rr = request.getTargetResource();
        Resource r = null;
        if (rr != null) {
            try {
                r = rr.lock();
                DeletedEntry e = new DeletedEntry();
                e.setValue(ATTR_URL, request.getURL().toExternalForm());
                if (r instanceof FileResource) e.setValue(ATTR_FILENAME, ((FileResource) r).getFile().getAbsolutePath());
                e.update(request);
                return e;
            } catch (InvalidResourceException ex) {
                return null;
            } finally {
                rr.unlock();
            }
        }
        return null;
    }
}
