package org.w3c.www.mux;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

public class MuxStream {

    /**
     * That stream accept handler.
     */
    protected MuxStreamHandler handler = null;

    /**
     * This stream reader.
     */
    protected MuxReader reader = null;

    /**
     * This stream writer.
     */
    protected MuxWriter writer = null;

    /**
     * Currently defined sessions.
     */
    protected MuxSession sessions[] = null;

    /**
     * Is this the server side of the MUX channel ?
     */
    protected boolean server = false;

    /**
     * Inet address of the other end's connection (maybe <strong>null</strong>)
     */
    protected InetAddress inetaddr = null;

    /**
     * The raw input stream.
     */
    protected InputStream in = null;

    /**
     * The raw output stream.
     */
    protected OutputStream out = null;

    /**
     * Is this muxed stream still alive ?
     */
    protected boolean alive = true;

    private synchronized MuxSession createSession(int sessid, int protid) throws IOException {
        MuxSession session = sessions[sessid];
        if (session == null) {
            session = new MuxSession(this, sessid, protid);
            sessions[sessid] = session;
        } else {
            System.out.println("MuxStream:createSession: already existing !");
        }
        return session;
    }

    private MuxSession acceptSession(int flags, int sessid, int protid) throws IOException {
        if (server & ((sessid & 1) == 0)) throw new IOException("MUX: Invalid even session id " + sessid);
        MuxSession session = null;
        if ((handler != null) && handler.acceptSession(this, sessid, protid)) {
            session = createSession(sessid, protid);
            handler.notifySession(session);
        } else {
            session = null;
            System.out.println(this + ": RST (accept) session " + sessid);
            writer.writeMessage(sessid, MUX.RST, 0);
            writer.flush();
        }
        return session;
    }

    private final synchronized MuxSession allocateSession(int protid) throws IOException {
        int i = (server ? 2 : 3);
        edu.hkust.clap.monitor.Monitor.loopBegin(505);
for (; i < sessions.length; i += 2) { 
edu.hkust.clap.monitor.Monitor.loopInc(505);
{
            if (sessions[i] == null) {
                sessions[i] = new MuxSession(this, i, protid);
                return sessions[i];
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(505);

        MuxSession session = checkSession(i);
        if (session == null) session = new MuxSession(this, i, protid);
        sessions[i] = session;
        return session;
    }

    private final synchronized MuxSession checkSession(int sessid) throws IOException {
        if (sessid >= MUX.MAX_SESSION) throw new IOException("MUX: Invalid session id " + sessid);
        if (sessid >= sessions.length) {
            MuxSession ns[] = new MuxSession[sessid + MUX.SESSIONS_INCR];
            System.arraycopy(sessions, 0, ns, 0, sessions.length);
            sessions = ns;
        }
        return sessions[sessid];
    }

    /**
     * This stream is dying, clean it up.
     * It is up to the caller to make sure all existing sessions have been
     * terminated (gracefully or not).
     * <p>This will shutdown all realted threads, and close the transport 
     * streams.
     */
    private synchronized void cleanup() {
        alive = false;
        reader.shutdown();
        writer.shutdown();
        reader = null;
        writer = null;
        try {
            in.close();
            out.close();
        } catch (IOException ex) {
        }
        in = null;
        out = null;
    }

    /**
     * Get this stream MuxWriter object.
     * @return A MuxWriter instance.
     */
    protected final MuxWriter getMuxWriter() {
        return writer;
    }

    /**
     * A severe (fatal for that connection) errror has occured. Cleanup.
     * @param obj The object that has generated the error.
     * @param ex The exception that triggered the error (or <strong>null
     * </strong> null if this was a logical error).
     */
    protected void error(Object obj, Exception ex) {
        System.out.println("*** Fatal error on " + this);
        ex.printStackTrace();
        System.out.println("No recovery !");
        System.exit(1);
    }

    /**
     * A soft error has occured (eg socket close), Cleanup.
     * @param obj The object that has detected the soft error.
     * @param msg An associated String message.
     */
    protected synchronized void error(Object obj, String msg) {
        boolean problems = false;
        synchronized (this) {
            edu.hkust.clap.monitor.Monitor.loopBegin(506);
for (int i = 0; i < sessions.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(506);
{
                if (sessions[i] != null) sessions[i].abort();
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(506);

        }
        cleanup();
    }

    /**
     * Handle the given DefineString control message.
     * @param strid The identifier for that String in the futur.
     * @param str This String being defined.
     */
    protected void ctrlDefineString(int strid, String str) {
    }

    /**
     * Handle the given DefineStack control message.
     * @param id The identifier for that stack in the future.
     * @param stack The stack description (as an array of shorts).
     */
    protected void ctrlDefineStack(int id, int stack[]) throws IOException {
    }

    /**
     * Handle the given MuxControl control message.
     * @param sessid The session to which that message applies.
     * @param fragsz The max allowed fragment size on that session.
     */
    protected void ctrlMuxControl(int sessid, int fragsz) throws IOException {
        MuxSession session = lookupSession(sessid, true);
        session.notifyControl(fragsz);
    }

    /**
     * Handle the given SendCredit control message.
     * @param sessid The session to which that message applies.
     * @param credit The allowed credits.
     */
    protected void ctrlSendCredit(int sessid, int credit) throws IOException {
        MuxSession session = lookupSession(sessid, true);
        session.notifyCredit(credit);
    }

    /**
     * Handle that new incomming message.
     * This method is called by the reader of that session, to dispatch
     * the message currently being read.
     * @return A MuxSession instance to dispatch that message to, or
     * <strong>null</strong> otherwise (ie a new session was rejected, etc).
     * In that last case, it is up to the reader of that session to discard 
     * any pending data.
     */
    protected MuxSession lookupSession(int flags, int sessid, int length, int llength) throws IOException {
        MuxSession session = checkSession(sessid);
        if (session == null) {
            if ((flags & MUX.SYN) != 0) {
                session = acceptSession(flags, sessid, length);
            } else if ((flags & MUX.FIN) != MUX.FIN) {
                System.out.println(this + ": RST (lookup) session " + sessid);
                if ((flags & MUX.RST) != MUX.RST) {
                    writer.writeMessage(sessid, MUX.RST, 0);
                }
            }
        }
        return session;
    }

    /**
     * Lookup for an already existing session having the given identifier.
     * @param sessid The identifier of the session to look for.
     * @param check Is <strong>null</strong> a valid answer, if set and
     * the requested session doesn't exist, a runtime exception is thrown.
     * @return A MuxSession instance, or <strong>null</strong> if check is
     * <strong>false</strong> and no session was found.
     */
    protected synchronized MuxSession lookupSession(int sessid, boolean check) {
        if (sessid < sessions.length) {
            MuxSession session = sessions[sessid];
            if (session != null) return session;
        }
        if (check) {
            throw new RuntimeException("MuxStream:lookupSession: " + " invalid session id " + sessid + ".");
        }
        return null;
    }

    /**
     * Unregiter the given session, it has been closed.
     * @param session The session to unregister.
     */
    protected synchronized void unregisterSession(MuxSession session) {
        sessions[session.getIdentifier()] = null;
    }

    /**
     * Create a new MUX session, by connecting to the other end.
     * @param protid The protocol that is going to be spoken on that new 
     * session.
     * @return A connected MuxSession.
     * @exception IOException If the connection couldn't be set up properly.
     */
    public MuxSession connect(int protid) throws IOException {
        synchronized (this) {
            if (!alive) throw new IOException("Broken mux stream");
        }
        MuxSession session = allocateSession(protid);
        writer.writeMessage(session.getIdentifier(), MUX.SYN, protid, null, 0, 0);
        return session;
    }

    /**
     * Get the InetAddress associated with that MUX stream, if any.
     * MUX streams can run on any kind of Input/Output streams. This method
     * will only return a non-null instance when possible.
     * @return An InetAddress instance, or <strong>null</strong> if not
     * available.
     */
    public InetAddress getInetAddress() {
        return inetaddr;
    }

    /**
     * Shutdown this stream, and associated sessions gracefully.
     * @param force If <strong>true</strong> abort all existing sessions, and
     * close the muxed streams physically. Otherwise, shutdown the muxed stream
     * gracefully only if no more sessions are running.
     * @return A boolean, <strong>true</strong> if shutdown was performed,
     * <strong>false</strong> if it was not performed because <em>force</em>
     * was <strong>false</strong> and some sessions were still running.
     * @exception IOException If some IO error occured.
     */
    public synchronized boolean shutdown(boolean force) throws IOException {
        if (!alive) return true;
        boolean terminate = true;
        if (force) {
            edu.hkust.clap.monitor.Monitor.loopBegin(507);
for (int i = 0; i < sessions.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(507);
{
                MuxSession s = sessions[i];
                if (s != null) s.abort();
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(507);

        } else {
            edu.hkust.clap.monitor.Monitor.loopBegin(508);
for (int i = 0; i < sessions.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(508);
{
                if (sessions[i] != null) {
                    terminate = false;
                    break;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(508);

        }
        if (terminate) cleanup();
        return terminate;
    }

    public MuxStream(boolean server, MuxStreamHandler handler, InputStream in, OutputStream out) throws IOException {
        this.server = server;
        this.handler = handler;
        this.in = in;
        this.out = out;
        this.reader = new MuxReader(this, in);
        this.writer = new MuxWriter(this, out);
        this.sessions = new MuxSession[8];
        this.reader.start();
    }

    public MuxStream(boolean server, MuxStreamHandler handler, Socket socket) throws IOException {
        this(server, handler, socket.getInputStream(), socket.getOutputStream());
        this.inetaddr = socket.getInetAddress();
    }
}
