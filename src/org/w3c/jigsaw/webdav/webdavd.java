package org.w3c.jigsaw.webdav;

import org.w3c.jigsaw.http.httpd;
import org.w3c.jigsaw.http.Client;
import org.w3c.www.mime.MimeParserFactory;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class webdavd extends httpd {

    protected MimeParserFactory getMimeClientFactory(Client client) {
        return new DAVMimeClientFactory(client);
    }
}
