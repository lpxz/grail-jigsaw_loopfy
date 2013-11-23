package org.w3c.www.protocol.http;

import java.io.OutputStream;

public class ConnectedEvent extends RequestEvent {

    /**
     * The HTTP <code>CONTINUE</code> packet.
     */
    public OutputStream output = null;

    public ConnectedEvent(HttpServer s, Request req, OutputStream output) {
        super(s, req, EVT_CONNECTED);
        this.output = output;
    }
}
