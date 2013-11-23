package org.w3c.jigsaw.http;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public interface httpdPreloadInterface {

    /**
     * Perform some actions on the server just before startup
     * @param server the http server
     */
    public void preload(httpd server);
}
