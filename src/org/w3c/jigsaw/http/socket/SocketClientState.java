package org.w3c.jigsaw.http.socket;

import org.w3c.util.LRUAble;

public class SocketClientState implements LRUAble {

    static final int C_IDLE = 0;

    static final int C_BUSY = 1;

    static final int C_FREE = 2;

    static final int C_KILL = 3;

    static final int C_FIN = 4;

    LRUAble next = null;

    LRUAble prev = null;

    SocketClient client = null;

    int id = 0;

    int status = C_IDLE;

    boolean bound = false;

    boolean marked = false;

    SocketClientState csnext = null;

    SocketClientState csprev = null;

    static int nextid = 0;

    static final synchronized int nextId() {
        return nextid++;
    }

    public final LRUAble getNext() {
        return next;
    }

    public final LRUAble getPrev() {
        return prev;
    }

    public final void setNext(LRUAble next) {
        this.next = next;
    }

    public final void setPrev(LRUAble prev) {
        this.prev = prev;
    }

    SocketClientState(SocketClientState cshead) {
        this.status = C_IDLE;
        this.id = nextId();
        cshead.csprev = this;
        this.csnext = cshead;
    }

    SocketClientState() {
    }
}
