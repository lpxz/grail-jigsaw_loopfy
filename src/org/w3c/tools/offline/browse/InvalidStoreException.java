package org.w3c.tools.offline.browse;

/**
 * The store is not valid (missing repository file...) .
 */
public class InvalidStoreException extends Exception {

    public InvalidStoreException(String msg) {
        super("reading store failed: " + msg);
    }
}
