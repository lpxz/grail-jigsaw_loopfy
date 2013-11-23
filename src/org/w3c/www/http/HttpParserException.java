package org.w3c.www.http;

import org.w3c.www.mime.MimeParserException;

public class HttpParserException extends MimeParserException {

    HttpRequestMessage req = null;

    public boolean hasRequest() {
        return (req != null);
    }

    public HttpRequestMessage getRequest() {
        return req;
    }

    public HttpParserException(String msg) {
        super(msg);
    }

    public HttpParserException(String msg, HttpRequestMessage req) {
        super(msg);
        this.req = req;
    }
}
