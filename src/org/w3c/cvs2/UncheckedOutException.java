package org.w3c.cvs2;

/**
 * This exception is used whenever an abnormal situation in CVS processing
 * is encountered.
 */
public class UncheckedOutException extends CvsException {

    UncheckedOutException(String msg) {
        super(msg);
    }
}
