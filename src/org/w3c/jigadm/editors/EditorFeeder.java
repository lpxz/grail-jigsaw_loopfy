package org.w3c.jigadm.editors;

import org.w3c.jigadm.RemoteResourceWrapper;
import java.util.Properties;

public interface EditorFeeder {

    /**
     * Compute the possible items for a StringArrayEditor.
     * @return The possible items for the selection.
     */
    public String[] getDefaultItems();

    public void initialize(RemoteResourceWrapper rrw, Properties p);
}
