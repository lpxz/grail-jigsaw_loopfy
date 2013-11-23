package org.w3c.util;

public class AsyncLRUList extends LRUList {

    public final synchronized void toHead(LRUAble node) {
        _remove(node);
        if (head.next != null) {
            node.setNext(head.next);
            head.next.setPrev(node);
            node.setPrev(head);
            head.next = node;
        } else {
            node.setNext(head.next);
            node.setPrev(head);
            head.next = node;
        }
    }

    public final synchronized void toTail(LRUAble node) {
        _remove(node);
        if (tail.prev != null) {
            node.setPrev(tail.prev);
            tail.prev.setNext(node);
            node.setNext(tail);
            tail.prev = node;
        } else {
            node.setPrev(tail.prev);
            node.setNext(tail);
            tail.prev = node;
        }
    }

    private final synchronized void _remove(LRUAble node) {
        LRUAble itsPrev = node.getPrev();
        if (itsPrev == null) return;
        LRUAble itsNext = node.getNext();
        node.setNext(null);
        node.setPrev(null);
        itsPrev.setNext(itsNext);
        if (itsNext == null) return;
        itsNext.setPrev(itsPrev);
    }

    public final synchronized LRUAble remove(LRUAble node) {
        _remove(node);
        node.setNext((LRUAble) null);
        node.setPrev((LRUAble) null);
        return node;
    }

    public final synchronized LRUAble getTail() {
        if (tail.prev == null) return null;
        LRUAble prev = tail.prev;
        return (prev == head) ? null : prev;
    }

    public final synchronized LRUAble getHead() {
        LRUAble next = head.next;
        return (next == tail) ? null : next;
    }

    public final synchronized LRUAble removeTail() {
        return (tail.prev != head) ? remove(tail.prev) : null;
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
