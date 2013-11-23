package org.w3c.jigsaw.https;

import java.net.URL;
import org.w3c.jigsaw.daemon.ServerHandlerInitException;
import org.w3c.jigsaw.http.httpd;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.ProtocolException;

/**
 * @author Thomas Kopp, Dialogika GmbH
 * @version 1.0.2, 30 January 2001
 * 
 * This class extends a Jigsaw httpd daemon
 * in order to supply a Jigsaw httpd daemon with https support
 * in accordance with the JSSE API.
 */
public class httpsd extends httpd {

    /**
     * reference to the TLS support adapter of this daemon
     */
    private SSLAdapter adapter = null;

    /**
     * constructor of this daemon
     */
    public httpsd() {
        super();
        adapter = new SSLAdapter(this);
    }

    /**
     * clone method of this daemon
     */
    protected Object clone() throws CloneNotSupportedException {
        httpsd daemon = (httpsd) (super.clone());
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
