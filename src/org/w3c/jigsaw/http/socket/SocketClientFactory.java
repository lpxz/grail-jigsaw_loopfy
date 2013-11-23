package org.w3c.jigsaw.http.socket;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import org.w3c.jigsaw.http.Client;
import org.w3c.jigsaw.http.ClientFactory;
import org.w3c.jigsaw.http.httpd;
import org.w3c.jigsaw.config.PropertySet;
import org.w3c.util.LRUAble;
import org.w3c.util.LRUList;
import org.w3c.util.ObservableProperties;
import org.w3c.util.PropertyMonitoring;
import org.w3c.util.Status;
import org.w3c.util.SyncLRUList;
import org.w3c.util.ThreadCache;

class DebugThread extends Thread {

    SocketClientFactory pool = null;

    public void run() {
        edu.hkust.clap.monitor.Monitor.loopBegin(455);
while (true) { 
edu.hkust.clap.monitor.Monitor.loopInc(455);
{
            try {
                sleep(1000 * 10);
                SocketClientState cs = null;
                cs = (SocketClientState) pool.freeList.getHead();
                edu.hkust.clap.monitor.Monitor.loopBegin(456);
while (cs != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(456);
{
                    System.out.println(cs.client + " reqcount=" + cs.client.getRequestCount() + ", bindcount=" + cs.client.getBindCount());
                    cs = (SocketClientState) pool.freeList.getNext((LRUAble) cs);
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(456);

                System.out.println("freeCount =" + pool.freeCount);
                System.out.println("idleCount =" + pool.idleCount);
                System.out.println("totalCount=" + pool.clientCount);
                System.out.println("estimCount=" + pool.clientEstim);
                System.out.println("Average: " + pool.loadavg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }} 


    }

    DebugThread(SocketClientFactory pool) {
        this.pool = pool;
        setPriority(Thread.MAX_PRIORITY);
    }
}

/**
 * The client pool is a kind of client factory.
 * Each time the server gets a new connection, it calls the client pool
 * to bound a client object (newly created or spared) to handle it.
 */
public class SocketClientFactory implements ClientFactory, PropertyMonitoring, Status {

    private static final boolean debug = false;

    private static final boolean debugstats = false;

    private static final boolean debugthread = false;

    public static final int MINSPARE_FREE = 5;

    public static final int MAXSPARE_FREE = 10;

    public static final int MAXSPARE_IDLE = 20;

    public static final int MAXTHREADS = 4;

    public static final int MAXCLIENTS = 32;

    public static final int IDLETO = 10000;

    public static final int AVG_LIGHT = 1;

    public static final int AVG_NORMAL = 2;

    public static final int AVG_HIGH = 3;

    public static final int AVG_DEAD = 4;

    public static final String MINSPARE_FREE_P = "org.w3c.jigsaw.http.socket.SocketClientFactory.minFree";

    public static final String MAXSPARE_FREE_P = "org.w3c.jigsaw.http.socket.SocketClientFactory.maxFree";

    public static final String MAXSPARE_IDLE_P = "org.w3c.jigsaw.http.socket.SocketClientFactory.maxIdle";

    public static final String MAXTHREADS_P = "org.w3c.jigsaw.http.socket.SocketClientFactory.maxThreads";

    public static final String MAXCLIENTS_P = "org.w3c.jigsaw.http.socket.SocketClientFactory.maxClients";

    public static final String IDLETO_P = "org.w3c.jigsaw.http.socket.SocketClientFactory.idleTimeout";

    public static final String BINDADDR_P = "org.w3c.jigsaw.http.socket.SocketClientFactory.bindAddress";

    public static final String TIMEOUT_P = "org.w3c.jigsaw.http.socket.SocketClientFactory.timeout";

    int minFree = 0;

    int maxFree = 0;

    int maxIdle = 0;

    int maxClients = 0;

    InetAddress bindAddr = null;

    int timeout = 0;

    int count = 0;

    httpd server = null;

    ObservableProperties props = null;

    int busyCount = 0;

    LRUList idleList = null;

    LRUList freeList = null;

    SocketClientState csList = null;

    int idleCount = 0;

    int freeCount = 0;

    int clientCount = 0;

    int clientEstim = 0;

    ThreadCache threadcache = null;

    int loadavg = AVG_LIGHT;

    boolean alive = true;

    /**
     * Give the status of this class as a partial HTML text which will be added
     * into a block level element
     * @return a String, the generated HTML
     */
    public String getHTMLStatus() {
        int idle = 0;
        int free = 0;
        int used = 0;
        int bndc = 0;
        int reqc = 0;
        int bnd, req;
        StringBuffer sb = new StringBuffer();
        SocketClientState cs = null;
        StringBuffer sb1 = null;
        if (debugstats) {
            sb1 = new StringBuffer();
        }
        cs = csList;
        if (debugstats) {
            sb1.append("<table border=\"1\" class=\"idle\">\n" + "<caption>Used Clients" + "</caption><tr><th>Id</th><th>BindCount</th>" + "<th>ReqCount</th><th>Diff</th>" + "<th>BoundTo</th><th>URI</th></tr>\n");
        }
        edu.hkust.clap.monitor.Monitor.loopBegin(457);
while (cs != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(457);
{
            if (cs.status == SocketClientState.C_BUSY) {
                InetAddress ia = cs.client.getInetAddress();
                bnd = cs.client.getBindCount();
                req = cs.client.getRequestCount();
                if (debugstats) {
                    sb1.append("<tr><td>");
                    sb1.append(cs.id);
                    sb1.append("</td><td>");
                    sb1.append(bnd);
                    sb1.append("</td><td>");
                    sb1.append(req);
                    sb1.append("</td><td>");
                    sb1.append(req - bnd);
                    sb1.append("</td><td>");
                    if (ia == null) {
                        sb1.append("Unbound");
                    } else {
                        sb1.append(cs.client.getInetAddress().getHostAddress());
                    }
                    sb1.append("</td><td>");
                    if (cs.client.currentURI == null) {
                        sb1.append('-');
                    } else {
                        String u = cs.client.currentURI.toString();
                        edu.hkust.clap.monitor.Monitor.loopBegin(458);
for (int i = 0; i < u.length(); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(458);
{
                            char ch = u.charAt(i);
                            switch(ch) {
                                case '<':
                                    sb1.append("&lt;");
                                    break;
                                case '>':
                                    sb1.append("&gt;");
                                    break;
                                case '&':
                                    sb1.append("&amp;");
                                    break;
                                default:
                                    sb1.append(ch);
                                    break;
                            }
                        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(458);

                    }
                    sb1.append("</td></tr>\n");
                }
                used++;
                bndc += bnd;
                reqc += req;
            }
            cs = cs.csnext;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(457);

        if (debugstats) {
            sb1.append("</table>\n");
        }
        cs = (SocketClientState) idleList.getHead();
        if (debugstats) {
            sb1.append("<table border=\"1\" class=\"idle\">\n<caption>Idle " + "Clients</caption><tr><th>Id<th>BindCount<th>ReqCount" + "<th>Diff<th>BoundTo</tr>\n");
        }
        edu.hkust.clap.monitor.Monitor.loopBegin(459);
while (cs != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(459);
{
            InetAddress ia = cs.client.getInetAddress();
            idle++;
            bnd = cs.client.getBindCount();
            req = cs.client.getRequestCount();
            if (debugstats) {
                sb1.append("<tr><td>" + cs.id + "<td>" + bnd + "<td>" + req + "<td>" + (req - bnd) + "<td>" + ((ia == null) ? "Unbound" : cs.client.getInetAddress().getHostAddress()) + "</tr>\n");
            }
            bndc += bnd;
            reqc += req;
            cs = (SocketClientState) idleList.getNext(cs);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(459);

        if (debugstats) {
            sb1.append("</table>\n");
        }
        cs = (SocketClientState) freeList.getHead();
        if (debugstats) {
            sb1.append("<table border=\"1\" class=\"idle\">\n" + "<caption>Free Clients" + "</caption><tr><th>Id<th>BindCount<th>ReqCount<th>" + "Diff</tr>\n");
        }
        edu.hkust.clap.monitor.Monitor.loopBegin(460);
while (cs != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(460);
{
            free++;
            bnd = cs.client.getBindCount();
            req = cs.client.getRequestCount();
            if (debugstats) {
                sb1.append("<tr><td>" + cs.id + "<td>" + bnd + "<td>" + req + "<td>" + (req - bnd) + "\n");
            }
            bndc += bnd;
            reqc += req;
            cs = (SocketClientState) freeList.getNext(cs);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(460);

        if (debugstats) {
            sb1.append("</table>\n");
        }
        sb.append("<table border class=\"thread\">\n<caption>Thread counts" + "</caption><tr><th>free<th>idle<th>used" + "<th>estim<th>total<th>Load</tr>");
        sb.append("<tr><td>");
        sb.append(freeCount);
        sb.append('(');
        sb.append(free);
        sb.append(")</td><td>");
        sb.append(idleCount);
        sb.append('(');
        sb.append(idle);
        sb.append(")</td><td>");
        sb.append(clientCount - freeCount - idleCount);
        sb.append('(');
        sb.append(used);
        sb.append(")</td><td>");
        sb.append(clientEstim);
        sb.append("</td><td>");
        sb.append(clientCount);
        sb.append("</td><td>");
        sb.append(loadavg);
        sb.append("</td></tr></table>\n");
        sb.append("<table border class=\"usage\">\n<caption>Usage</caption>" + "<tr><th>ReqCount<th>BindCount<th>Diff</tr>\n<tr><td>");
        sb.append(reqc);
        sb.append("</td><td>");
        sb.append(bndc);
        sb.append("</td><td>");
        sb.append(reqc - bndc);
        sb.append("</td></tr></table>\n");
        if (debugstats) {
            sb.append(sb1);
        }
        if (debugstats) {
            cs = csList;
            sb.append("<table border=\"1\" class=\"idle\">\n<caption>General" + " Status</caption><tr><th>Id<th>Client<th>" + "Status<th>marked<th>Thread</tr>\n");
            edu.hkust.clap.monitor.Monitor.loopBegin(461);
while (cs != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(461);
{
                sb.append("<tr><td>" + cs.id + "<td>" + ((cs.client == null) ? "None" : "bound") + "<td>");
                switch(cs.status) {
                    case SocketClientState.C_IDLE:
                        sb.append("Idle");
                        break;
                    case SocketClientState.C_BUSY:
                        sb.append("Busy");
                        break;
                    case SocketClientState.C_FREE:
                        sb.append("Free");
                        break;
                    case SocketClientState.C_KILL:
                        sb.append("Kill");
                        break;
                    case SocketClientState.C_FIN:
                        sb.append("Fin");
                        break;
                }
                sb.append("<td>" + cs.marked);
                if (cs.client != null) {
                    sb.append("<td>" + cs.client.thread + "</tr>\n");
                } else {
                    sb.append("<td>No CLient</tr>\n");
                }
                cs = cs.csnext;
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(461);

            sb.append("</table>\n");
        }
        return sb.toString();
    }

    /**
     * Some property have changed, update our setting.
     * @param name The name of the property that has changed.
     * @return A boolean, <strong>true</strong> if we updated ourself 
     *    successfully.
     */
    public boolean propertyChanged(String name) {
        httpd s = server;
        if (name.equals(MINSPARE_FREE_P)) {
            minFree = props.getInteger(MINSPARE_FREE_P, minFree);
        } else if (name.equals(MAXSPARE_FREE_P)) {
            maxFree = props.getInteger(MAXSPARE_FREE_P, maxFree);
        } else if (name.equals(MAXSPARE_IDLE_P)) {
            maxIdle = props.getInteger(MAXSPARE_IDLE_P, maxIdle);
        } else if (name.equals(MAXTHREADS_P)) {
            int maxThreads = props.getInteger(MAXTHREADS_P, -1);
            if (maxThreads > 0) threadcache.setCachesize(maxThreads);
        } else if (name.equals(IDLETO_P)) {
            int idleto = props.getInteger(IDLETO_P, -1);
            if (idleto > 0) {
                threadcache.setIdleTimeout(idleto);
            }
        } else if (name.equals(MAXCLIENTS_P)) {
            int newmax = props.getInteger(MAXCLIENTS_P, -1);
            if (newmax > maxClients) {
                edu.hkust.clap.monitor.Monitor.loopBegin(462);
for (int i = maxClients - newmax; --i >= 0; ) { 
edu.hkust.clap.monitor.Monitor.loopInc(462);
addClient(true);} 
edu.hkust.clap.monitor.Monitor.loopEnd(462);

            } else if (newmax > 0) {
                maxClients = newmax;
            }
        } else if (name.equals(BINDADDR_P)) {
            try {
                bindAddr = InetAddress.getByName(props.getString(BINDADDR_P, null));
            } catch (Exception ex) {
            }
        } else if (name.equals(TIMEOUT_P)) {
            timeout = props.getInteger(IDLETO_P, timeout);
        }
        return true;
    }

    /**
     * Remove this client state from the glohbal client list.
     * @param cs The client state to remove from the list.
     */
    protected synchronized void deleteClient(SocketClientState cs) {
        synchronized (csList) {
            if (cs.csprev == null) {
                csList = cs.csnext;
            } else if (cs.csnext == null) {
                cs.csprev.csnext = null;
            } else {
                cs.csprev.csnext = cs.csnext;
                cs.csnext.csprev = cs.csprev;
            }
        }
    }

    /**
     * Factory for creating a new client for this pool.
     * @param server  the target http daemon 
     * @param state  the client state holder
     * @return a new socket client
     */
    protected SocketClient createClient(httpd server, SocketClientState state) {
        return new SocketClient(server, this, state);
    }

    /**
     * Create a new client for this pool.
     * @param free A boolean, if <strong>true</strong> the client is inserted
     * straight into the free list, otherwise, it is not plugged into any
     * list.
     * @return A SocketClientState instance, if creation of a new client was
     * allowed, <strong>null</strong> if no more clients could be created.
     */
    protected synchronized SocketClientState addClient(boolean free) {
        csList = new SocketClientState(csList);
        SocketClientState cs = csList;
        SocketClient client = createClient(server, cs);
        cs.client = client;
        clientCount++;
        clientEstim++;
        if (free) {
            cs.status = SocketClientState.C_FREE;
            freeList.toHead(cs);
            freeCount++;
        }
        return cs;
    }

    /**
     * We are not using synchronized functions to speed up things, 
     * but it is sometime useful to check that clients are not in a bad
     * shape
     */
    private final void checkDeadClients() {
        SocketClientState cs = null;
        cs = csList;
        boolean check = true;
        int idlecount = 0;
        int freecount = 0;
        int clientcount = 0;
        edu.hkust.clap.monitor.Monitor.loopBegin(463);
while (cs != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(463);
{
            if (cs.client != null) {
                clientcount++;
                switch(cs.status) {
                    case SocketClientState.C_BUSY:
                        if (cs.client.thread == null) {
                            if (cs.marked) {
                                if (clientEstim <= maxClients) {
                                    cs.marked = false;
                                    ++freeCount;
                                    updateLoadAverage();
                                    freeList.toHead(cs);
                                    cs.status = SocketClientState.C_FREE;
                                    cs.client.done = true;
                                }
                                check = false;
                            } else {
                                cs.marked = true;
                            }
                        }
                        break;
                    case SocketClientState.C_FREE:
                        freecount++;
                        cs.marked = false;
                        break;
                    default:
                        cs.marked = false;
                        break;
                }
            }
            cs = cs.csnext;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(463);

        if (freecount != freeCount) {
            cs = (SocketClientState) idleList.getHead();
            edu.hkust.clap.monitor.Monitor.loopBegin(464);
while (cs != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(464);
{
                idlecount++;
                cs = (SocketClientState) idleList.getNext(cs);
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(464);

            cs = (SocketClientState) freeList.getHead();
            freecount = 0;
            edu.hkust.clap.monitor.Monitor.loopBegin(465);
while (cs != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(465);
{
                freecount++;
                cs = (SocketClientState) freeList.getNext(cs);
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(465);

            freeCount = freecount;
            idleCount = idlecount;
        }
    }

    /**
     * Update our idea of the current load.
     * The one important invariant here, is that whenever the free list
     * becomes empty, then the load average should be equals to
     * <strong>AVG_DEAD</strong>. This ensures that the server will start 
     * dropping connections.
     *
     */
    private final void updateLoadAverage() {
        int oldavg = loadavg;
        if (freeCount >= maxFree) {
            loadavg = AVG_LIGHT;
        } else if ((freeCount >= minFree) || (idleCount >= maxIdle)) {
            if ((loadavg = AVG_NORMAL) < oldavg) {
                server.thread.setPriority(Thread.MAX_PRIORITY);
            }
        } else if (freeCount > 0) {
            if ((loadavg = AVG_HIGH) > oldavg) {
                server.thread.setPriority(server.getClientThreadPriority() - 2);
            }
        } else {
            loadavg = AVG_DEAD;
        }
    }

    private final synchronized void incrClientCount() {
        ++clientCount;
        ++clientEstim;
        updateLoadAverage();
    }

    private final synchronized void decrClientCount() {
        --clientCount;
        updateLoadAverage();
    }

    private final synchronized boolean incrFreeCount() {
        if (clientEstim > maxClients) {
            clientEstim--;
            return false;
        }
        ++freeCount;
        updateLoadAverage();
        return true;
    }

    private final synchronized boolean decrFreeCount() {
        if (freeCount > 0) {
            --freeCount;
            updateLoadAverage();
            return true;
        } else {
            return false;
        }
    }

    private final synchronized boolean incrIdleCount() {
        if ((loadavg > AVG_HIGH) || (idleCount + 1 >= maxIdle)) return false;
        ++idleCount;
        updateLoadAverage();
        return true;
    }

    private final synchronized boolean decrIdleCount() {
        if (idleCount > 0) {
            --idleCount;
            updateLoadAverage();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes an idle client from the list, updates only the idle list
     * as the free count has already be accessed
     * @param the socket client to remove from the idle list
     */
    protected boolean idleClientRemove(SocketClient client) {
        if (!alive) return false;
        SocketClientState cs = client.state;
        synchronized (csList) {
            switch(cs.status) {
                case SocketClientState.C_IDLE:
                    decrIdleCount();
                    idleList.remove(cs);
                    cs.status = SocketClientState.C_FREE;
                    break;
                case SocketClientState.C_KILL:
                case SocketClientState.C_BUSY:
                default:
                    break;
            }
        }
        return true;
    }

    /**
     * Notify that this client has finished with its connection.
     * If the pool wants the client to be freed (because it has too many of 
     * them), it makes the client kill itself (which will trigger a call to
     * the clientFinished method, were enventual cleanup is performed).
     * @param client The client that is done with its connection.
     */
    protected boolean clientConnectionFinished(SocketClient client) {
        if (!alive) return false;
        SocketClientState cs = client.state;
        synchronized (csList) {
            switch(cs.status) {
                case SocketClientState.C_IDLE:
                    decrIdleCount();
                    idleList.remove(cs);
                    break;
                case SocketClientState.C_BUSY:
                case SocketClientState.C_KILL:
                    break;
                case SocketClientState.C_FREE:
                    if (client.done) {
                        client.done = false;
                        return true;
                    }
                default:
                    break;
            }
            if (incrFreeCount()) {
                if (debug) System.out.println(client + ": now free.");
                cs.status = SocketClientState.C_FREE;
                freeList.toHead(cs);
                return true;
            } else {
                if (debug) System.out.println(client + ": terminate.");
                return false;
            }
        }
    }

    /**
     * Notify that this client has been killed.
     * @param client The client that has terminate.
     */
    protected void clientFinished(SocketClient client) {
        if (!alive) return;
        SocketClientState cs = client.state;
        synchronized (csList) {
            if (debug) System.out.println(client + ": finished " + cs.status);
            switch(cs.status) {
                case SocketClientState.C_IDLE:
                    break;
                case SocketClientState.C_FREE:
                    decrFreeCount();
                    freeList.remove(cs);
                    break;
                case SocketClientState.C_BUSY:
                default:
                    String msg = (client + ": finished with unknown status " + cs.status);
                    server.errlog(msg);
                    break;
            }
            cs.status = SocketClientState.C_FIN;
            decrClientCount();
            deleteClient(cs);
        }
    }

    /**
     * The client notifies the pool that is has been activated.
     * The client state object is updated to unmark the client as idle.
     * <p>This method needs not be synchronized, as it affect only the client
     * state, <em>not</em> the client list.
     * @param client The activated client.
     */
    protected void notifyUse(SocketClient client) {
        if (debug) System.out.println(client + ": used.");
        SocketClientState cs = client.state;
        synchronized (csList) {
            if (cs.status == SocketClientState.C_IDLE) {
                decrIdleCount();
                idleList.remove(cs);
            }
            cs.status = SocketClientState.C_BUSY;
        }
    }

    /**
     * The client notifies the pool that it enters idle state.
     * <p>This method needs not be synchronized, as it affect only the client
     * state, <em>not</em> the client list.
     * @param client The client that is going to be idle.
     */
    protected boolean notifyIdle(SocketClient client) {
        SocketClientState cs = client.state;
        if (alive) {
            synchronized (csList) {
                if (incrIdleCount()) {
                    if (debug) System.out.println(client + ": idle, keep-alive.");
                    cs.status = SocketClientState.C_IDLE;
                    idleList.toHead(cs);
                    return true;
                } else {
                    if (debug) System.out.println(client + ": idle, closed.");
                    int killsome = Math.max((maxFree - freeCount), (maxIdle - idleCount));
                    killSomeClients((killsome > 0) ? killsome : 1);
                    if (incrIdleCount()) {
                        if (debug) System.out.println(client + ": idle, keep-alive.");
                        cs.status = SocketClientState.C_IDLE;
                        idleList.toHead(cs);
                        return true;
                    }
                    return false;
                }
            }
        } else {
            if (debug) System.out.println(client + ": idle (dead), closed.");
            int killsome = Math.max((maxFree - freeCount), (maxIdle - idleCount));
            killSomeClients((killsome > 0) ? killsome : 1);
            return false;
        }
    }

    protected void killSomeClients(int howmany) {
        int count = (howmany > 0) ? howmany : Math.max((maxFree - freeCount), (maxIdle - idleCount));
        if (debug) {
            System.out.println("Killing :" + howmany);
        }
        edu.hkust.clap.monitor.Monitor.loopBegin(466);
while (--count >= 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(466);
{
            SocketClientState cs = (SocketClientState) idleList.removeTail();
            if (cs != null) {
                synchronized (csList) {
                    if (cs.status == SocketClientState.C_IDLE) {
                        if (debug) System.out.println(cs.client + ": kill (some-client).");
                        decrIdleCount();
                        cs.status = SocketClientState.C_KILL;
                        cs.client.unbind();
                    }
                }
            } else {
                break;
            }
            if ((freeCount > minFree) && (idleCount < maxIdle)) {
                break;
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(466);

    }

    protected final void killSomeClients() {
        killSomeClients(-1);
    }

    protected void run(SocketClient client) {
        if (debug) System.out.println(client + ": warming up...");
        boolean threaded = threadcache.getThread(client, true);
        if (debug) System.out.println(client + ": threaded=" + threaded);
    }

    /**
     * Handle the given connection.
     * Find a free client, bind it to the given socket, and run it. If we
     * have reached our maximum allowed number of clients, kill some
     * connections.
     * <p>A client enters the LRU list (and become a candidate for kill) only
     * after it has handle one request (or if some timeout expires). This is
     * performed by the first call to <code>notifyUse</code> which will
     * silently insert the client into the LRU if it was not there already.
     * <p>This client pool does a lot of nice thinigs, but could probably be
     * implemented in a much better way (while keeping the features it has).
     * Contention arond the pool is probably concern number 1 of performances.
     * @param socket The connection to handle.
     */
    public void handleConnection(Socket socket) {
        if (debug) System.out.println("new connection.");
        SocketClientState cs = null;
        switch(loadavg) {
            case AVG_LIGHT:
                if (decrFreeCount()) {
                    cs = (SocketClientState) freeList.removeTail();
                    if (cs == null) {
                        edu.hkust.clap.monitor.Monitor.loopBegin(467);
while (!incrFreeCount()) { 
edu.hkust.clap.monitor.Monitor.loopInc(467);
{
                        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(467);

                    }
                }
                break;
            case AVG_NORMAL:
            case AVG_HIGH:
                killSomeClients();
                if (decrFreeCount()) {
                    cs = (SocketClientState) freeList.removeTail();
                    if (cs == null) {
                        edu.hkust.clap.monitor.Monitor.loopBegin(467);
while (!incrFreeCount()) { 
edu.hkust.clap.monitor.Monitor.loopInc(467);
{
                        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(467);

                    }
                }
                break;
            case AVG_DEAD:
                break;
        }
        if (debug) System.out.println("load " + loadavg + ", client=" + ((cs != null) ? cs.client.toString() : "unbound"));
        if (cs != null) {
            if (debug) System.out.println(cs.client + ": bound.");
            cs.status = SocketClientState.C_BUSY;
            cs.client.bind(socket);
        } else {
            if (debug) System.out.println("*** connection refused (overloaded).");
            try {
                socket.close();
            } catch (IOException ex) {
            }
            server.errlog(socket.getInetAddress() + " refused (overloaded).");
        }
        return;
    }

    protected synchronized void killClients(boolean force) {
        alive = false;
        SocketClientState cs = csList;
        edu.hkust.clap.monitor.Monitor.loopBegin(468);
while ((cs != null) && (cs.client != null)) { 
edu.hkust.clap.monitor.Monitor.loopInc(468);
{
            synchronized (csList) {
                cs.client.kill(cs.status == SocketClientState.C_IDLE);
            }
            cs = cs.csnext;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(468);

        try {
            Thread.sleep(5000);
        } catch (Exception ex) {
        }
        cs = csList;
        edu.hkust.clap.monitor.Monitor.loopBegin(469);
while ((cs != null) && (cs.client != null)) { 
edu.hkust.clap.monitor.Monitor.loopInc(469);
{
            synchronized (csList) {
                cs.client.kill(true);
            }
            cs = cs.csnext;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(469);

    }

    /**
     * Shutdown the client pool. 
     * If force is <strong>true</strong>, kill all running clients right
     * now, otherwise, wait for them to terminate gracefully, and return
     * when done.
     * @param force Should we interrupt running clients.
     */
    public void shutdown(boolean force) {
        killClients(force);
        SocketClientState cs = csList;
        edu.hkust.clap.monitor.Monitor.loopBegin(470);
while ((cs != null) && (cs.client != null)) { 
edu.hkust.clap.monitor.Monitor.loopInc(470);
{
            if (debug) System.out.println(cs.client + ": join.");
            cs.client.join();
            cs = cs.csnext;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(470);

        props.unregisterObserver(this);
        props = null;
        csList = null;
        freeList = null;
        idleList = null;
        server = null;
    }

    /**
     * Create the master socket for this client factory.
     * @exception IOException If some IO error occurs while creating the
     * server socket.
     * @return A ServerSocket instance.
     */
    public ServerSocket createServerSocket() throws IOException {
        if (bindAddr == null) {
            return new ServerSocket(server.getPort(), Math.max(128, maxClients));
        } else {
            return new ServerSocket(server.getPort(), Math.max(128, maxClients), bindAddr);
        }
    }

    /**
     * Initialize the raw, client socket factory.
     * @param server The server context we are attached to.
     */
    public void initialize(httpd server) {
        this.server = server;
        this.props = server.getProperties();
        this.props.registerObserver(this);
        PropertySet set = new SocketConnectionProp("SocketConnectionProp", server);
        server.registerPropertySet(set);
        this.minFree = props.getInteger(MINSPARE_FREE_P, MINSPARE_FREE);
        this.maxFree = props.getInteger(MAXSPARE_FREE_P, MAXSPARE_FREE);
        this.maxIdle = props.getInteger(MAXSPARE_IDLE_P, MAXSPARE_IDLE);
        this.maxClients = props.getInteger(MAXCLIENTS_P, MAXCLIENTS);
        this.timeout = props.getInteger(TIMEOUT_P, IDLETO);
        String bindAddrName = props.getString(BINDADDR_P, null);
        if (bindAddrName != null) {
            try {
                bindAddr = InetAddress.getByName(bindAddrName);
            } catch (Exception ex) {
            }
        }
        idleList = new SyncLRUList();
        freeList = new SyncLRUList();
        csList = new SocketClientState();
        edu.hkust.clap.monitor.Monitor.loopBegin(471);
for (int i = 0; i < maxClients; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(471);
{
            if (addClient(true) == null) throw new RuntimeException(this.getClass().getName() + "[construstructor]" + ": unable to create clients.");
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(471);

        threadcache = new ThreadCache(server.getIdentifier() + "-socket-clients");
        threadcache.setCachesize(props.getInteger(MAXTHREADS_P, MAXTHREADS));
        threadcache.setThreadPriority(server.getClientThreadPriority());
        threadcache.setIdleTimeout(props.getInteger(IDLETO_P, IDLETO));
        threadcache.setGrowAsNeeded(true);
        threadcache.initialize();
        if (debugthread) {
            new DebugThread(this).start();
        }
    }

    /**
     * Empty constructor for dynamic class instantiation.
     */
    public SocketClientFactory() {
    }
}
