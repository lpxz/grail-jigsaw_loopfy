package org.w3c.tools.codec;

/**
 * Exception for invalid BASE64 streams.
 */
public class Base64FormatException extends Exception {

    /**
   * Create that kind of exception
   * @param msg The associated error message 
   */
    public Base64FormatException(String msg) {
        super(msg);
    }
}
