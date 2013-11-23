package org.w3c.tools.resources;

/**
 * Exception throw when unable to restore an attribute holder.
 */
public class HolderInitException extends RuntimeException {

    public HolderInitException(String msg) {
        super(msg);
    }
}
