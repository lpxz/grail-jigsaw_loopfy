package org.w3c.www.protocol.http.icp;

import java.util.Vector;

class ICPWaiter {

    /**
     * The identifier this waiter is waitin for.
     */
    protected int id = -1;

    /**
     * The queue of replies, as they arrive:
     */
    protected Vector replies = null;

    /**
     * Get the identifier this waiter is waiting on.
     * @return The integer identifier.
     */
    protected final int getIdentifier() {
        return id;
    }

    /**
     * Get next matching reply until timeout expires.
     * @param timeout The timeout to wait until filaure.
     * @return A ICPReply instance, if available, or <strong>null</strong>
     * if timeout has expired.
     */
    protected synchronized ICPReply getNextReply(long timeout) {
        if (replies.size() > 0) {
            ICPReply reply = (ICPReply) replies.elementAt(0);
            replies.removeElementAt(0);
            return reply;
        }
        try {
            wait(timeout);
        } catch (InterruptedException ex) {
        }
        if (replies.size() == 0) return null;
        ICPReply reply = (ICPReply) replies.elementAt(0);
        replies.removeElementAt(0);
        return reply;
    }

    /**
     * Notify that waiter that a matching reply was received.
     * @param reply The matching ICP reply.
     */
    protected synchronized void notifyReply(ICPReply reply) {
        replies.addElement(reply);
        notifyAll();
    }

    ICPWaiter(int id) {
        this.replies = new Vector(4);
        this.id = id;
    }
}
