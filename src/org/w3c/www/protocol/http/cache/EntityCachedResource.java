package org.w3c.www.protocol.http.cache;

import java.net.URL;
import java.util.Enumeration;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import org.w3c.util.ArrayDictionary;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.LongAttribute;
import org.w3c.tools.resources.StringAttribute;
import org.w3c.www.http.HeaderDescription;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpCacheControl;
import org.w3c.www.http.HttpContentRange;
import org.w3c.www.http.HttpRange;
import org.w3c.www.http.HttpEntityTag;
import org.w3c.www.http.HttpFactory;
import org.w3c.www.http.ByteRangeOutputStream;
import org.w3c.www.protocol.http.HttpException;
import org.w3c.www.protocol.http.Reply;
import org.w3c.www.protocol.http.Request;
import org.w3c.www.mime.MimeType;
import org.w3c.jigsaw.frames.MimeTypeAttribute;

/**
 * A cached resource with an entity
 */
public class EntityCachedResource extends CachedResource {

    /**
     * Condition check return code - Condition existed but failed.
     */
    public static final int COND_FAILED = 1;

    /**
     * Condition check return code - Condition existed and succeeded.
     */
    public static final int COND_OK = 2;

    /**
     * Condition check return code - Condition existed and succeeded 
     *                               but is a weak validation.
     */
    public static final int COND_WEAK = 3;

    /**
     * Attribute index - The Content-Type of the resource
     */
    protected static int ATTR_CONTENT_TYPE = -1;

    /**
     * Attribute index - The resource's max age.
     */
    protected static int ATTR_FRESHNESS_LIFETIME = -1;

    /**
     * Attribute index - The initial age of this resource.
     */
    protected static int ATTR_INITIAL_AGE = -1;

    /**
     * Attribute index - The response time
     */
    protected static int ATTR_RESPONSE_TIME = -1;

    /**
     * Attribute index - Revalidate flag
     */
    protected static int ATTR_REVALIDATE = -1;

    /**
     * Attribute index - The download state
     */
    protected static int ATTR_LOAD_STATE = -1;

    static {
        Attribute a = null;
        Class c = null;
        try {
            c = Class.forName("org.w3c.www.protocol.http.cache.EntityCachedResource");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        a = new MimeTypeAttribute("content-type", null, Attribute.COMPUTED);
        ATTR_CONTENT_TYPE = AttributeRegistry.registerAttribute(c, a);
        a = new IntegerAttribute("freshness-lifetime", null, Attribute.COMPUTED);
        ATTR_FRESHNESS_LIFETIME = AttributeRegistry.registerAttribute(c, a);
        a = new IntegerAttribute("initial-age", null, Attribute.COMPUTED);
        ATTR_INITIAL_AGE = AttributeRegistry.registerAttribute(c, a);
        a = new LongAttribute("response-time", null, Attribute.COMPUTED);
        ATTR_RESPONSE_TIME = AttributeRegistry.registerAttribute(c, a);
        a = new BooleanAttribute("revalidate", Boolean.FALSE, Attribute.COMPUTED);
        ATTR_REVALIDATE = AttributeRegistry.registerAttribute(c, a);
    }

    protected boolean revalidating = false;

    protected boolean regetting = false;

    protected boolean hasEntity = false;

    protected int oldsize = -1;

    protected int wantedsize = -1;

    protected CacheFilter filter;

    /**
     * Get the Content-Type of the cached resource of <code>null</code> if
     * there is no mime type (it should NEVER happen!)
     * @return a MimeType
     */
    public MimeType getContentType() {
        return (MimeType) getValue(ATTR_CONTENT_TYPE, null);
    }

    /**
     * Set the Content-Type of this cached resource
     * @param a MimeType, the mime type of this resource
     */
    public void setContentType(MimeType type) {
        setValue(ATTR_CONTENT_TYPE, type);
    }

    /**
     * Get this resource's freshness lifetime (RFC2616: 13.2.4).
     * @return A long number of seconds for which that entry will remain
     * valid, or <strong>-1</strong> if undefined.
     */
    public int getFreshnessLifetime() {
        return getInt(ATTR_FRESHNESS_LIFETIME, -1);
    }

    /**
     * Set this cached entry . freshness lifetime (RFC2616: 13.2.4).
     * @param maxage A number of seconds during which the entry will 
     * remain valid, or <strong>-1</strong> to undefine previous setting.
     */
    public void setFreshnessLifetime(int freshnessLifetime) {
        setInt(ATTR_FRESHNESS_LIFETIME, freshnessLifetime);
    }

    /**
     * Get this cached entry initial age.
     * @return A long number of seconds giving the initial age
     * or <strong>-1</strong> if undefined.
     */
    public int getInitialAge() {
        return getInt(ATTR_INITIAL_AGE, -1);
    }

    /**
     * Set this resource's initial age.
     * @param initage The initial age as a number of seconds
     * or <strong>-1</strong> to undefine previous setting.
     */
    public void setInitialAge(int initage) {
        setInt(ATTR_INITIAL_AGE, initage);
    }

    /**
     * Get the time of the response used to cached that entry.
     * @return A long number of milliseconds since Java epoch, or <strong>
     * -1</strong> if undefined.
     */
    public long getResponseTime() {
        return getLong(ATTR_RESPONSE_TIME, -1);
    }

    /**
     * Set this cached entry response time.
     * @param responsetime A long number of milliseconds indicating the 
     * response time relative to Java epoch, or <strong>-1</strong> to 
     * undefined previous setting.
     */
    public void setResponseTime(long responsetime) {
        setLong(ATTR_RESPONSE_TIME, responsetime);
    }

    /**
     * Get the revalidate flag
     * @return a boolean, <code>true</code> if the proxy must revalidate
     * stale entries
     * -1</strong> if undefined.
     */
    public boolean getRevalidate() {
        return getBoolean(ATTR_REVALIDATE, false);
    }

    /**
     * Set this cached entry revalidate flag.
     * @param validate, a boolean, <code>true</code> if this entry needs
     * to be revalidated while stale.
     */
    public void setRevalidate(boolean validate) {
        setBoolean(ATTR_REVALIDATE, validate);
    }

    /**
     * Get the entity tag associated with that cached entry
     * @return the entity tag or <strong>null</strong> if undefined
     */
    public HttpEntityTag getHETag() {
        if (definesAttribute(ATTR_ETAG)) {
            if (etags == null) {
                etags = new HttpEntityTag[1];
                etags[0] = HttpFactory.parseETag(getETag());
            }
            return etags[0];
        }
        return null;
    }

    /**
     * Get the cached data for that cached entry.
     * @return A <em>non-buffered</em> output stream.
     */
    public synchronized InputStream getInputStream() throws IOException {
        return new BufferedInputStream(new FileInputStream(getFile()));
    }

    /**
     * Get the current age of this resource
     * @return a long the current age of this resource
     */
    public int getCurrentAge() {
        long now = System.currentTimeMillis();
        return (int) (getInitialAge() + ((now - getResponseTime()) / 1000));
    }

    /**
     * Try to validate an <code>If-Modified-Since</code> request.
     * @param request The request to validate.
     * @return An integer, <code>COND_FAILED</code>, if the condition  was
     * checked, but failed; <code>COND_OK</code> of condition was checked
     * and succeeded, <strong>0</strong> otherwise.
     */
    public int checkIfModifiedSince(Request request) {
        long ims = request.getIfModifiedSince();
        long cmt = getLastModified();
        if (ims >= 0) {
            if (cmt > 0) {
                long s_cmt = cmt / 1000;
                long s_ims = ims / 1000;
                if (s_cmt < s_ims) {
                    return COND_FAILED;
                } else if (s_cmt == s_ims) {
                    return COND_WEAK;
                }
                return COND_OK;
            }
        }
        return 0;
    }

    /**
     * Try to validate an <code>If-Unmodified-Since</code> request.
     * @param request The request to validate.
     * @return An integer, <code>COND_FAILED</code>, if the condition  was
     * checked, but failed; <code>COND_OK</code> of condition was checked
     * and succeeded, <strong>0</strong> otherwise.
     */
    public int checkIfUnmodifiedSince(Request request) {
        long iums = request.getIfUnmodifiedSince();
        long cmt = getLastModified();
        if (iums >= 0) return ((cmt > 0) && (cmt - 1000) >= iums) ? COND_FAILED : COND_OK;
        return 0;
    }

    /**
     * Try to validate an <code>If-Match</code> request.
     * @param request The request to validate.
     * @return An integer, <code>COND_FAILED</code>, if the condition  was
     * checked, but failed; <code>COND_OK</code> of condition was checked
     * and succeeded, <strong>0</strong> otherwise.
     */
    public int checkIfMatch(Request request) {
        HttpEntityTag tags[] = request.getIfMatch();
        if (tags != null) {
            HttpEntityTag etag = getHETag();
            if (etag != null) {
                edu.hkust.clap.monitor.Monitor.loopBegin(262);
for (int i = 0; i < tags.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(262);
{
                    HttpEntityTag t = tags[i];
                    if (t.getTag().equals(etag.getTag())) {
                        if (t.isWeak() || etag.isWeak()) {
                            return COND_WEAK;
                        } else {
                            return COND_OK;
                        }
                    }
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(262);

            }
            return COND_FAILED;
        }
        return 0;
    }

    /**
     * Try to validate an <code>If-None-Match</code> request.
     * @param request The request to validate.
     * @return An integer, <code>COND_FAILED</code>, if the condition  was
     * checked, but failed; <code>COND_OK</code> of condition was checked
     * and succeeded, <strong>0</strong> otherwise.
     */
    public int checkIfNoneMatch(Request request) {
        String setag = getETag();
        HttpEntityTag etag = null;
        HttpEntityTag tags[] = request.getIfNoneMatch();
        if (setag != null) {
            etag = HttpFactory.parseETag(getETag());
        }
        if (tags != null) {
            if (etag == null) {
                return COND_OK;
            }
            int status = COND_OK;
            edu.hkust.clap.monitor.Monitor.loopBegin(619);
for (int i = 0; i < tags.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(619);
{
                HttpEntityTag t = tags[i];
                if (t.getTag().equals(etag.getTag())) {
                    if (t.isWeak() || etag.isWeak()) {
                        status = COND_WEAK;
                    } else {
                        return COND_FAILED;
                    }
                }
                if (t.getTag().equals("*")) {
                    return COND_FAILED;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(619);

            return status;
        }
        return 0;
    }

    /**
     * Called when the tee succeed, it allows you to notify a listener of the 
     * Tee that the download completed succesfully with a specific size
     * @parameter the size received, an integer
     */
    public synchronized void notifyTeeSuccess(int size) {
        int state = getLoadState();
        try {
            if (wantedsize > 0) {
                if (!regetting) {
                    if (state == STATE_NOT_LOADED) {
                        if (size == wantedsize) {
                            setCurrentLength(size);
                            setLoadState(STATE_LOAD_COMPLETE);
                        } else {
                            setCurrentLength(size);
                            setLoadState(STATE_LOAD_ERROR);
                            System.out.println(getIdentifier() + ": tee stream mismatch, " + "bytes(adv/got)=" + wantedsize + "/" + size);
                        }
                    } else {
                        setCurrentLength(size);
                        setLoadState(STATE_LOAD_ERROR);
                        System.out.println(getIdentifier() + ": UNKNOWN STATE for " + "tee stream!, bytes(adv/got)=" + wantedsize + "/" + size);
                    }
                } else {
                    if (size == wantedsize) {
                        setCurrentLength(oldsize + wantedsize);
                        setLoadState(STATE_LOAD_COMPLETE);
                    } else {
                        setCurrentLength(size);
                        setLoadState(STATE_LOAD_ERROR);
                        System.out.println(getIdentifier() + ": tee stream mismatch in reget, " + "bytes(adv/got)=" + wantedsize + "/" + size);
                    }
                }
            } else {
                setCurrentLength(size);
                setLoadState(STATE_LOAD_COMPLETE);
            }
        } finally {
            cleanUpload();
        }
    }

    public void notifyTeeFailure(int size) {
        System.out.println(getIdentifier() + ": tee streaming failed !");
        int state = getLoadState();
        setCurrentLength(size);
        setLoadState(STATE_LOAD_ERROR);
        System.out.println(getIdentifier() + ": tee stream mismatch, " + "bytes(adv/got)=" + wantedsize + "/" + size);
        cleanUpload();
    }

    protected synchronized void cleanUpload() {
        uploading = false;
        filter.cleanUpload(this);
        notifyAll();
    }

    /**
     * FIXME Will be replaced soon, so that multiple people may share 
     * the same temporary resource.
     * Wait for the upload to finish, if needed.
     */
    protected final synchronized void waitUpload() {
        edu.hkust.clap.monitor.Monitor.loopBegin(620);
while (uploading) { 
edu.hkust.clap.monitor.Monitor.loopInc(620);
{
            try {
                wait();
            } catch (InterruptedException ex) {
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(620);

    }

    /**
     * handle a range request, according to the first range or the
     * request FIXME we should handle all the ranges at some point...
     */
    protected Reply handleRangeRequest(Request request, HttpRange r) {
        HttpEntityTag t = request.getIfRange();
        if (t != null) {
            if (t.isWeak() || !t.getTag().equals(getHETag().getTag())) return null;
        }
        int cl = getContentLength();
        int fb = r.getFirstPosition();
        int lb = r.getLastPosition();
        int sz;
        if (fb > cl - 1) {
            HttpContentRange cr = HttpFactory.makeContentRange("bytes", 0, cl - 1, cl);
            Reply rr;
            rr = request.makeReply(HTTP.REQUESTED_RANGE_NOT_SATISFIABLE);
            rr.setContentLength(-1);
            rr.setHeaderValue(rr.H_CONTENT_RANGE, cr);
            rr.setContentMD5(null);
            return rr;
        }
        if ((fb < 0) && (lb >= 0)) {
            if (lb >= cl) lb = cl;
            sz = lb;
            fb = cl - lb;
            lb = cl - 1;
        } else if (lb < 0) {
            lb = cl - 1;
            sz = lb - fb + 1;
        } else {
            if (lb >= cl) lb = cl - 1;
            sz = lb - fb + 1;
        }
        if ((fb < 0) || (lb < 0) || (fb <= lb)) {
            HttpContentRange cr = null;
            fb = (fb < 0) ? 0 : fb;
            lb = ((lb > cl) || (lb < 0)) ? cl : lb;
            cr = HttpFactory.makeContentRange("bytes", fb, lb, cl);
            Reply rr = request.makeReply(HTTP.PARTIAL_CONTENT);
            try {
                rr.setContentMD5(null);
                rr.setContentLength(sz);
                rr.setHeaderValue(rr.H_CONTENT_RANGE, cr);
                rr.setStream(new ByteRangeOutputStream(getFile(), fb, lb + 1));
                return rr;
            } catch (IOException ex) {
            }
        }
        return null;
    }

    /**
     * decorate the reply header with some meta information taken
     * from the cached resource
     * @return a reply, the one we just updated
     */
    protected Reply setReplyHeaders(Reply reply) {
        int status = reply.getStatus();
        if (status != HTTP.NOT_MODIFIED) {
            reply.setContentLength(getContentLength());
            reply.setContentMD5(getContentMD5());
            reply.setContentLanguage(getContentLanguage());
            reply.setContentEncoding(getContentEncoding());
            reply.setContentType(getContentType());
            reply.setLastModified(getLastModified());
            reply.setVary(getVary());
        }
        reply.setETag(getHETag());
        long date = getDate();
        if (date > 0) reply.setDate(getDate());
        reply.setAge(getCurrentAge());
        ArrayDictionary a = getExtraHeaders();
        if (a != null) {
            Enumeration e = a.keys();
            edu.hkust.clap.monitor.Monitor.loopBegin(621);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(621);
{
                String hname = (String) e.nextElement();
                String hvalue = (String) a.get(hname);
                reply.setValue(hname, hvalue);
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(621);

        }
        if ((filter != null) && filter.isShared()) {
            HttpCacheControl hcc = reply.getCacheControl();
            if (hcc != null) {
                String priv[] = hcc.getPrivate();
                if (priv != null) {
                    edu.hkust.clap.monitor.Monitor.loopBegin(622);
for (int i = 0; i < priv.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(622);
{
                        reply.setHeaderValue(priv[i], null);
                    }} 
edu.hkust.clap.monitor.Monitor.loopEnd(622);

                }
            }
        }
        return reply;
    }

    /**
     * check the validators namely LMT/Etags according to rfc2616 rules
     * @return An integer, either <code>COND_FAILED</cond> if condition
     * was checked, but failed, <code>COND_OK</code> if condition was checked
     * and succeeded, or <strong>0</strong> if the condition was not checked
     * at all (eg because the resource or the request didn't support it).
     */
    public int checkValidators(Request request) {
        int v_inm = checkIfNoneMatch(request);
        int v_ims = checkIfModifiedSince(request);
        if ((v_inm == COND_OK) || (v_ims == COND_OK)) {
            return COND_OK;
        }
        if ((v_inm == COND_FAILED) || (v_ims == COND_FAILED)) {
            return COND_FAILED;
        }
        if ((v_inm == COND_WEAK) || (v_ims == COND_WEAK)) {
            return COND_FAILED;
        }
        return 0;
    }

    /**
     * This cached entry has been checked valid, perform given request.
     * @param request The request to perform.
     * @return An Reply instance.
     * @exception HttpException If something went wrong.
     */
    public Reply perform(Request request) throws HttpException {
        waitUpload();
        try {
            Reply reply = null;
            boolean needsEntity = true;
            HttpRange ranges[] = request.getRange();
            if ((ranges != null) && (ranges.length == 1)) reply = handleRangeRequest(request, ranges[0]);
            if (reply == null) {
                int status = getStatus();
                int cim = checkIfMatch(request);
                if ((cim == COND_FAILED) || (cim == COND_WEAK)) {
                    status = HTTP.PRECONDITION_FAILED;
                    needsEntity = false;
                    reply = request.makeReply(status);
                    reply.setContent("Pre-conditions failed.");
                    throw new HttpException(request, reply, "pre-condition");
                } else if (checkIfUnmodifiedSince(request) == COND_FAILED) {
                    status = HTTP.PRECONDITION_FAILED;
                    reply = request.makeReply(status);
                    reply.setContent("Pre-conditions failed.");
                    throw new HttpException(request, reply, "pre-condition");
                } else if (checkValidators(request) == COND_FAILED) {
                    status = HTTP.NOT_MODIFIED;
                    needsEntity = false;
                }
                reply = request.makeReply(status);
                if (needsEntity) {
                    reply.setStream(getInputStream());
                }
            }
            setReplyHeaders(reply);
            String mth = request.getMethod();
            if (mth.equals("HEAD") || mth.equals("OPTIONS")) reply.setStream(null);
            return reply;
        } catch (IOException ex) {
        }
        return null;
    }

    /**
     * Try using an active stream to cache the content.
     * Byte size usage is taken care of only at the end of the download
     * to make sure we get the right sizes (might different from the
     * advertized ones).
     * @return An InputStream instance if active caching was possible,
     * <strong>null</strong> otherwise.
     */
    public synchronized InputStream tryActiveCacheContent(InputStream in) throws IOException {
        InputStream tee = null;
        OutputStream out = null;
        uploading = true;
        try {
            out = new FileOutputStream(getFile());
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        tee = ActiveStream.createTee(this, in, out);
        if (tee != null) return tee;
        try {
            out.close();
        } catch (IOException ex) {
        }
        return null;
    }

    /**
     * The basic initialization
     */
    public void initialize(Object values[]) {
        super.initialize(values);
    }

    /**
     * sets some useful information about the entity
     * @param the request that requested this entity
     * @param the reply triggered by this request
     */
    protected void updateInfo(Request request, Reply rep) {
        String mth = request.getMethod();
        Reply reply = (Reply) rep.getClone();
        boolean hasEntity = !(mth.equals("HEAD") || mth.equals("OPTIONS"));
        if (!request.hasState(CacheState.STATE_REVALIDATION)) {
            HttpCacheControl hcc = reply.getCacheControl();
            if (hcc != null) {
                String nocache[] = hcc.getNoCache();
                if (nocache != null) {
                    edu.hkust.clap.monitor.Monitor.loopBegin(623);
for (int i = 0; i < nocache.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(623);
{
                        reply.setHeaderValue(nocache[i], null);
                    }} 
edu.hkust.clap.monitor.Monitor.loopEnd(623);

                }
            }
            setStatus(reply.getStatus());
            setContentType(reply.getContentType());
            setContentLength(reply.getContentLength());
            setLastModified(reply.getLastModified());
            setContentMD5(reply.getContentMD5());
            String vary[] = reply.getVary();
            setVary(vary);
            if (vary != null) {
                ArrayDictionary a = null;
                edu.hkust.clap.monitor.Monitor.loopBegin(624);
for (int i = 0; i < vary.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(624);
{
                    if (vary[i].equals("*")) {
                        continue;
                    }
                    if (a == null) {
                        a = new ArrayDictionary(vary.length);
                    }
                    a.put(vary[i].toLowerCase(), request.getValue(vary[i]));
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(624);

                if (a != null) {
                    setConnegHeaders(a);
                }
            }
            if (reply.hasHeader(reply.H_ETAG)) {
                setETag(reply.getETag().toString());
            } else {
                setETag(null);
            }
            ArrayDictionary a = new ArrayDictionary(5, 5);
            Enumeration e = reply.enumerateHeaderDescriptions();
            edu.hkust.clap.monitor.Monitor.loopBegin(625);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(625);
{
                HeaderDescription d = (HeaderDescription) e.nextElement();
                if (d.isHeader(Reply.H_CONTENT_TYPE) || d.isHeader(Reply.H_CONTENT_LENGTH) || d.isHeader(Reply.H_LAST_MODIFIED) || d.isHeader(Reply.H_ETAG) || d.isHeader(Reply.H_AGE) || d.isHeader(Reply.H_DATE) || d.isHeader(Reply.H_VARY) || d.isHeader(Reply.H_CONNECTION) || d.isHeader(Reply.H_PROXY_CONNECTION) || d.isHeader(Reply.H_TRANSFER_ENCODING) || d.isHeader(Reply.H_CONTENT_MD5) || d.getName().equalsIgnoreCase("keep-alive")) continue;
                a.put(d.getName(), reply.getValue(d));
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(625);

            setExtraHeaders(a);
        }
    }

    /**
     * This cached entry needs revalidation, it will modify the 
     * request to do that.
     */
    public Request setRequestRevalidation(Request request) {
        Request origreq = (Request) request.getClone();
        request.setState(CacheState.STATE_RESOURCE, this);
        request.setState(CacheState.STATE_ORIGREQ, origreq);
        request.setIfModifiedSince(getLastModified());
        if ((etags == null) && (getETag() != null)) {
            etags = new HttpEntityTag[1];
            etags[0] = HttpFactory.parseETag(getETag());
        }
        request.setIfNoneMatch(etags);
        request.setIfRange(null);
        request.setRange(null);
        request.setIfUnmodifiedSince(-1);
        request.setIfMatch(null);
        return request;
    }

    /**
     * A constructor for new resources that will get some data
     * directly
     * FIXME params
     */
    public EntityCachedResource(CacheFilter filter, Request req, Reply rep) {
        invalidated = false;
        setValue(ATTR_IDENTIFIER, req.getURL().toExternalForm());
        this.filter = filter;
        updateInfo(req, rep);
        filter.getValidator().updateExpirationInfo(this, req, rep);
        setFile(filter.getStore().getNewEntryFile());
        wantedsize = rep.getContentLength();
        InputStream in;
        try {
            in = tryActiveCacheContent(rep.getInputStream());
            if (in == null) {
            }
            rep.setStream(in);
        } catch (IOException ex) {
        }
    }

    public EntityCachedResource() {
        super();
    }
}
