package org.w3c.jigsaw.webdav;

import org.w3c.jigsaw.http.Client;
import org.w3c.www.mime.MimeHeaderHolder;
import org.w3c.www.mime.MimeParser;
import org.w3c.www.mime.MimeParserException;
import org.w3c.www.mime.MimeParserFactory;

/**
 * The Mime factory for creating requests out of the client transport streams.
 * This factory creates instances of 
 * <code>org.w3c.jigsaw.webdav.DAVRequest</code>
 */
class DAVMimeClientFactory implements MimeParserFactory {

    Client client = null;

    public MimeHeaderHolder createHeaderHolder(MimeParser parser) {
        return new DAVRequest(client, parser);
    }

    DAVMimeClientFactory(Client client) {
        this.client = client;
    }
}
