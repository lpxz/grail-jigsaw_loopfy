package org.w3c.jigedit.filters;

import java.io.IOException;
import java.io.InputStream;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFilter;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.StringArrayAttribute;
import org.w3c.tools.resources.StringAttribute;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;
import org.w3c.www.mime.MimeType;
import org.w3c.jigsaw.http.Client;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.frames.HTTPFrame;

public class SimpleGrepFilter extends ResourceFilter {

    class ByteArrayComp {

        byte[] tab = null;

        String string = null;

        int idx = -1;

        int length = -1;

        boolean matchNow(byte c) {
            if (tab[idx++] == c) {
                return (idx == length);
            } else {
                idx = 0;
                return false;
            }
        }

        void reset() {
            idx = 0;
        }

        String getString() {
            return string;
        }

        ByteArrayComp(String string) {
            tab = string.getBytes();
            idx = 0;
            length = tab.length;
            this.string = string;
        }
    }

    protected ByteArrayComp[] forbiddenBytes = null;

    /**
     * Attribute index - The strings to grep.
     */
    protected static int ATTR_FORBIDSTRING_ARRAY = -1;

    /**
     * Attribute index - The url to redirect.
     */
    protected static int ATTR_REDIRECT = -1;

    static {
        Class c = null;
        Attribute a = null;
        try {
            c = Class.forName("org.w3c.jigedit.filters.SimpleGrepFilter");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        a = new StringArrayAttribute("forbidden-strings", null, Attribute.EDITABLE | Attribute.MANDATORY);
        ATTR_FORBIDSTRING_ARRAY = AttributeRegistry.registerAttribute(c, a);
        a = new StringAttribute("redirect-url", null, Attribute.EDITABLE | Attribute.MANDATORY);
        ATTR_REDIRECT = AttributeRegistry.registerAttribute(c, a);
    }

    protected String[] getForbiddenStrings() {
        return (String[]) getValue(ATTR_FORBIDSTRING_ARRAY, null);
    }

    protected String getRedirectURL() {
        return (String) getValue(ATTR_REDIRECT, null);
    }

    protected ByteArrayComp[] getForbiddenBytes() {
        if (forbiddenBytes == null) {
            String[] fstrings = getForbiddenStrings();
            forbiddenBytes = new ByteArrayComp[fstrings.length];
            edu.hkust.clap.monitor.Monitor.loopBegin(671);
for (int i = 0; i < fstrings.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(671);
forbiddenBytes[i] = new ByteArrayComp(fstrings[i]);} 
edu.hkust.clap.monitor.Monitor.loopEnd(671);

        }
        return forbiddenBytes;
    }

    /**
     * Catch assignements to the forbidden strings attribute.
     * <p>When a change to that attribute is detected, the cached value
     * are updated.
     */
    public void setValue(int idx, Object value) {
        super.setValue(idx, value);
        if (idx == ATTR_FORBIDSTRING_ARRAY) {
            forbiddenBytes = null;
        }
    }

    /**
     * Searh for a forbidden string in given stream.
     * @param in the InputStream
     * @return The String found or <strong>null</strong> if none
     * was found.
     */
    protected String searchForbiddenStrings(InputStream in) {
        if (getForbiddenStrings() == null) return null;
        try {
            ByteArrayComp comp[] = getForbiddenBytes();
            int len = in.available();
            int c;
            in.mark(len);
            int baclen = comp.length;
            edu.hkust.clap.monitor.Monitor.loopBegin(672);
for (int j = 0; j < baclen; j++) { 
edu.hkust.clap.monitor.Monitor.loopInc(672);
comp[j].reset();} 
edu.hkust.clap.monitor.Monitor.loopEnd(672);

            edu.hkust.clap.monitor.Monitor.loopBegin(673);
while ((c = in.read()) != -1) { 
edu.hkust.clap.monitor.Monitor.loopInc(673);
{
                edu.hkust.clap.monitor.Monitor.loopBegin(674);
for (int i = 0; i < baclen; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(674);
{
                    if (comp[i].matchNow((byte) c)) {
                        in.reset();
                        return comp[i].getString();
                    }
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(674);

            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(673);

            in.reset();
            return null;
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Search the forbidden string in the body, if found return
     * an ACCES FORBIDDEN Reply.
     * @param request The request that is about to be processsed.
     */
    public ReplyInterface ingoingFilter(RequestInterface req) {
        Request request = (Request) req;
        if (request.getMethod().equals("PUT")) {
            try {
                MimeType req_mt = request.getContentType();
                if (req_mt.match(MimeType.TEXT) == MimeType.NO_MATCH) return null;
            } catch (NullPointerException ex) {
            }
            InputStream in = null;
            try {
                in = request.getInputStream();
                if (in == null) {
                    return null;
                }
            } catch (IOException ex) {
                return null;
            }
            ResourceReference rr = request.getTargetResource();
            if (rr != null) {
                try {
                    FramedResource target = (FramedResource) rr.lock();
                    HTTPFrame frame = null;
                    try {
                        frame = (HTTPFrame) target.getFrame(Class.forName("org.w3c.jigsaw.frames.HTTPFrame"));
                    } catch (ClassNotFoundException cex) {
                        cex.printStackTrace();
                    }
                    if (frame == null) return null;
                    if (!frame.getPutableFlag()) {
                        return null;
                    }
                    int cim = frame.checkIfMatch(request);
                    if ((cim == HTTPFrame.COND_FAILED) || (cim == HTTPFrame.COND_WEAK) || (frame.checkIfNoneMatch(request) == HTTPFrame.COND_FAILED) || (frame.checkIfModifiedSince(request) == HTTPFrame.COND_FAILED) || (frame.checkIfUnmodifiedSince(request) == HTTPFrame.COND_FAILED)) {
                        Reply r = request.makeReply(HTTP.PRECONDITION_FAILED);
                        r.setContent("Pre-condition failed.");
                        return r;
                    }
                } catch (InvalidResourceException ex) {
                    ex.printStackTrace();
                } finally {
                    rr.unlock();
                }
            }
            String expect = request.getExpect();
            if (expect != null) {
                if (expect.startsWith("100")) {
                    Client client = request.getClient();
                    if (client != null) {
                        try {
                            client.sendContinue();
                        } catch (java.io.IOException ex) {
                            return null;
                        }
                    }
                }
            }
            String found = searchForbiddenStrings(in);
            if (found != null) {
                Reply error = request.makeReply(HTTP.FORBIDDEN);
                error.setReason("the string \"" + found + "\" is forbidden.");
                error.setContent("<p>the string \"" + found + "\" is forbidden.</p><br> click " + "<A HREF=\"" + getRedirectURL() + "\">here</A>" + " for explaination.");
                return error;
            }
            return null;
        } else return null;
    }
}
