package org.w3c.jigadmin.gui.slist;

import java.util.EventListener;
import org.w3c.jigadmin.RemoteResourceWrapper;

/**
 * The interface for ServerList listeners
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public interface ServerListListener extends EventListener {

    /**
     * A server has been selected.
     * @param name the server name.
     * @param rrw the server RemoteResourceWrapper
     */
    public void serverSelected(String name, RemoteResourceWrapper rrw);
}
