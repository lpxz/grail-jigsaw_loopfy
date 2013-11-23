package org.w3c.tools.resources.store;

/**
 * This is exception gets thrown if an invalid resource store is detected.
 */
public class NotAStoreException extends Exception {

    public NotAStoreException(String msg) {
        super(msg);
    }
}
