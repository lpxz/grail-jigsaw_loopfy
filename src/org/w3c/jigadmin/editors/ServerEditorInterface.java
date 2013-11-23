package org.w3c.jigadmin.editors;

import org.w3c.jigadmin.RemoteResourceWrapper;

/**
 * Interface for server editors.
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public interface ServerEditorInterface extends EditorInterface {

    /**
     * Load or reload the server configuration.
     * @param server the new server wrapper
     */
    public void setServer(RemoteResourceWrapper server);
}
