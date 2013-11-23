package org.w3c.jigadmin.gui.slist;

import java.util.Hashtable;
import org.w3c.jigadmin.RemoteResourceWrapper;
import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;
import org.w3c.tools.sorter.Sorter;

/**
 * The default ServerList model.
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class ServerListModel implements ServerListModelInterface {

    RemoteResourceWrapper root = null;

    String servers[] = null;

    Hashtable serversrrw = null;

    /**
     * Returns a array of the server names.
     * @return an array of String
     */
    public String[] getServers() {
        return servers;
    }

    /**
     * Get the server with the given name.
     * @param name the server name
     * @return The RemoteResourceWrapper of the server.
     */
    public RemoteResourceWrapper getServer(String name) {
        return (RemoteResourceWrapper) serversrrw.get(name);
    }

    /**
     * Build the ServerListModel.
     * @exception RemoteAccessException if a remote error occurs.
     */
    protected void build() throws RemoteAccessException {
        RemoteResource rr = root.getResource();
        boolean ic = false;
        try {
            ic = rr.isContainer();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        String names[] = rr.enumerateResourceIdentifiers();
        Sorter.sortStringArray(names, true);
        servers = new String[names.length - 1];
        serversrrw = new Hashtable(servers.length);
        int j = 0;
        servers[j++] = ADMIN_SERVER_NAME;
        serversrrw.put(ADMIN_SERVER_NAME, root);
        edu.hkust.clap.monitor.Monitor.loopBegin(978);
for (int i = 0; i < names.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(978);
{
            if ((!names[i].equals("control")) && (!names[i].equals("realms"))) {
                servers[j++] = names[i];
                RemoteResourceWrapper server = root.getChildResource(names[i]);
                serversrrw.put(names[i], server);
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(978);

    }

    /**
     * Constructor.
     * @param root the root RemoteResourceWrapper.
     * @exception RemoteAccessException if a remote error occurs.
     */
    public ServerListModel(RemoteResourceWrapper root) throws RemoteAccessException {
        this.root = root;
        build();
    }
}
