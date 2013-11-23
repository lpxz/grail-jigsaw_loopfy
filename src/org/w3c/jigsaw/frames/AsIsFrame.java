package org.w3c.jigsaw.frames;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.event.AttributeChangedEvent;
import org.w3c.util.CountInputStream;
import org.w3c.www.mime.MimeParser;
import org.w3c.www.mime.MimeParserException;
import org.w3c.www.http.ByteRangeOutputStream;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HeaderDescription;
import org.w3c.www.http.HeaderValue;
import org.w3c.www.http.MimeParserReplyFactory;
import org.w3c.www.http.HttpContentRange;
import org.w3c.www.http.HttpEntityTag;
import org.w3c.www.http.HttpRange;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpFactory;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

/**
 * Read the HTTP reply directly from a file. Like in Apache mod_asis
 * http://httpd.apache.org/docs/mod/mod_asis.html
 * 
 */
public class AsIsFrame extends HTTPFrame {

    int foffset = -1;

    HttpReplyMessage asisreply = null;

    /**
     * Listen its resource.
     */
    public void attributeChanged(AttributeChangedEvent evt) {
        super.attributeChanged(evt);
        asisreply = null;
    }

    /**
     * shadows the call from HTTPFrame
     * It updates the cached headers for the automagic reply generation
     */
    protected void updateCachedHeaders() {
        super.updateCachedHeaders();
        if (asisreply == null) {
            if (fresource != null) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(fresource.getFile());
                } catch (FileNotFoundException fex) {
                }
                CountInputStream cis = new CountInputStream(fis);
                MimeParserReplyFactory repfact = new MimeParserReplyFactory();
                MimeParser mp = new MimeParser(cis, repfact);
                try {
                    asisreply = (HttpReplyMessage) mp.parse();
                } catch (MimeParserException mex) {
                    return;
                } catch (IOException ioex) {
                    return;
                }
                foffset = (int) cis.getBytesRead();
                try {
                    cis.close();
                } catch (IOException ioex) {
                }
                ;
                int cl = fresource.getFileLength();
                cl -= foffset;
                setValue(ATTR_CONTENT_LENGTH, new Integer(cl));
                contentlength = HttpFactory.makeInteger(cl);
                md5Digest = null;
            }
        }
    }

    /**
     * Create a reply to answer to request on this file.
     * This method will create a suitable reply (matching the given request)
     * and will set all its default header values to the appropriate 
     * values.
     * The Status is ignored as it is superceded byt the Asis file
     * @param request The request to make a reply for.
     * @return An instance of Reply, suited to answer this request.
     */
    public Reply createDefaultReply(Request request, int status) {
        if (asisreply == null) {
            updateCachedHeaders();
        }
        if (asisreply == null) {
            return super.createDefaultReply(request, status);
        }
        Reply reply = super.createDefaultReply(request, asisreply.getStatus());
        reply.setReason(asisreply.getReason());
        Enumeration e = asisreply.enumerateHeaderDescriptions();
        edu.hkust.clap.monitor.Monitor.loopBegin(177);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(177);
{
            HeaderDescription d = (HeaderDescription) e.nextElement();
            HeaderValue v = asisreply.getHeaderValue(d);
            if (v != null) reply.setHeaderValue(d, v);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(177);

        return reply;
    }

    /**
     * Create the reply relative to the given file.
     * @param request the incomming request.
     * @return A Reply instance
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    protected Reply createFileReply(Request request) throws ProtocolException, ResourceException {
        File file = fresource.getFile();
        Reply reply = null;
        reply = createDefaultReply(request, HTTP.OK);
        try {
            FileInputStream fis = new FileInputStream(file);
            fis.skip(foffset);
            reply.setStream(fis);
        } catch (IOException ex) {
            Reply error = request.makeReply(HTTP.SERVICE_UNAVAILABLE);
            error.setContent(ex.getMessage());
            return error;
        }
        return reply;
    }
}
