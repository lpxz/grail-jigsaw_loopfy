package org.w3c.util;

public interface LRUAble {

    public LRUAble getNext();

    public LRUAble getPrev();

    public void setNext(LRUAble next);

    public void setPrev(LRUAble prev);
}
