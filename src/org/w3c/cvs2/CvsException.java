package org.w3c.cvs2;

/**
 * This exception is used whenever an abnormal situation in CVS processing
 * is encountered.
 */
public class CvsException extends Exception {

    CvsException(String msg) {
        super(msg);
    }
}
