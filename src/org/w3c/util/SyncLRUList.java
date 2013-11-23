package org.w3c.util;

public class SyncLRUList extends LRUList {

    public synchronized void toHead(LRUAble node) {
        _remove(node);
        node.setNext(head.next);
        node.setPrev(head);
        head.next.setPrev(node);
        head.next = node;
    }

    public synchronized void toTail(LRUAble node) {
        _remove(node);
        node.setPrev(tail.prev);
        node.setNext(tail);
        tail.prev.setNext(node);
        tail.prev = node;
    }

    private final synchronized void _remove(LRUAble node) {
        LRUAble itsPrev, itsNext;
        itsPrev = node.getPrev();
        if (itsPrev == null) return;
        itsNext = node.getNext();
        itsPrev.setNext(itsNext);
        itsNext.setPrev(itsPrev);
    }

    public final synchronized LRUAble remove(LRUAble node) {
        _remove(node);
        node.setNext((LRUAble) null);
        node.setPrev((LRUAble) null);
        return node;
    }

    public final synchronized LRUAble getTail() {
        LRUAble prev = tail.prev;
        return (prev == head) ? null : prev;
    }

    public final synchronized LRUAble getHead() {
        LRUAble next = head.next;
        return (next == tail) ? null : next;
    }

    public final synchronized LRUAble removeTail() {
        if (tail.prev != head) return remove(tail.prev);
        return null;
    }

    public final synchronized LRUAble getNext(LRUAble node) {
        LRUAble next = node.getNext();
        return ((next == tail) || (next == head)) ? null : next;
    }

    public final synchronized LRUAble getPrev(LRUAble node) {
        LRUAble prev = node.getPrev();
        return ((prev == tail) || (prev == head)) ? null : prev;
    }
}
