package org.w3c.jigsaw.webdavs;

import java.net.URL;
import org.w3c.jigsaw.daemon.ServerHandlerInitException;
import org.w3c.jigsaw.https.SSLAdapter;
import org.w3c.jigsaw.webdav.webdavd;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.ProtocolException;

/**
 * @author Thomas Kopp, Dialogika GmbH
 * @version 1.0.2, 30 January 2001
 * 
 * This class extends a Jigsaw webdav daemon
 * in order to supply a Jigsaw webdav daemon with https support
 * in accordance with the JSSE API.
 */
public class webdavsd extends webdavd {

    /**
     * reference to the TLS support adapter of this daemon
     */
    private SSLAdapter adapter = null;

    /**
     * constructor of this daemon
     */
    public webdavsd() {
        super();
        adapter = new SSLAdapter(this);
    }

    /**
     * clone method of this daemon
     */
    protected Object clone() throws CloneNotSupportedException {
        webdavsd daemon = (webdavsd) (super.clone());
        daemon.adapter = new SSLAdapter(daemon);
        return daemon;
    }

    /**
     * method for initializing the properties of this daemon
     * @exception ServerHandlerInitException if initialization fails
     */
    protected void initializeProperties() throws ServerHandlerInitException {
        super.initializeProperties();
        adapter.initializeProperties();
    }

    /**
     * method for supplying a reply interface of a request
     * @param req current request to be handled
     * @return reply for a current request
     */
    public ReplyInterface perform(RequestInterface req) throws ProtocolException, ResourceException {
        adapter.perform(req);
        return super.perform(req);
    }

    /**
     * method for supplying the uri of this daemon
     * @return uri of this daemon
     */
    public URL getURL() {
        return adapter.getURL();
    }
}
