package org.w3c.jigsaw.pics;

import java.net.URL;

public interface LabelInterface {

    /**
     * Dump this label in the given buffer.
     * @param buffer The buffer to dump the label to.
     * @param format The PICS format in which the label was requested.
     */
    public void dump(StringBuffer buffer, int format);
}
