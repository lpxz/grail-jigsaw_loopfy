package org.w3c.www.protocol.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import org.w3c.www.mime.MimeType;
import org.w3c.www.http.HeaderDescription;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;

public class HttpURLConnection extends java.net.HttpURLConnection {

    protected Request request = null;

    protected Reply reply = null;

    protected ByteArrayOutputStream output = null;

    protected final synchronized void checkRequest() {
        if (request == null) {
            request = HttpManager.getManager().createRequest();
            request.setMethod(method);
            if (ifModifiedSince > 0) request.setIfModifiedSince(ifModifiedSince * 1000);
            if (!useCaches) {
                request.addPragma("no-cache");
                request.setNoCache();
            }
            request.setURL(url);
        }
    }

    protected final void checkReply() {
        if ((reply == null) && connected) {
            error("Was connected but didn't get any reply !");
        } else if (!connected) {
            try {
                connect();
            } catch (IOException ex) {
                ex.printStackTrace();
                error("Error while silently connecting.");
            }
        }
    }

    protected void error(String msg) {
        debug("ERROR \"" + msg + "\"");
        throw new RuntimeException(msg);
    }

    protected final void debug(String m) {
    }

    public boolean usingProxy() {
        return HttpManager.getManager().usingProxy();
    }

    public synchronized void disconnect() {
        if (request != null) request.interruptRequest();
    }

    public void connect() throws IOException {
        debug("connect");
        if (connected) return;
        checkRequest();
        if (doOutput) {
            byte data[] = output.toByteArray();
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            request.setOutputStream(in);
            if (!request.hasHeader(Request.H_CONTENT_LENGTH)) request.setContentLength(data.length);
            if (!request.hasHeader(Request.H_CONTENT_TYPE)) {
                MimeType t = (request.getMethod().equals("POST") ? MimeType.APPLICATION_X_WWW_FORM_URLENCODED : MimeType.TEXT_PLAIN);
                request.setContentType(t);
            }
        }
        try {
            int redirects = 0;
            edu.hkust.clap.monitor.Monitor.loopBegin(206);
            redir: 
while (redirects < 5) { 
edu.hkust.clap.monitor.Monitor.loopInc(206);
{
                if (redirects == 1) {
                    request.setOutputStream(null);
                    request.setContentLength(-1);
                    request.setMethod("GET");
                }
                reply = request.getManager().runRequest(request);
                if ((reply.getStatus() / 100) == 3) {
                    String reloc = reply.getLocation();
                    if (reloc == null) break redir;
                    reply.getInputStream().close();
                    URL orig = request.getURL();
                    try {
                        url = new URL(orig, reloc);
                        request.setURL(url);
                    } catch (MalformedURLException ex) {
                        break redir;
                    }
                } else {
                    break redir;
                }
                if (!getFollowRedirects()) break redir;
                redirects++;
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(206);

        } catch (HttpException ex) {
            ex.printStackTrace();
            throw new IOException(ex.getMessage());
        }
        connected = true;
    }

    public int getResponseCode() throws IOException {
        checkReply();
        return reply.getStatus();
    }

    public int getContentLength() {
        checkReply();
        return reply.getContentLength();
    }

    public String getContentType() {
        checkReply();
        MimeType t = reply.getContentType();
        if (t == null) {
            return null;
        }
        return t.toString();
    }

    public long getExpiration() {
        checkReply();
        return reply.getExpires();
    }

    public long getDate() {
        checkReply();
        return reply.getDate();
    }

    public long getLastModified() {
        checkReply();
        return reply.getLastModified();
    }

    public String getHeaderField(String name) {
        checkReply();
        return reply.getValue(name);
    }

    public String getHeaderFieldKey(int n) {
        Enumeration e = reply.enumerateHeaderDescriptions(true);
        HeaderDescription d = null;
        edu.hkust.clap.monitor.Monitor.loopBegin(207);
while (--n >= 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(207);
{
            if (e.hasMoreElements()) d = (HeaderDescription) e.nextElement();
            return null;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(207);

        if (d != null) return d.getName();
        return null;
    }

    public String getHeaderField(int n) {
        String key = getHeaderFieldKey(n);
        if (key != null) return reply.getValue(key);
        return null;
    }

    public InputStream getInputStream() throws IOException {
        debug("getInputStream");
        checkReply();
        return reply.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        debug("getOutputStream");
        if ((!connected) && (!doOutput)) {
            setDoOutput(true);
        } else if (connected) {
            error("Already connected, too late for getOutputStream.");
        }
        output = new ByteArrayOutputStream();
        return output;
    }

    public InputStream getErrorStream() {
        debug("getErrorStream");
        try {
            int errorCat = reply.getStatus() / 100;
            if (connected && (errorCat == 4 || errorCat == 5)) {
                return reply.getInputStream();
            }
        } catch (Exception ex) {
            return null;
        }
        return null;
    }

    public void setRequestProperty(String key, String value) {
        checkRequest();
        request.setValue(key, value);
    }

    public String getRequestProperty(String key) {
        checkRequest();
        return request.getValue(key);
    }

    public static void setGlobalRequestProperty(String key, String value) {
        HttpManager.getManager().setGlobalHeader(key, value);
    }

    public static String getDefaultRequestProperty(String key) {
        return HttpManager.getManager().getGlobalHeader(key);
    }

    public void setDoOutput(boolean doinput) {
        debug("DoOutput !!");
        super.setDoOutput(doinput);
        checkRequest();
        if (request.getContentType() == null) request.setContentType(MimeType.APPLICATION_X_WWW_FORM_URLENCODED);
    }

    HttpURLConnection(URL u) {
        super(u);
    }
}
