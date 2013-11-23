package org.w3c.www.http;

/**
 * Invalid access to an HTTP message header.
 * Invalid access to HTTP message headers can take several forms.
 */
public class HttpAccessException extends RuntimeException {

    /**
     * Invalid header index.
     * @param idx The faulty header index.
     */
    public HttpAccessException(int idx) {
        super(idx + ": invalid header index.");
    }

    /**
     * Invalid header name.
     * @param name The name of the unknown header.
     */
    public HttpAccessException(String name) {
        super(name + ": invalid header name.");
    }
}