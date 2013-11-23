package org.w3c.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/** Iterates through array skipping nulls. */
public class ArrayEnumeration implements Enumeration {

    private int nelems;

    private int elemCount;

    private int arrayIdx;

    private Object[] array;

    public ArrayEnumeration(Object[] array) {
        this(array, array.length);
    }

    public ArrayEnumeration(Object[] array, int nelems) {
        arrayIdx = elemCount = 0;
        this.nelems = nelems;
        this.array = array;
    }

    public final boolean hasMoreElements() {
        return elemCount < nelems;
    }

    public final Object nextElement() {
        edu.hkust.clap.monitor.Monitor.loopBegin(474);
while (array[arrayIdx] == null && arrayIdx < array.length) { 
edu.hkust.clap.monitor.Monitor.loopInc(474);
arrayIdx++;} 
edu.hkust.clap.monitor.Monitor.loopEnd(474);

        if (arrayIdx >= array.length) throw new NoSuchElementException();
        elemCount++;
        return array[arrayIdx++];
    }
}
