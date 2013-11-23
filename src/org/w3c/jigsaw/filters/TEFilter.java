package org.w3c.jigsaw.filters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFilter;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.StringArrayAttribute;
import org.w3c.www.mime.MimeType;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.www.http.HttpAcceptEncoding;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpRequestMessage;

/**
 * This filter will compress the content of replies using GZIP or whatever
 * encoding scheme requested in the TE: header of the request.
 * Compression is done <em>on the fly</em>. This assumes that you're really
 * on a slow link, where you have lots of CPU, but not much bandwidth.
 * <p>A nifty usage for that filter, is to plug it on top of a
 * <code>org.w3c.jigsaw.proxy.ProxyFrame</code>, in which case it
 * will encode the data when it flies out of the proxy.
 */
public class TEFilter extends ResourceFilter {

    /**
     * Attribute index - List of MIME type that we can compress
     */
    protected static int ATTR_MIME_TYPES = -1;

    static {
        Class c = null;
        Attribute a = null;
        try {
            c = Class.forName("org.w3c.jigsaw.filters.TEFilter");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        a = new StringArrayAttribute("mime-types", null, Attribute.EDITABLE);
        ATTR_MIME_TYPES = AttributeRegistry.registerAttribute(c, a);
    }

    /**
     * The set of MIME types we are allowed to compress.
     */
    protected MimeType types[] = null;

    private class DataMover extends Thread {

        InputStream in = null;

        OutputStream out = null;

        public void run() {
            try {
                byte buf[] = new byte[1024];
                int got = -1;
                edu.hkust.clap.monitor.Monitor.loopBegin(64);
while ((got = in.read(buf)) >= 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(64);
out.write(buf, 0, got);} 
edu.hkust.clap.monitor.Monitor.loopEnd(64);

            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    in.close();
                } catch (Exception ex) {
                }
                ;
                try {
                    out.close();
                } catch (Exception ex) {
                }
                ;
            }
        }

        DataMover(InputStream in, OutputStream out) {
            this.in = in;
            this.out = out;
            setName("DataMover");
            start();
        }
    }

    /**
     * Catch the setting of mime types to compress.
     * @param idx The attribute being set.
     * @param val The new attribute value.
     */
    public void setValue(int idx, Object value) {
        super.setValue(idx, value);
        if (idx == ATTR_MIME_TYPES) {
            synchronized (this) {
                types = null;
            }
        }
    }

    /**
     * Get the set of MIME types to match:
     * @return An array of MimeType instances.
     */
    public synchronized MimeType[] getMimeTypes() {
        if (types == null) {
            String strtypes[] = (String[]) getValue(ATTR_MIME_TYPES, null);
            if (strtypes == null) return null;
            types = new MimeType[strtypes.length];
            edu.hkust.clap.monitor.Monitor.loopBegin(65);
for (int i = 0; i < types.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(65);
{
                try {
                    types[i] = new MimeType(strtypes[i]);
                } catch (Exception ex) {
                    types[i] = null;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(65);

        }
        return types;
    }

    protected double getCompressibilityFactor(Reply reply) {
        MimeType t[] = getMimeTypes();
        if (t != null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(66);
for (int i = 0; i < t.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(66);
{
                if (t[i] == null) continue;
                if (t[i].match(reply.getContentType()) > 0) {
                    String enc[] = reply.getContentEncoding();
                    if (enc != null) {
                        edu.hkust.clap.monitor.Monitor.loopBegin(67);
for (int j = 0; j < enc.length; j++) { 
edu.hkust.clap.monitor.Monitor.loopInc(67);
{
                            if ((enc[j].indexOf("gzip") >= 0) || (enc[j].indexOf("deflate") >= 0) || (enc[j].indexOf("compress") >= 0)) {
                                return 0.001;
                            }
                        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(67);

                    }
                    return 1.0;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(66);

        }
        return 0.001;
    }

    protected void doEncoding(HttpAcceptEncoding enc, Reply reply) {
        if (!reply.hasStream()) return;
        MimeType t[] = getMimeTypes();
        boolean matched = false;
        if (t != null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(68);
for (int i = 0; i < t.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(68);
{
                if (t[i] == null) continue;
                if (t[i].match(reply.getContentType()) > 0) {
                    matched = true;
                    break;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(68);

        }
        if (!matched) return;
        InputStream orig_is = reply.openStream();
        if (enc.getEncoding().equals("gzip")) {
            try {
                PipedOutputStream gzpout = new PipedOutputStream();
                PipedInputStream gzpin = new PipedInputStream(gzpout);
                new DataMover(reply.openStream(), new GZIPOutputStream(gzpout));
                reply.setStream(gzpin);
            } catch (IOException ex) {
                ex.printStackTrace();
                reply.setStream(orig_is);
                return;
            }
            reply.addTransferEncoding("gzip");
            reply.setContentLength(-1);
        } else if (enc.getEncoding().equals("deflate")) {
            try {
                PipedOutputStream zpout = new PipedOutputStream();
                PipedInputStream zpin = new PipedInputStream(zpout);
                new DataMover(reply.openStream(), new DeflaterOutputStream(zpout));
                reply.setStream(zpin);
            } catch (IOException ex) {
                ex.printStackTrace();
                reply.setStream(orig_is);
                return;
            }
            reply.addTransferEncoding("deflate");
            reply.setContentLength(-1);
        }
        return;
    }

    /**
     * @param request The original request.
     * @param reply It's original reply. 
     * @return A Reply instance, or <strong>null</strong> if processing 
     * should continue normally. 
     * @exception ProtocolException If processing should be interrupted,  
     * because an abnormal situation occured. 
     */
    public ReplyInterface outgoingFilter(RequestInterface req, ReplyInterface rep) throws ProtocolException {
        Request request = (Request) req;
        Reply reply = (Reply) rep;
        HttpAcceptEncoding encs[] = request.getTE();
        String trenc[] = reply.getTransferEncoding();
        if (!reply.hasStream()) return null;
        if (trenc != null) {
            return null;
        }
        if (encs != null) {
            double max = -1.0;
            double identity = 1.0;
            double chunked = 1.0;
            double comp_factor = getCompressibilityFactor(reply);
            HttpAcceptEncoding best = null;
            edu.hkust.clap.monitor.Monitor.loopBegin(69);
for (int i = 0; i < encs.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(69);
{
                if (encs[i].getEncoding().equals("identity")) {
                    identity = encs[i].getQuality();
                    continue;
                } else if (encs[i].getEncoding().equals("chunked")) {
                    chunked = encs[i].getQuality();
                    continue;
                } else if (encs[i].getEncoding().equals("trailers")) {
                    continue;
                }
                if (encs[i].getQuality() * comp_factor > max) {
                    best = encs[i];
                    max = encs[i].getQuality() * comp_factor;
                    if (max == 1.0) break;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(69);

            if (best != null && (max >= identity)) {
                doEncoding(best, reply);
            } else {
                if (identity > 0) {
                    if (chunked > identity) reply.setContentLength(-1);
                } else {
                    reply.setContentLength(-1);
                }
            }
        }
        return null;
    }
}
