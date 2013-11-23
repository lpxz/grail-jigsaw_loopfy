package org.w3c.jigsaw.https.socket;

import java.net.Socket;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import org.w3c.jigsaw.http.httpd;
import org.w3c.jigsaw.http.socket.SocketClient;
import org.w3c.jigsaw.http.socket.SocketClientFactory;
import org.w3c.jigsaw.http.socket.SocketClientState;

/**
 * @author Thomas Kopp, Dialogika GmbH
 * @version 1.1, 6 February 2004
 * 
 * This class extends a Jigsaw SocketClient designed for the 
 * http protocol
 * in order to supply a SocketClient for the https protocol including 
 * client authentication in accordance with the JSSE API.
 */
public class SSLSocketClient extends SocketClient {

    /**
     * Creates an empty client.
     *
     * @param server  the target http(s( daemon
     * @param pool  the client factory in use
     * @param state  the socket cliente state in use
     */
    protected SSLSocketClient(httpd server, SocketClientFactory pool, SocketClientState state) {
        super(server, pool, state);
    }

    /**
     * Supplies the ssl session associated with the underlying socket.
     *
     * @return  the associated ssl session or null
     */
    public SSLSession getSession() {
        if (socket instanceof SSLSocket) {
            return ((SSLSocket) socket).getSession();
        }
        return null;
    }
}
