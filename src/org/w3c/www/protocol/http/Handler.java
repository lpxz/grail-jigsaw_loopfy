package org.w3c.www.protocol.http;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.io.IOException;

public class Handler extends URLStreamHandler {

    protected URLConnection openConnection(URL u) throws IOException {
        return new HttpURLConnection(u);
    }
}
