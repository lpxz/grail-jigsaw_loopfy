package org.w3c.www.protocol.http.icp;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.URL;
import org.w3c.www.protocol.http.HttpManager;
import org.w3c.www.protocol.http.PropRequestFilterException;
import org.w3c.www.protocol.http.cache.CacheFilter;

class ICPReceiver extends Thread implements ICP {

    private static final boolean debug = false;

    /**
     * The default waiter queue size.
     */
    public static final int DEFAULT_QUEUE_SIZE = 4;

    /**
     * The default received datagram packet size.
     */
    public static final int DEFAULT_PACKET_SIZE = 512;

    /**
     * The port number this receiver listens on.
     */
    protected int port = -1;

    /**
     * Our socket to receive packets.
     */
    DatagramSocket socket = null;

    /**
     * Our current request identifier.
     */
    protected int nextid = 1;

    /**
     * The CacheFilter we use for answering queries.
     */
    CacheFilter cache = null;

    /**
     * The queue of objects waiting for an ICP reply.
     */
    ICPWaiter queue[] = null;

    /**
     * The ICP filter we are working with.
     */
    protected ICPFilter filter = null;

    protected DatagramSocket getSocket() {
        return socket;
    }

    /**
     * Create a new ICP query instance.
     * @param url The URL to be queried.
     */
    protected ICPQuery createQuery(URL url) {
        int rid = -1;
        synchronized (this) {
            rid = nextid++;
        }
        return new ICPQuery(rid, url);
    }

    /**
     * Add a waiter for on the given request identifier.
     * @param waiter The ICPWaiter instance for that request.
     */
    protected synchronized void addReplyWaiter(ICPWaiter waiter) {
        if (queue == null) {
            queue = new ICPWaiter[DEFAULT_QUEUE_SIZE];
            queue[0] = waiter;
        } else {
            edu.hkust.clap.monitor.Monitor.loopBegin(764);
for (int i = 0; i < queue.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(764);
{
                if (queue[i] == null) {
                    queue[i] = waiter;
                    return;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(764);

            ICPWaiter nqueue[] = new ICPWaiter[queue.length << 1];
            System.arraycopy(queue, 0, nqueue, 0, queue.length);
            nqueue[queue.length] = waiter;
            queue = nqueue;
            return;
        }
    }

    /**
     * Remove the given waiter from the waiters queue.
     * This waiter has completed his job, he doesn't care about what happens
     * next at the ICP level.
     * @param waiter The wauter to remove from our queue.
     */
    protected synchronized void removeReplyWaiter(ICPWaiter waiter) {
        if (queue != null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(765);
for (int i = 0; i < queue.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(765);
{
                if (queue[i] == waiter) {
                    queue[i] = null;
                    return;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(765);

        }
    }

    /**
     * Handle the given ICP reply.
     */
    protected synchronized void handleReply(ICPReply reply) throws ICPProtocolException {
        int id = reply.getIdentifier();
        edu.hkust.clap.monitor.Monitor.loopBegin(766);
for (int i = 0; i < queue.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(766);
{
            if (queue[i] == null) continue;
            if (queue[i].getIdentifier() == id) {
                queue[i].notifyReply(reply);
                return;
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(766);

        if (debug) System.out.println("icp: discarding reply " + id);
        return;
    }

    /**
     * Handle the given ICP query.
     * @param p The DatagramPacket that wraps up the query.
     */
    protected synchronized void handleQuery(ICPQuery query) throws ICPProtocolException {
        if (debug) System.out.println("icp[" + port + "]: query for " + query.getURL() + " from " + query.getSenderAddress() + "/" + query.getSenderPort());
        boolean hit = false;
        ICPReply reply = new ICPReply(query.getIdentifier(), hit ? ICP_OP_HIT : ICP_OP_MISS);
        ICPSender sender = filter.getSender(query.getSenderAddress(), query.getSenderPort());
        if (sender != null) {
            if (debug) System.out.println("icp[" + port + "]: reports " + reply.getOpcode() + " for " + reply.getIdentifier() + " to " + sender);
            sender.send(reply);
        } else {
            if (debug) System.out.println("icp[" + port + "]: couldn't locate peer at " + query.getSenderAddress() + "/" + query.getSenderPort());
        }
    }

    /**
     * Run the ICP manager.
     */
    public void run() {
        byte pbuf[] = new byte[DEFAULT_PACKET_SIZE];
        readloop: 
while (true) { 
{
            DatagramPacket p = new DatagramPacket(pbuf, pbuf.length);
            try {
                socket.receive(p);
            } catch (IOException ex) {
                ex.printStackTrace();
                continue readloop;
            }
            try {
                ICPMessage m = ICPMessage.parse(p);
                if (m instanceof ICPQuery) handleQuery((ICPQuery) m); else handleReply((ICPReply) m);
            } catch (ICPProtocolException ex) {
                ex.printStackTrace();
            }
        }} 

    }

    ICPReceiver(HttpManager manager, ICPFilter filter, int port) throws SocketException, PropRequestFilterException {
        setName("ICP-Receiver");
        setDaemon(true);
        this.port = port;
        this.filter = filter;
        this.socket = new DatagramSocket(port);
        try {
            Class c = Class.forName("org.w3c.www.protocol.http.cache.CacheFilter");
            cache = (CacheFilter) manager.getGlobalFilter(c);
        } catch (Exception ex) {
        }
        if (cache == null) throw new PropRequestFilterException("no cache filter.");
        start();
    }
}
