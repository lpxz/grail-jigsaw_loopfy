package org.w3c.www.protocol.http;

import org.w3c.util.LRUAble;

abstract class HttpConnection implements LRUAble {

    protected LRUAble lru_next = null;

    protected LRUAble lru_prev = null;

    /**
     * The server this connection is attached to.
     */
    protected HttpServer server = null;

    /**
     * state if the connection was cached or not.
     */
    protected boolean cached = false;

    /**
     * LRUAble interface - Get previous item in the LRU list.
     * @return The previous item, or <strong>null</strong>.
     */
    public LRUAble getNext() {
        return lru_next;
    }

    /**
     * LRUAble interface - Get next item in the LRU list.
     * @return The next item, or <strong>null</strong>.
     */
    public LRUAble getPrev() {
        return lru_prev;
    }

    /**
     * LRUAble interface - Set the next item for this server.
     */
    public void setNext(LRUAble next) {
        lru_next = next;
    }

    /**
     * LRUAble interface - Set the previous item for this server.
     */
    public void setPrev(LRUAble prev) {
        lru_prev = prev;
    }

    protected final HttpServer getServer() {
        return server;
    }

    public abstract void close();

    /**
     * Can this connection be reused as a first choice when requested?
     * This is only a hint, as if all connections fail, the first one will
     * be forced by default
     * @return a boolean, true by default
     */
    protected boolean mayReuse() {
        return true;
    }
}
