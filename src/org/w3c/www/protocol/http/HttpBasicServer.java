package org.w3c.www.protocol.http;

import java.util.Date;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.w3c.www.mime.MimeHeaderHolder;
import org.w3c.www.mime.MimeParser;
import org.w3c.www.mime.MimeParserException;
import org.w3c.www.mime.MimeParserFactory;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;
import org.w3c.www.http.HttpStreamObserver;
import org.w3c.www.http.ChunkedOutputStream;

/**
 * The basic server class, to run requests.
 * A server instance (ie an object conforming to the <code>HttpServer</code>
 * interface is resposnsible for running all requests to a given host.
 * <p>To do so, it manages a connnnection pool (ie a set of available
 * connections) which it negotiates with the global HTTP manager. It keeps
 * track of the connections it creates in order to serialize requests
 * to the target server (when possible). In order to avoid deadlock due
 * to implicit ordering of requests, a server that manages persistent
 * (ie > 1.0) connections will always be able to open at least two
 * connections to the same target.
 * <p>Connections are kept track of by the server instance, which maintains
 * at all point in time a list of <em>idle</em> connections. The way this is
 * done may seem quite tricky, but beleive me, it's the best way I have
 * found to handle it (there is here a typical deadlock situation - the same
 * which is found in org.w3c.jigsaw.http.Client 
 * - due to the fact that the server instances need to be locked in order
 * to change the connection pool, and at the same time each connection of 
 * the pool might have to notify the pool  of some events. This can easily
 * produce a deadlock...This situation is avoided here, by having the server 
 * locked only when appropriate...
 */
public class HttpBasicServer extends HttpServer {

    private static final String STATE_CONNECTION = "org.w3c.www.protocol.http.HttpBasicServer.connection";

    private static final String PROTOCOL = "http";

    private static final boolean debug = false;

    /**
     * Request mode - Full HTTP/1.1 compliant mode.
     */
    protected static final int RQ_HTTP11 = 1;

    /**
     * Request mode - Full two stage HTTP/1.1 compliant mode.
     */
    protected static final int RQ_HTTP11_TS = 2;

    /**
     * Request mode - HTTP/1.0 with no keep-alive support.
     */
    protected static final int RQ_HTTP10 = 3;

    /**
     * Request mode - HTTP/1.0 with keep-alive support.
     */
    protected static final int RQ_HTTP10_KA = 4;

    /**
     * Request mode - Unknown target server.
     */
    protected static final int RQ_UNKNOWN = 5;

    /**
     * Our central HTTP manager.
     */
    protected HttpManager manager = null;

    /**
     * The host name of the server we handle.
     */
    protected String host = null;

    /**
     * The port number of the server we handle.
     */
    protected int port = -1;

    /**
     * The timeout on the socket
     */
    protected int timeout = 300000;

    /**
     * The connectiontimeout for the socket
     */
    protected int conn_timeout = 3000;

    boolean contacted = false;

    short major = -1;

    short minor = -1;

    boolean keepalive = false;

    InetAddress addrs[] = null;

    int addrptr = 0;

    Date lookupLimit = null;

    /**
     * set the inet addresses of this host, and timestamp it
     * to avoid caching IPs forever
     * @param hostAddrs, an array of InetAddress
     */
    protected void setHostAddr(InetAddress hostAddrs[]) {
        this.addrs = hostAddrs;
        addrptr = 0;
        lookupLimit = new Date(System.currentTimeMillis() + 300 * 1000);
    }

    /**
     * check the validity of the host address
     * if invalid, it will update the InetAddress associated with this host
     */
    protected void updateHostAddr() throws HttpException {
        Date now = new Date();
        if ((lookupLimit == null) || lookupLimit.before(now)) {
            try {
                InetAddress hostAddrs[];
                int hostlen = host.length();
                boolean isstring = true;
                edu.hkust.clap.monitor.Monitor.loopBegin(147);
for (int i = 0; i < hostlen; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(147);
{
                    char c = host.charAt(i);
                    if (isstring = !(((c <= '9') && (c >= '0')) || (c == '.'))) {
                        break;
                    }
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(147);

                if (isstring) {
                    hostAddrs = InetAddress.getAllByName(host);
                } else {
                    hostAddrs = new InetAddress[1];
                    hostAddrs[0] = InetAddress.getByName(host);
                }
                if (hostAddrs != null) {
                    setHostAddr(hostAddrs);
                }
            } catch (UnknownHostException ex) {
                String msg = ("The host name [" + host + "] couldn't be resolved. " + "Details: \"" + ex.getMessage() + "\"");
                HttpException hex = new HttpException(ex, msg);
                state.ex = hex;
                state.state = HttpServerState.ERROR;
                throw hex;
            }
        }
    }

    /**
     * HttpServer implementation - Get this servers' protocol.
     * @return A String encoding the protocol used to dialog with the target
     * server.
     */
    public String getProtocol() {
        return PROTOCOL;
    }

    /**
     * HttpServer implementation - Get this server's major version number.
     * @return The server's major number version, or <strong>-1</strong>
     * if still unknown.
     */
    public short getMajorVersion() {
        return major;
    }

    /**
     * HttpServer implementation - Get this server's minor version number.
     * @return The server's minor number version, or <strong>-1</strong>
     * if still unknown.
     */
    public short getMinorVersion() {
        return minor;
    }

    /**
     * Set the timeout for the next connections
     * @param timeout The timeout in milliseconds
     */
    public synchronized void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Set the connection timeout for the next connections
     * @param timeout The timeout in milliseconds
     */
    public synchronized void setConnTimeout(int conn_timeout) {
        this.conn_timeout = conn_timeout;
    }

    /**
     * Connections management - Allocate a new connection for this server.
     * The connection is bound to the next available IP address, so that
     * we are able to round-robin on them. If one of the DNS advertized
     * IP address fails, we just try the next one, until one of them
     * succeed or all of them fail.
     * @return A freshly allocated connection, inserted in the idle connection
     * list.
     * @exception IOException If the server is unreachable through all of
     * Its IP addresses.
     */
    protected int connid = 0;

    protected HttpBasicConnection allocateConnection() throws IOException {
        HttpBasicConnection conn = null;
        try {
            updateHostAddr();
        } catch (HttpException hex) {
            if (addrs == null) {
                throw new IOException(hex.getMessage());
            }
        }
        edu.hkust.clap.monitor.Monitor.loopBegin(148);
for (int loop = 0; loop < addrs.length; loop++) { 
edu.hkust.clap.monitor.Monitor.loopInc(148);
{
            InetAddress addr = null;
            int i = addrptr;
            do {
                if ((addr = addrs[i]) != null) break;
                i = (i + 1) % addrs.length;
            } while (i != addrptr);
            addrptr = (addrptr + 1) % addrs.length;
            if (addr == null) {
                throw new IOException("Host " + host + " resolved to a null" + " InetAddr");
            }
            try {
                int l_connid;
                synchronized (this) {
                    l_connid = connid++;
                }
                conn = new HttpBasicConnection(this, l_connid, addr, port, timeout, conn_timeout, manager.getReplyFactory());
                break;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(148);

        if (conn == null) throw new IOException("Unable to connect to " + host);
        synchronized (manager) {
            state.incrConnectionCount();
            manager.notifyConnection(conn);
        }
        return conn;
    }

    /**
     * Connections management - Register a connection as being idle.
     * When a connection is created, or when it becomes idle again (after
     * having been used to handle a request), it has to be registered back to
     * the idle list of connection for the server. All connections
     * contained in this idle list are candidates for being elected to handle
     * some pending or incoming request to the host we manager.
     * @param conn The connection that is now idle.
     */
    public void registerConnection(HttpConnection conn) {
        synchronized (manager) {
            state.registerConnection(conn);
            manager.notifyIdle(conn);
        }
    }

    /**
     * Unregister a connection from the idle list.
     * Unregistering a connection means that the server shouldn't keep
     * track of it any more. This can happen in two situations:
     * <ul>
     * <li>The connection won't be reusable, so there is no point
     * for the server to try to keep track of it. In this case, the
     * connection is forgotten, and the caller will terminate it by invoking
     * the connection's input stream close method.
     * <li>The connection has successfully handle a connection, and the
     * connection is about to be reused. During the time of the request
     * processing, the server looses track of this connection, which will
     * register itself again when back to idle.
     * @param conn The connection to unregister from the idle list.
     */
    public synchronized void unregisterConnection(HttpConnection conn) {
        manager.notifyUse(conn);
    }

    public void deleteConnection(HttpConnection conn) {
        synchronized (manager) {
            state.decrConnectionCount();
            state.unregisterConnection(conn);
            manager.deleteConnection(conn);
        }
    }

    /**
     * Connections management - Get an idle connection to run a request.
     * The server has been asked to run a new request, and it now wants 
     * a connection to run it on. This method will try various ways of
     * aqcuiring a connection:
     * <ul>
     * <li>It will look for an idle connection.
     * <li>It will then try to negotiate with the HTTP manager the creation
     * of a new connection to the target server.
     * <li>If this fails too, it will just wait until a connection becomes
     * available.
     * </ul>
     * The connection returned is marked for use (ie it is unregistered from
     * the idle connection list), it is up to the caller to make sure that
     * if possible, the connection registers itself again to the idle list
     * after the processing is done.
     * <p>This method can return <strong>null</strong> to indicate to the 
     * caller that it should try again, in the hope that the target
     * server has multiple (different) IP addresses.
     * @return A connection marked in use, and which should be marked as
     * idle after the processing it is use for is done, or <strong>null
     * </strong> if a fresh connection cannot be established.
     */
    protected HttpBasicConnection getConnection() throws IOException {
        int _waits = 0;
        edu.hkust.clap.monitor.Monitor.loopBegin(149);
while (_waits < 3) { 
edu.hkust.clap.monitor.Monitor.loopInc(149);
{
            HttpBasicConnection conn = null;
            edu.hkust.clap.monitor.Monitor.loopBegin(150);
while (true) { 
edu.hkust.clap.monitor.Monitor.loopInc(150);
{
                conn = (HttpBasicConnection) manager.getConnection(this);
                if (conn == null) break;
                try {
                    if (conn.sock_m == null) {
                        conn.input.available();
                    }
                } catch (IOException ex) {
                    conn.close();
                    continue;
                }
                if (conn.markUsed()) {
                    return conn;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(150);

            if (manager.negotiateConnection(this)) {
                conn = allocateConnection();
                if (conn.markUsed()) {
                    return conn;
                } else {
                    return null;
                }
            }
            try {
                long _stime = System.currentTimeMillis();
                manager.waitForConnection(this);
                long _etime = System.currentTimeMillis();
                if (_etime - _stime > 20000) {
                    if (debug) {
                        System.err.println("wait " + _waits + " diff is " + (_etime - _stime));
                        System.err.println("ManagerState: " + manager);
                    }
                    _waits++;
                }
            } catch (InterruptedException ex) {
                return null;
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(149);

        return null;
    }

    /**
     * Display this server into a String.
     * @return A String based representation of the server object.
     */
    public String toString() {
        return host + ":" + port;
    }

    /**
     * A full round-trip has been run with the target server, update infos.
     * Each server instance maintains a set of informations to be reused
     * if needed when recontacting the server later. After a full round
     * trip has been performed with the server, it is time to update
     * the target server version number, and keeps-alive flag.
     * @param reply The first reply we got from this server.
     */
    protected synchronized void updateServerInfo(Reply reply) {
        if (contacted) return;
        major = reply.getMajorVersion();
        minor = reply.getMinorVersion();
        keepalive = reply.keepsAlive();
        contacted = true;
        if (debug) System.out.println("*** " + this + " major=" + major + ", minor=" + minor + ", ka=" + keepalive);
    }

    /**
     * HttpServer implementation - Initialize this server to its target.
     * @param manager The central HTTP manager.
     * @param state The manager's state for that server.
     * @param host The target server's host name.
     * @param port The target server's port number.
     * @param timeout The timeout for the connection handled by the server
     * @exception HttpException If the server host couldn't be resolved
     * to one or more IP addresses.
     */
    public void initialize(HttpManager manager, HttpServerState state, String host, int port, int timeout) throws HttpException {
        initialize(manager, state, host, port, timeout, conn_timeout);
    }

    /**
     * HttpServer implementation - Initialize this server to its target.
     * @param manager The central HTTP manager.
     * @param state The manager's state for that server.
     * @param host The target server's host name.
     * @param port The target server's port number.
     * @param timeout The timeout for the connection handled by the server
     * @param timeout The connection timeout in millisecond for the sockets
     * @exception HttpException If the server host couldn't be resolved
     * to one or more IP addresses.
     */
    public void initialize(HttpManager manager, HttpServerState state, String host, int port, int timeout, int conn_timeout) throws HttpException {
        this.manager = manager;
        this.state = state;
        this.host = host;
        this.port = port;
        this.timeout = timeout;
        this.conn_timeout = conn_timeout;
        this.lookupLimit = null;
        updateHostAddr();
        this.state.state = HttpServerState.OK;
    }

    protected void notifyObserver(RequestObserver obs, Request request, int code) {
        RequestEvent evt = new RequestEvent(this, request, code);
        obs.notifyProgress(evt);
    }

    protected void notifyObserver(RequestObserver obs, RequestEvent evt) {
        obs.notifyProgress(evt);
    }

    /**
     * Is this request a two stage request.
     * @return A boolean, <strong>true</strong> if the request is two
     * stage, <strong>false</strong> otherwise.
     */
    protected boolean isTwoStage_10(Request request) {
        return request.hasOutputStream();
    }

    /**
     * Is this request a two stage request.
     * @return A boolean, <strong>true</strong> if the request is two
     * stage, <strong>false</strong> otherwise.
     */
    protected boolean isTwoStage_11(Request request) {
        boolean hasOutput = request.hasOutputStream();
        if (hasOutput) {
            if (request.getExpect() != null) return true;
        }
        return false;
    }

    /**
     * Get the current mode of running request for that server.
     * This method check our knowledge of the target server, and deduce
     * the mode in which the given request should be run.
     * @return An integer code, indicating the mode in which the request should
     * be run:
     * <dl>
     * <dt>RQ_HTTP11<dl>The request should be run as an HTTP/1.1 request.
     * <dt>RQ_HTTP10<dl>The request should be run as an HTTP/1.0 request.
     * <dt>RQ_HTTP10_KA<dl>HTTP/1.0 with keep-alive support.
     * <dt>RQ_UNKNOWN</dl>This is the first request, we don't know yet.
     * </dl>
     */
    protected int getRequestMode(Request request) {
        if ((!contacted) || (major < 1)) return RQ_UNKNOWN;
        if (minor < 1) {
            return keepalive ? RQ_HTTP10_KA : RQ_HTTP10;
        } else {
            return isTwoStage_11(request) ? RQ_HTTP11_TS : RQ_HTTP11;
        }
    }

    /**
     * Run a fully HTTP/1.1 compliant request.
     * This request has no body, so we can do whatever we want with it,
     * and retry as many time as we want.
     * @param conn The connection to run the request on.
     * @param request The request to run.
     * @return A Reply instance, if success; <strong>null</strong> if the 
     * request should be retried.
     * @exception IOException If some IO error occured.
     * @exception MimeParserException If some MIME parsing error occured.
     */
    protected Reply http11_run(HttpBasicConnection conn, Request request) throws IOException, MimeParserException {
        if (debug) System.out.println(conn + ": runs[11] " + request.getURL());
        RequestObserver o = request.getObserver();
        OutputStream os = conn.getOutputStream();
        MimeParser p = conn.getParser();
        Reply reply = null;
        boolean needsChunk = false;
        if (request.hasOutputStream()) {
            if (request.getContentLength() < 0) {
                needsChunk = true;
                String tes[] = request.getTransferEncoding();
                if (tes == null) {
                    request.addTransferEncoding("chunked");
                } else {
                    boolean addIt = true;
                    edu.hkust.clap.monitor.Monitor.loopBegin(151);
for (int i = 0; !addIt && (i < tes.length); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(151);
{
                        addIt = addIt && !tes[i].equals("chunked");
                    }} 
edu.hkust.clap.monitor.Monitor.loopEnd(151);

                    if (addIt) {
                        request.addTransferEncoding("chunked");
                    } else {
                        if (os instanceof ChunkedOutputStream) {
                            needsChunk = false;
                        }
                    }
                }
            }
        }
        try {
            request.emit(os, Request.EMIT_HEADERS);
            os.flush();
            if (o != null) {
                notifyObserver(o, new ConnectedEvent(this, request, os));
            }
            if (request.hasOutputStream()) {
                String exp = request.getExpect();
                if ((exp != null) && (exp.equalsIgnoreCase("100-continue"))) {
                    if (o != null) {
                        notifyObserver(o, new ContinueEvent(this, request));
                    }
                }
                if (needsChunk) {
                    DataOutputStream dos = new DataOutputStream(os);
                    ChunkedOutputStream cos = new ChunkedOutputStream(dos);
                    request.emit(cos, Request.EMIT_BODY);
                    cos.flush();
                    cos.close(false);
                    request.emit(os, Request.EMIT_FOOTERS);
                } else {
                    request.emit(os, Request.EMIT_BODY | Request.EMIT_FOOTERS);
                }
                os.flush();
            }
            reply = (Reply) p.parse(manager.isLenient());
            edu.hkust.clap.monitor.Monitor.loopBegin(152);
while ((reply.getStatus() / 100) == 1) { 
edu.hkust.clap.monitor.Monitor.loopInc(152);
{
                if (o != null) {
                    notifyObserver(o, new ContinueEvent(this, request, reply));
                }
                reply = (Reply) p.parse(manager.isLenient());
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(152);

        } catch (MimeParserException ex) {
            return null;
        } catch (IOException ioex) {
            return null;
        }
        if (reply != null) {
            conn.setCloseOnEOF(reply.hasConnection("close"));
        }
        return reply;
    }

    /**
     * Run a two stage HTTP/1.1 compliant request.
     * The neat thing about this sort of request is that as they support 
     * <strong>100</strong> status codes, we <em>know</em> when we have
     * to retry them.
     * @param conn The connection to run the request on.
     * @param request The request to run.
     * @return A Reply instance, if success; <strong>null</strong> if the 
     * request should be retried.
     * @exception IOException If some IO error occured.
     * @exception MimeParserException If some MIME parsing error occured.
     */
    protected Reply http11_ts_run(HttpBasicConnection conn, Request request) throws IOException, MimeParserException {
        if (debug) System.out.println(conn + ": runs[11ts] " + request.getURL());
        RequestObserver o = request.getObserver();
        OutputStream os = conn.getOutputStream();
        MimeParser p = conn.getParser();
        Reply reply = null;
        boolean needsChunk = false;
        if (request.getContentLength() < 0) {
            needsChunk = true;
            String tes[] = request.getTransferEncoding();
            if (tes == null) {
                request.addTransferEncoding("chunked");
            } else {
                boolean addIt = true;
                edu.hkust.clap.monitor.Monitor.loopBegin(151);
for (int i = 0; !addIt && (i < tes.length); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(151);
{
                    addIt = addIt && !tes[i].equals("chunked");
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(151);

                if (addIt) {
                    request.addTransferEncoding("chunked");
                } else {
                    if (os instanceof ChunkedOutputStream) {
                        needsChunk = false;
                    }
                }
            }
        }
        try {
            request.emit(os, Request.EMIT_HEADERS);
            os.flush();
            if (o != null) notifyObserver(o, new ConnectedEvent(this, request, os));
            reply = (Reply) p.parse(manager.isLenient());
            boolean bodySent = false;
            edu.hkust.clap.monitor.Monitor.loopBegin(153);
while ((reply.getStatus() / 100) == 1 || reply.getStatus() == HTTP.EXPECTATION_FAILED) { 
edu.hkust.clap.monitor.Monitor.loopInc(153);
{
                if (reply.getStatus() == HTTP.EXPECTATION_FAILED) return reply;
                reply = null;
                if (o != null) {
                    notifyObserver(o, new ContinueEvent(this, request, reply));
                }
                if (!bodySent) {
                    bodySent = true;
                    if (needsChunk) {
                        DataOutputStream dos = new DataOutputStream(os);
                        ChunkedOutputStream cos = new ChunkedOutputStream(dos);
                        request.emit(cos, Request.EMIT_BODY);
                        cos.flush();
                        cos.close(false);
                        request.emit(os, Request.EMIT_FOOTERS);
                    } else {
                        request.emit(os, Request.EMIT_BODY | Request.EMIT_FOOTERS);
                    }
                    os.flush();
                }
                reply = (Reply) p.parse(manager.isLenient());
                edu.hkust.clap.monitor.Monitor.loopBegin(152);
while ((reply.getStatus() / 100) == 1) { 
edu.hkust.clap.monitor.Monitor.loopInc(152);
{
                    if (o != null) {
                        notifyObserver(o, new ContinueEvent(this, request, reply));
                    }
                    reply = (Reply) p.parse(manager.isLenient());
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(152);

            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(153);

        } catch (MimeParserException ex) {
            return null;
        } catch (IOException ieox) {
            return null;
        }
        if (reply != null) {
            conn.setCloseOnEOF(reply.hasConnection("close"));
        }
        return reply;
    }

    /**
     * Run an HTTP/1.0 request that has support for keep alive.
     * This kind of request are the worst one with regard to the retry 
     * strategy we can adopt.
     * @param conn The connection to run the request on.
     * @param request The request to run.
     * @return A Reply instance, if success; <strong>null</strong> if the 
     * request should be retried.
     * @exception IOException If some IO error occured.
     * @exception MimeParserException If some MIME parsing error occured.
     */
    protected Reply http10_ka_run(HttpBasicConnection conn, Request request) throws IOException, MimeParserException {
        if (debug) System.out.println(conn + ": runs[10_ka] " + request.getURL());
        RequestObserver o = request.getObserver();
        OutputStream os = conn.getOutputStream();
        MimeParser p = conn.getParser();
        Reply reply = null;
        String exp = request.getExpect();
        if ((exp != null) && (exp.equalsIgnoreCase("100-continue"))) {
            reply = request.makeReply(HTTP.EXPECTATION_FAILED);
            reply.setContent("100-continue is not supported by upstream " + "HTTP/1.0 Server");
            return reply;
        }
        if (request.getConnection() == null) {
            if (request.hasProxy()) {
                request.addProxyConnection("Keep-Alive");
            } else {
                request.addConnection("Keep-Alive");
            }
        }
        try {
            request.emit(os, Request.EMIT_HEADERS);
            os.flush();
        } catch (IOException ioex) {
            return null;
        }
        if (o != null) notifyObserver(o, new ConnectedEvent(this, request, os));
        if (isTwoStage_10(request)) {
            if (o != null) notifyObserver(o, new ContinueEvent(this, request));
            request.emit(os, Request.EMIT_BODY | Request.EMIT_FOOTERS);
            os.flush();
        }
        try {
            reply = (Reply) p.parse(manager.isLenient());
            edu.hkust.clap.monitor.Monitor.loopBegin(154);
while ((reply.getStatus() / 100) == 1) { 
edu.hkust.clap.monitor.Monitor.loopInc(154);
{
                if (o != null) {
                    notifyObserver(o, new ContinueEvent(this, request, reply));
                }
                reply = null;
                reply = (Reply) p.parse(manager.isLenient());
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(154);

        } catch (IOException ex) {
            if (isTwoStage_10(request)) {
                try {
                    reply = (Reply) p.parse(manager.isLenient());
                } catch (MimeParserException mex) {
                    return null;
                }
                try {
                    request.getOutputStream().close();
                } catch (Exception cex) {
                }
                ;
                return reply;
            }
            return null;
        } catch (MimeParserException ex) {
        }
        if (reply != null) {
            conn.setCloseOnEOF(reply.hasConnection("close"));
        }
        return reply;
    }

    /**
     * Run a simple HTTP/1.0 request.
     * This server doesn't support keep-alive, we know the connection is
     * always fresh, we don't need to go into the retry buisness.
     * <p>That's <strong>cool</strong> !
     * @param conn The connection to run the request on.
     * @param request The request to run.
     * @return A Reply instance, if success; <strong>null</strong> if the 
     * request should be retried.
     * @exception IOException If some IO error occured.
     * @exception MimeParserException If some MIME parsing error occured.
     */
    protected Reply http10_run(HttpBasicConnection conn, Request request) throws IOException, MimeParserException {
        if (debug) System.out.println(conn + ": runs[10] " + request.getURL());
        RequestObserver o = request.getObserver();
        OutputStream os = conn.getOutputStream();
        MimeParser p = conn.getParser();
        Reply reply = null;
        String exp = request.getExpect();
        if ((exp != null) && (exp.equalsIgnoreCase("100-continue"))) {
            reply = request.makeReply(HTTP.EXPECTATION_FAILED);
            reply.setContent("100-continue is not supported by upstream " + "HTTP/1.0 Server");
            return reply;
        }
        request.emit(os, Request.EMIT_HEADERS);
        os.flush();
        if (o != null) {
            notifyObserver(o, new ConnectedEvent(this, request, os));
            if (isTwoStage_10(request)) notifyObserver(o, new ContinueEvent(this, request));
        }
        try {
            request.emit(os, Request.EMIT_BODY | Request.EMIT_FOOTERS);
            os.flush();
        } catch (IOException ex) {
            if (isTwoStage_10(request)) {
                try {
                    reply = (Reply) p.parse(manager.isLenient());
                } catch (MimeParserException mex) {
                    throw ex;
                }
                if (reply != null) {
                    try {
                        request.getOutputStream().close();
                    } catch (Exception cex) {
                    }
                    ;
                    return reply;
                }
            }
            throw ex;
        }
        try {
            reply = (Reply) p.parse(manager.isLenient());
            edu.hkust.clap.monitor.Monitor.loopBegin(154);
while ((reply.getStatus() / 100) == 1) { 
edu.hkust.clap.monitor.Monitor.loopInc(154);
{
                if (o != null) {
                    notifyObserver(o, new ContinueEvent(this, request, reply));
                }
                reply = null;
                reply = (Reply) p.parse(manager.isLenient());
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(154);

        } catch (MimeParserException ex) {
        }
        conn.setCloseOnEOF(true);
        return reply;
    }

    /**
     * Run that request, we don't know what server we have at the other end.
     * We know the connection is fresh, we use the <code>http10_run</code>
     * and update the server infos by the end of processing.
     * @param conn The connection to run the request on.
     * @param request The request to run.
     * @return A Reply instance, if success; <strong>null</strong> if the 
     * request should be retried.
     * @exception IOException If some IO error occured.
     * @exception MimeParserException If some MIME parsing error occured.
     */
    protected Reply http_unknown(HttpBasicConnection conn, Request request) throws IOException, MimeParserException {
        boolean eventfaked = false;
        if (debug) System.out.println(conn + ": runs[unknown] " + request.getURL());
        if (request.getConnection() == null) {
            if (request.hasProxy()) {
                request.addProxyConnection("Keep-Alive");
            } else {
                request.addConnection("Keep-Alive");
            }
        }
        RequestObserver o = request.getObserver();
        OutputStream os = conn.getOutputStream();
        MimeParser p = conn.getParser();
        Reply reply = null;
        request.emit(os, Request.EMIT_HEADERS);
        if (o != null) {
            notifyObserver(o, new ConnectedEvent(this, request, os));
            if (isTwoStage_10(request)) {
                eventfaked = true;
                notifyObserver(o, new ContinueEvent(this, request));
            }
        }
        try {
            request.emit(os, Request.EMIT_BODY | Request.EMIT_FOOTERS);
            os.flush();
        } catch (IOException ex) {
            if (isTwoStage_10(request)) {
                try {
                    reply = (Reply) p.parse(manager.isLenient());
                } catch (MimeParserException mex) {
                    throw ex;
                }
                if (reply != null) {
                    try {
                        request.getOutputStream().close();
                    } catch (Exception cex) {
                    }
                    ;
                    return reply;
                }
            }
            throw ex;
        }
        try {
            reply = (Reply) p.parse(manager.isLenient());
            edu.hkust.clap.monitor.Monitor.loopBegin(155);
while ((reply.getStatus() / 100) == 1) { 
edu.hkust.clap.monitor.Monitor.loopInc(155);
{
                if (eventfaked) {
                    eventfaked = false;
                    continue;
                }
                if (o != null) notifyObserver(o, new ContinueEvent(this, request, reply));
                reply = (Reply) p.parse(manager.isLenient());
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(155);

            if (reply != null) {
                updateServerInfo(reply);
            }
        } catch (MimeParserException ex) {
        }
        if (reply != null) {
            if ((major == 1) && ((minor == 1) || keepalive)) {
                conn.setCloseOnEOF(false);
            } else {
                conn.setCloseOnEOF(true);
            }
        }
        return reply;
    }

    /**
     * Exceute given request on given conection, according to server.
     * @param conn The connection to use to run that request.
     * @param request The request to execute.
     * @exception IOException If some IO error occurs, or if the 
     * request is interrupted.
     * @exception MimeParserException If taregt server doesn't conform to 
     * the HTTP specification.
     */
    protected Reply doRequest(HttpBasicConnection conn, Request request) throws IOException, MimeParserException {
        if (request.isInterrupted()) throw new IOException("Interrupted Request");
        request.setState(STATE_CONNECTION, conn);
        switch(getRequestMode(request)) {
            case RQ_HTTP11:
                return http11_run(conn, request);
            case RQ_HTTP11_TS:
                return http11_ts_run(conn, request);
            case RQ_HTTP10_KA:
                return http10_ka_run(conn, request);
            case RQ_HTTP10:
                return http10_run(conn, request);
            case RQ_UNKNOWN:
                return http_unknown(conn, request);
            default:
                throw new RuntimeException("Implementation bug.");
        }
    }

    /**
     * Interrupt given request, that was launched by ourself.
     * @param request The request to interrupt.
     */
    protected void interruptRequest(Request request) {
        HttpBasicConnection c = null;
        c = (HttpBasicConnection) request.getState(STATE_CONNECTION);
        if (c == null) {
            ;
        } else {
            c.markIdle(true);
        }
    }

    /**
     * Run the given request in synchronous mode.
     * @param request The request to run.
     * @return A Reply instance, containing the reply headers, and the 
     * optional reply entity, to be read by the calling thread.
     * @exception HttpException If the request processing failed.
     */
    public Reply runRequest(Request request) throws HttpException {
        RequestObserver o = request.getObserver();
        if (debug) {
            System.out.println("Running request: " + request.getURL());
            request.dump(System.out);
        }
        HttpBasicConnection conn = null;
        Reply reply = null;
        Exception lastex = null;
        try {
            if (o != null) notifyObserver(o, request, RequestEvent.EVT_QUEUED);
            int maxretry = 1;
            if (!request.hasOutputStream()) {
                String method = request.getMethod();
                if (method.length() <= 6) {
                    method = method.intern();
                    if ((method == HTTP.GET) || (method == HTTP.HEAD) || (method == HTTP.OPTIONS) || (method == HTTP.TRACE)) {
                        maxretry = 3;
                    }
                }
            }
            edu.hkust.clap.monitor.Monitor.loopBegin(156);
for (int i = 0; (reply == null) && (i < maxretry); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(156);
{
                if ((conn = getConnection()) == null) {
                    continue;
                }
                if (maxretry == 1) {
                    if (manager.keepbody) {
                        if (request.hasOutputStream()) {
                            BufferedInputStream bis;
                            bis = new BufferedInputStream(request.getOutputStream());
                            request.setOutputStream(bis);
                            int cl = request.getContentLength();
                            if (cl < 0) cl = 65536;
                            bis.mark(cl);
                        }
                    } else {
                        edu.hkust.clap.monitor.Monitor.loopBegin(157);
while (conn.cached) { 
edu.hkust.clap.monitor.Monitor.loopInc(157);
{
                            conn.markIdle(true);
                            conn = getConnection();
                        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(157);

                    }
                }
                try {
                    if ((reply = doRequest(conn, request)) == null) {
                        if (conn.cached) {
                            --i;
                            if (request.hasOutputStream() && manager.keepbody) {
                                request.getOutputStream().reset();
                            }
                        }
                        conn.markIdle(true);
                    }
                } catch (MimeParserException mex) {
                    lastex = mex;
                    conn.markIdle(true);
                    throw (mex);
                } catch (Exception ioex) {
                    lastex = ioex;
                    if (debug) ioex.printStackTrace();
                    try {
                        if (request.hasOutputStream()) {
                            request.getOutputStream().reset();
                        }
                    } catch (IOException mrex) {
                    } finally {
                        conn.markIdle(true);
                    }
                } catch (Throwable thro) {
                    conn.markIdle(true);
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(156);

            if (request.hasOutputStream()) {
                request.getOutputStream().close();
            }
            if (reply == null) {
                String msg = ("Unable to contact target server " + this + " after " + maxretry + " tries.");
                if (o != null) notifyObserver(o, request, RequestEvent.EVT_UNREACHABLE);
                if (lastex != null) {
                    throw new HttpException(request, null, lastex, msg);
                } else {
                    throw new HttpException(request, msg);
                }
            }
            reply.matchesRequest(request);
            if (o != null) notifyObserver(o, request, RequestEvent.EVT_REPLIED);
            if (reply.keepsAlive()) {
                reply.setStreamObserver(conn);
                if (!reply.hasInputStream()) {
                    conn.markIdle(false);
                }
            } else {
                conn.detach();
            }
            if (debug) {
                System.out.println("Request done !");
                reply.dump(System.out);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            if (conn != null) conn.markIdle(true);
            if (o != null) notifyObserver(o, request, RequestEvent.EVT_CLOSED);
            throw new HttpException(request, ex);
        } catch (MimeParserException ex) {
            ex.printStackTrace();
            if (conn != null) conn.markIdle(true);
            if (o != null) notifyObserver(o, request, RequestEvent.EVT_CLOSED);
            throw new HttpException(request, ex);
        }
        return reply;
    }

    HttpBasicServer() {
    }
}
