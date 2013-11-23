package org.w3c.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * An empty enumeration.
 */
public class EmptyEnumeration implements Enumeration {

    public final boolean hasMoreElements() {
        return false;
    }

    public final Object nextElement() {
        throw new NoSuchElementException("empty enumeration");
    }
}
