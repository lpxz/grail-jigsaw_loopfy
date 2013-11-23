package org.w3c.www.http;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class HttpExtException extends RuntimeException {

    protected HttpExtException(String msg) {
        super(msg);
    }

    protected HttpExtException() {
        super();
    }
}
