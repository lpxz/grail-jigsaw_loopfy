package org.w3c.www.protocol.http;

/**
 * The HttpServer interface.
 * This interface is used to control the communication between the HttpManager
 * and the HttpServer on one side, and between the HttpServer and the
 * HttpConnection on the other side.
 * <p>The implementation of the Connection part of the interface is optional
 * and should be provided only if your server instance uses the connection
 * concept.
 */
public abstract class HttpServer {

    protected HttpServerState state = null;

    /**
     * Get this servers' protocol.
     * @return A String encoding the protocol used to dialog with the target
     * server.
     */
    public abstract String getProtocol();

    /**
     * Get the manager's state for that server.
     * @return The manager state.
     */
    protected final HttpServerState getState() {
        return state;
    }

    /**
     * Get this server's major version number.
     * @return The server's major number version, or <strong>-1</strong>
     * if still unknown.
     */
    public abstract short getMajorVersion();

    /**
     * Get this server's minor version number.
     * @return The server's minor number version, or <strong>-1</strong>
     * if still unknown.
     */
    public abstract short getMinorVersion();

    /**
     * HTTP manager interface - Handle this request in sync mode.
     * @param request The request this server should run.
     * @return A Reply instance, containing the target server's reply.
     * @exception HttpException If anything failed during request processing.
     */
    public abstract Reply runRequest(Request request) throws HttpException;

    /**
     * Interrupt the given request (that was launched by that server).
     * @param request The request to interrupt.
     */
    protected abstract void interruptRequest(Request request);

    /**
     * Set the new timeout for this server
     * @param timeout The timeout value in milliseconds
     */
    protected abstract void setTimeout(int timeout);

    /**
     * Set the new connection timeout for this server
     * @param timeout The timeout value in milliseconds
     */
    protected abstract void setConnTimeout(int conn_timeout);

    /**
     * Initialize this server instance for the given target location.
     * @param manager The central HTTP protocol manager.
     * @param state The manager's state for that server.
     * @param host The target server's FQDN.
     * @param port The target server's port number.
     * @param timeout The timeout in millisecond for the sockets
     * @exception HttpException If host coulnd't be resolved.
     */
    public abstract void initialize(HttpManager manager, HttpServerState state, String host, int port, int timeout) throws HttpException;

    /**
     * Initialize this server instance for the given target location.
     * @param manager The central HTTP protocol manager.
     * @param state The manager's state for that server.
     * @param host The target server's FQDN.
     * @param port The target server's port number.
     * @param timeout The timeout in millisecond for the sockets
     * @param timeout The connection timeout in millisecond for the sockets
     * @exception HttpException If host coulnd't be resolved.
     */
    public abstract void initialize(HttpManager manager, HttpServerState state, String host, int port, int timeout, int connect_timeout) throws HttpException;
}
