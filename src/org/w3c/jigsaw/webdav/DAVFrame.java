package org.w3c.jigsaw.webdav;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.Vector;
import org.xml.sax.SAXException;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.jigsaw.auth.AuthFilter;
import org.w3c.jigsaw.frames.PostableFrame;
import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.www.http.ContentLengthInputStream;
import org.w3c.tools.codec.Base64Decoder;
import org.w3c.tools.codec.Base64Encoder;
import org.w3c.tools.codec.Base64FormatException;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.ContainerResource;
import org.w3c.tools.resources.DateAttribute;
import org.w3c.tools.resources.DirectoryResource;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.LookupResult;
import org.w3c.tools.resources.LookupState;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.PropertiesAttribute;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.ServerInterface;
import org.w3c.tools.resources.StringAttribute;
import org.w3c.util.ArrayDictionary;
import org.w3c.util.DateParser;
import org.w3c.util.InvalidDateException;
import org.w3c.util.ObservableProperties;
import org.w3c.util.URLUtils;
import org.w3c.www.http.HeaderDescription;
import org.w3c.www.http.HeaderValue;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpDate;
import org.w3c.www.http.HttpEntityTag;
import org.w3c.www.http.HttpFactory;
import org.w3c.www.http.HttpInvalidValueException;
import org.w3c.www.http.HttpMimeType;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;
import org.w3c.www.http.HttpTokenList;
import org.w3c.www.mime.MimeType;
import org.w3c.www.mime.MimeTypeFormatException;
import org.w3c.www.webdav.WEBDAV;
import org.w3c.www.webdav.DAVEntityTag;
import org.w3c.www.webdav.DAVIf;
import org.w3c.www.webdav.DAVParser;
import org.w3c.www.webdav.DAVStateToken;
import org.w3c.www.webdav.xml.DAVBody;
import org.w3c.www.webdav.xml.DAVFactory;
import org.w3c.www.webdav.xml.DAVLockInfo;
import org.w3c.www.webdav.xml.DAVMultiStatus;
import org.w3c.www.webdav.xml.DAVNode;
import org.w3c.www.webdav.xml.DAVPropAction;
import org.w3c.www.webdav.xml.DAVPropertyBehavior;
import org.w3c.www.webdav.xml.DAVPropFind;
import org.w3c.www.webdav.xml.DAVPropStat;
import org.w3c.www.webdav.xml.DAVProperties;
import org.w3c.www.webdav.xml.DAVPropertyUpdate;
import org.w3c.www.webdav.xml.DAVResponse;

/**
 * @version $Revision: 1.2 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class DAVFrame extends PostableFrame {

    public static final boolean debug = false;

    public static final boolean debugxml = false;

    /**
     * Condition check return code - Condition existed and succeeded.
     * And lock has been checked
     */
    public static final int COND_OK_LOCK = 3;

    /**
     * Cached mime type
     */
    protected static HttpMimeType xmlcontenttype = null;

    protected static HttpMimeType collectioncontenttype = null;

    protected static boolean isReadOnly(String propname) {
        return (propname.equals(DAVNode.CREATIONDATE_NODE) || propname.equals(DAVNode.GETCONTENTLENGTH_NODE) || propname.equals(DAVNode.GETETAG_NODE) || propname.equals(DAVNode.GETLASTMODIFIED_NODE));
    }

    protected static boolean acceptRedirect(DAVRequest request) {
        String method = request.getMethod();
        return !(method.equals("PROPPATCH") || method.equals("PROPFIND") || method.equals("COPY") || method.equals("MOVE") || method.equals("DELETE") || method.equals("LOCK") || method.equals("UNLOCK"));
    }

    /**
     * The DAVManager we use.
     */
    protected org.w3c.www.protocol.webdav.DAVManager manager = null;

    /**
     * Name ot the state to hold the remaining path (used with MKCOL)
     */
    public static final String REMAINING_PATH = "org.w3c.jigsaw.webdav.rpath";

    public static final String LOCK_USERNAME = "org.w3c.jigsaw.webdav.user";

    public static final String LOCK_TOKEN = "org.w3c.jigsaw.webdav.token";

    public static final String LOCK_OWNER = "org.w3c.jigsaw.webdav.owner";

    public static final String LOCK_EXPIRE = "org.w3c.jigsaw.webdav.expire";

    public static final String LOCK_TIMEOUT = "org.w3c.jigsaw.webdav.timeout";

    public static final String LOCKED_REREFENCE = "org.w3c.jigsaw.webdav.reference";

    public static final Long DEFAULT_LOCK_TIMEOUT = new Long(24 * 60 * 60 * 1000);

    public static final Long MAX_LOCK_TIMEOUT = new Long(7 * 24 * 60 * 60 * 1000);

    /**
     * Attribute index - The index for the creation date attribute.
     */
    protected static int ATTR_CREATION_DATE = -1;

    /**
     * Attribute index - The index for the dead properties
     */
    protected static int ATTR_DEAD_PROPERTIES = -1;

    /**
     * Attribute index - The index for the lock token
     */
    protected static int ATTR_LOCK_TOKEN = -1;

    /**
     * Attribute index - The index for the lock token
     */
    protected static int ATTR_LOCK_TIMEOUT = -1;

    /**
     * Attribute index - The index for the lock date
     */
    protected static int ATTR_LOCK_DATE = -1;

    /**
     * Attribute index - The index for the lock depth
     */
    protected static int ATTR_LOCK_DEPTH = -1;

    /**
     * Attribute index - The index for the lock owner
     */
    protected static int ATTR_LOCK_OWNER = -1;

    /**
     * Attribute index - The index for the lock username
     */
    protected static int ATTR_LOCK_USERNAME = -1;

    static {
        Class cls = null;
        try {
            cls = Class.forName("org.w3c.jigsaw.webdav.DAVFrame");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        Attribute a = new DateAttribute("creation-date", null, Attribute.COMPUTED);
        ATTR_CREATION_DATE = AttributeRegistry.registerAttribute(cls, a);
        a = new PropertiesAttribute("dead-properties", null, Attribute.COMPUTED);
        ATTR_DEAD_PROPERTIES = AttributeRegistry.registerAttribute(cls, a);
        a = new StringAttribute("lock-token", null, Attribute.COMPUTED);
        ATTR_LOCK_TOKEN = AttributeRegistry.registerAttribute(cls, a);
        a = new DateAttribute("lock-timeout", null, Attribute.COMPUTED);
        ATTR_LOCK_TIMEOUT = AttributeRegistry.registerAttribute(cls, a);
        a = new DateAttribute("lock-date", null, Attribute.COMPUTED);
        ATTR_LOCK_DATE = AttributeRegistry.registerAttribute(cls, a);
        a = new IntegerAttribute("lock-depth", null, Attribute.COMPUTED);
        ATTR_LOCK_DEPTH = AttributeRegistry.registerAttribute(cls, a);
        a = new StringAttribute("lock-owner", null, Attribute.COMPUTED);
        ATTR_LOCK_OWNER = AttributeRegistry.registerAttribute(cls, a);
        a = new StringAttribute("lock-username", null, Attribute.COMPUTED);
        ATTR_LOCK_USERNAME = AttributeRegistry.registerAttribute(cls, a);
        try {
            MimeType type = new MimeType("text/xml");
            type.addParameter("charset", WEBDAV.ENCODING);
            xmlcontenttype = HttpFactory.makeMimeType(type);
            type = new MimeType("httpd/unix-directory");
            type.addParameter("charset", WEBDAV.ENCODING);
            collectioncontenttype = HttpFactory.makeMimeType(type);
        } catch (MimeTypeFormatException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public boolean isCollection() {
        return (dresource != null);
    }

    /**
     * Get this resource creation date
     * @return A long giving the creation date or -1 if undefined.
     */
    public long getCreationDate() {
        return getLong(ATTR_CREATION_DATE, (long) -1);
    }

    protected void updateLockDate(DAVRequest request) {
        if (checkLockOwner(request)) {
            if (debug) {
                System.out.println(">>> update lock date");
            }
            setValue(ATTR_LOCK_DATE, new Long(System.currentTimeMillis()));
        }
    }

    /**
     * Get the lock token expiration date
     * @return A long giving the expiration date or -1 if undefined.
     */
    protected long getTokenExpirationDate(DAVRequest request) {
        long timeout = getLong(ATTR_LOCK_TIMEOUT, (long) -1);
        long lockdate = getLong(ATTR_LOCK_DATE, (long) -1);
        if ((lockdate != -1) && (timeout != -1)) {
            return lockdate + timeout;
        } else if (request != null) {
            Long expire = (Long) request.getState(LOCK_EXPIRE);
            if (expire != null) {
                return expire.longValue();
            }
        }
        return -1;
    }

    /**
     * Get the lock token 
     * @return A String instance or null
     */
    protected String getCurrentLockToken(DAVRequest request) {
        String token = getString(ATTR_LOCK_TOKEN, null);
        if ((token == null) && (request != null)) {
            token = (String) request.getState(LOCK_TOKEN);
        }
        return token;
    }

    protected long getCurrentLockTimeout(DAVRequest request) {
        long timeout = getLong(ATTR_LOCK_TIMEOUT, (long) -1);
        if (timeout == -1) {
            Long l = (Long) request.getState(LOCK_TIMEOUT);
            if (l != null) {
                return l.longValue();
            } else {
                return DEFAULT_LOCK_TIMEOUT.longValue();
            }
        } else {
            return timeout;
        }
    }

    protected Node ownerNode = null;

    /**
     * Get the lock owner
     * @return A String instance or null
     */
    protected Node getCurrentLockOwner(DAVRequest request) {
        if (ownerNode == null) {
            String owner = getString(ATTR_LOCK_OWNER, null);
            if (owner != null) {
                Base64Decoder decoder = new Base64Decoder(owner);
                try {
                    String decoded = decoder.processString();
                    ByteArrayInputStream in = new ByteArrayInputStream(decoded.getBytes());
                    Document doc = DAVBody.getDocument(in, null);
                    ownerNode = doc.getDocumentElement();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if ((owner == null) && (request != null)) {
                return (Node) request.getState(LOCK_OWNER);
            }
        }
        return ownerNode;
    }

    /**
     * Set the lock Owner
     */
    protected void setLockOwner(Node owner) {
        ownerNode = owner;
        saveLockOwner();
    }

    private synchronized void saveLockOwner() {
        if (ownerNode != null) {
            Document doc = DAVBody.createDocument(DAVNode.OWNER_NODE);
            DAVNode.exportChildren(doc, doc.getDocumentElement(), ownerNode, true);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            OutputFormat format = new OutputFormat(doc, WEBDAV.ENCODING, true);
            format.setOmitXMLDeclaration(false);
            format.setPreserveSpace(true);
            XMLSerializer serializer = new XMLSerializer(out, format);
            try {
                serializer.serialize(doc);
                if (debug) System.out.println("[" + out.toString(WEBDAV.ENCODING) + "]");
                Base64Encoder encoder = new Base64Encoder(out.toString(WEBDAV.ENCODING));
                setValue(ATTR_LOCK_OWNER, encoder.processString());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Get the lock depth  
     * @return An int (WEBDAV.DEPTH_0 or WEBDAV.DEPTH_INFINITY)
     */
    protected int getCurrentLockDepth() {
        return getInt(ATTR_LOCK_DEPTH, WEBDAV.DEPTH_INFINITY);
    }

    /**
     * Get the username of the client that lock the resource
     */
    protected String getCurrentLockUsername(DAVRequest request) {
        String username = (String) getValue(ATTR_LOCK_USERNAME, null);
        if ((username == null) && (request != null)) {
            username = (String) request.getState(LOCK_USERNAME);
        }
        return username;
    }

    private boolean hasLock() {
        return definesAttribute(ATTR_LOCK_TOKEN);
    }

    /**
     * Check if this resource or one of their parent (with depth equals
     * to infinity) has been locked
     * @param request the incomming request (it null isLocked will returns 
     * true if and only if the resource has been personally locked, not one
     * of its parents)
     * @return a boolean
     */
    protected boolean isLocked(DAVRequest request) {
        if (hasLock()) {
            if (debug) {
                System.out.println(">>> Checking timeout...");
                System.out.println(System.currentTimeMillis() + " > ");
                System.out.println(getTokenExpirationDate(null));
            }
            if (System.currentTimeMillis() > getTokenExpirationDate(null)) {
                unlock();
                return false;
            }
            return true;
        } else {
            return ((request != null) && (request.hasState(LOCK_TOKEN)));
        }
    }

    protected void setTimeout(String timeouts[]) {
        setValue(ATTR_LOCK_DATE, new Long(System.currentTimeMillis()));
        setValue(ATTR_LOCK_TIMEOUT, DEFAULT_LOCK_TIMEOUT);
        if (timeouts != null) {
            int len = timeouts.length;
            edu.hkust.clap.monitor.Monitor.loopBegin(79);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(79);
{
                if (timeouts[i].startsWith("Second-")) {
                    String sec = timeouts[i].substring(7);
                    long timeout = Long.parseLong(sec) * 1000;
                    if (timeout > MAX_LOCK_TIMEOUT.longValue()) {
                        setValue(ATTR_LOCK_TIMEOUT, MAX_LOCK_TIMEOUT);
                    } else if (timeout > 0) {
                        setValue(ATTR_LOCK_TIMEOUT, new Long(timeout));
                    } else {
                        setValue(ATTR_LOCK_TIMEOUT, DEFAULT_LOCK_TIMEOUT);
                    }
                    break;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(79);

        }
    }

    protected synchronized void refreshLock(String timeouts[]) {
        setTimeout(timeouts);
    }

    /**
     * lock this resource
     * @param token the lock token
     * @param depth the lock depth (0 or -1)
     * @param timeout the lock timeout
     */
    protected synchronized void lock(String token, int depth, String timeouts[], String username, Node owner) throws HTTPException {
        setValue(ATTR_LOCK_TOKEN, token);
        if (username != null) {
            setValue(ATTR_LOCK_USERNAME, username);
        } else if (debug) {
            System.out.println("************************************");
            System.out.println("WARNING NULL USER");
            System.out.println("************************************");
        }
        setValue(ATTR_LOCK_DEPTH, new Integer(depth));
        setLockOwner(owner);
        setTimeout(timeouts);
    }

    protected void unlock() {
        setValue(ATTR_LOCK_TOKEN, null);
        setValue(ATTR_LOCK_DEPTH, null);
        setValue(ATTR_LOCK_TIMEOUT, null);
        setValue(ATTR_LOCK_DATE, null);
        setValue(ATTR_LOCK_OWNER, null);
        setValue(ATTR_LOCK_USERNAME, null);
    }

    protected void addSupportedLock(DAVProperties prop) {
        Element sl = prop.addDAVNode(DAVNode.SUPPORTEDLOCK_NODE, null);
        Element le = DAVNode.addDAVNode(sl, DAVNode.LOCKENTRY_NODE, null);
        Element ls = DAVNode.addDAVNode(le, DAVNode.LOCKSCOPE_NODE, null);
        DAVNode.addDAVNode(ls, DAVNode.EXCLUSIVE_NODE, null);
        Element lt = DAVNode.addDAVNode(le, DAVNode.LOCKTYPE_NODE, null);
        DAVNode.addDAVNode(lt, DAVNode.WRITE_NODE, null);
    }

    protected void addLockDiscovery(DAVRequest request, DAVProperties prop) {
        addLockDiscovery(request, prop.getNode());
    }

    protected void addLockDiscovery(DAVRequest request, Node parent) {
        if (isLocked(request)) {
            Element ld = DAVNode.addDAVNode(parent, DAVNode.LOCKDISCOVERY_NODE, null);
            Element al = DAVNode.addDAVNode(ld, DAVNode.ACTIVELOCK_NODE, null);
            Element lt = DAVNode.addDAVNode(al, DAVNode.LOCKTYPE_NODE, null);
            DAVNode.addDAVNode(lt, DAVNode.WRITE_NODE, null);
            Element ls = DAVNode.addDAVNode(al, DAVNode.LOCKSCOPE_NODE, null);
            DAVNode.addDAVNode(ls, DAVNode.EXCLUSIVE_NODE, null);
            String depth = request.depthToString(getCurrentLockDepth());
            DAVNode.addDAVNode(al, DAVNode.DEPTH_NODE, depth);
            Node owner = getCurrentLockOwner(request);
            if (owner != null) {
                DAVNode.importNode(parent.getOwnerDocument(), al, owner, true);
            }
            String timeout = "Second-" + (getCurrentLockTimeout(request) / 1000);
            DAVNode.addDAVNode(al, DAVNode.TIMEOUT_NODE, timeout);
            Element ltk = DAVNode.addDAVNode(al, DAVNode.LOCKTOKEN_NODE, null);
            DAVNode.addDAVNode(ltk, DAVNode.HREF_NODE, getCurrentLockToken(request));
        }
    }

    /**
     * Get the dead properties
     * @return a ArrayDictionary instance
     * @see org.w3c.util.ArrayDictionary
     */
    public ArrayDictionary getDeadProperties() {
        return (ArrayDictionary) getValue(ATTR_DEAD_PROPERTIES, null);
    }

    protected Hashtable deadindex = null;

    protected boolean deadpropmodified = false;

    protected synchronized Hashtable getDeadPropertiesIndex() {
        if (deadindex == null) {
            ArrayDictionary dic = getDeadProperties();
            if (dic == null) {
                return new Hashtable();
            }
            Enumeration denum = dic.keys();
            deadindex = new Hashtable(dic.size());
            edu.hkust.clap.monitor.Monitor.loopBegin(80);
while (denum.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(80);
{
                String key = (String) denum.nextElement();
                Base64Decoder decoder = new Base64Decoder((String) dic.get(key));
                try {
                    String decoded = decoder.processString();
                    ByteArrayInputStream in = new ByteArrayInputStream(decoded.getBytes());
                    Document doc = DAVBody.getDocument(in, null);
                    deadindex.put(key, doc);
                } catch (Base64FormatException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (SAXException ex) {
                    ex.printStackTrace();
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(80);

        }
        return deadindex;
    }

    protected synchronized void setDeadProperty(Element el) throws DOMException {
        if (deadindex == null) {
            deadindex = new Hashtable();
        }
        String ns = el.getNamespaceURI();
        if (ns == null) {
            throw new DOMException(DOMException.NAMESPACE_ERR, "Missing namespace.");
        }
        Document doc = (Document) getDeadPropertiesIndex().get(ns);
        if (doc == null) {
            doc = DAVBody.createDocumentNS(DAVNode.PROP_NODE, ns, el.getPrefix());
            Element prop = doc.getDocumentElement();
            Node imported = doc.importNode(el, true);
            prop.appendChild(imported);
            deadindex.put(ns, doc);
            deadpropmodified = true;
        } else {
            Element prop = doc.getDocumentElement();
            DAVProperties dp = DAVFactory.createProperties(prop);
            Element props[] = dp.getProperties();
            int len = props.length;
            edu.hkust.clap.monitor.Monitor.loopBegin(81);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(81);
{
                Element p = props[i];
                if (p.getNamespaceURI().equals(ns) && p.getLocalName().equals(el.getLocalName())) {
                    prop.replaceChild(doc.importNode(el, true), p);
                    deadpropmodified = true;
                    return;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(81);

            prop.appendChild(doc.importNode(el, true));
            deadpropmodified = true;
        }
    }

    protected synchronized void removeDeadProperty(Element el) throws DOMException {
        String name = el.getNodeName();
        String ns = el.getNamespaceURI();
        if (ns == null) {
            throw new DOMException(DOMException.NAMESPACE_ERR, "Missing namespace");
        }
        Document doc = (Document) getDeadPropertiesIndex().get(ns);
        if (doc != null) {
            Element prop = doc.getDocumentElement();
            DAVProperties dp = DAVFactory.createProperties(prop);
            Element props[] = dp.getProperties();
            int len = props.length;
            edu.hkust.clap.monitor.Monitor.loopBegin(82);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(82);
{
                Element p = props[i];
                if (p.getNamespaceURI().equals(ns) && p.getLocalName().equals(el.getLocalName())) {
                    prop.removeChild(p);
                    deadpropmodified = true;
                    return;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(82);

        }
        String msg = el.getLocalName() + " property cannot be removed, not found";
        throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
    }

    protected synchronized void reloadDeadProperties() {
        deadindex = null;
        deadpropmodified = false;
    }

    protected synchronized void saveDeadProperties() {
        if (deadpropmodified) {
            ArrayDictionary dic = new ArrayDictionary(deadindex.size());
            Enumeration denum = deadindex.keys();
            edu.hkust.clap.monitor.Monitor.loopBegin(83);
while (denum.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(83);
{
                String ns = (String) denum.nextElement();
                Document doc = (Document) deadindex.get(ns);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                OutputFormat format = new OutputFormat(doc, WEBDAV.ENCODING, true);
                format.setOmitXMLDeclaration(false);
                format.setPreserveSpace(true);
                XMLSerializer serializer = new XMLSerializer(out, format);
                try {
                    serializer.serialize(doc);
                    if (debug) System.out.println("[" + out.toString(WEBDAV.ENCODING) + "]");
                    Base64Encoder encoder = new Base64Encoder(out.toString(WEBDAV.ENCODING));
                    dic.put(ns, encoder.processString());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(83);

            setValue(ATTR_DEAD_PROPERTIES, dic);
            deadpropmodified = false;
        }
    }

    /**
     * get the Allowed methods for this resource
     * @return an HttpTokenList
     */
    protected HttpTokenList getAllow() {
        allowed = super.getAllow();
        allowed.addToken("PROPFIND", false);
        allowed.addToken("PROPPATCH", false);
        allowed.addToken("MKCOL", false);
        allowed.addToken("COPY", false);
        allowed.addToken("MOVE", false);
        allowed.addToken("LOCK", false);
        allowed.addToken("UNLOCK", false);
        return allowed;
    }

    protected DAVBody getBody(DAVRequest request) throws HTTPException {
        try {
            InputStream in = request.getInputStream();
            if ((in instanceof ContentLengthInputStream) && (request.getContentLength() == 0)) {
                return null;
            }
            if (in != null) {
                return new DAVBody(in);
            }
            return null;
        } catch (Exception ex) {
            Reply error = request.makeReply(HTTP.BAD_REQUEST);
            error.setContent("Invalid request: " + ex.getMessage());
            throw new HTTPException(error);
        }
    }

    /**
     * The WEBDAV OPTIONS method replies with the DAV Header
     * @param request The request to handle.
     * @exception ProtocolException In case of errors.
     * @exception ResourceException If the resource got a fatal error.
     */
    public Reply options(Request request) throws ProtocolException, ResourceException {
        DAVReply reply = (DAVReply) super.options((Request) request);
        reply.setDAV(WEBDAV.CLASS_2_COMPLIANT);
        return reply;
    }

    /**
     * Perform the request
     * @param req The request to handle.
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public ReplyInterface perform(RequestInterface req) throws ProtocolException, ResourceException {
        if (req instanceof DAVRequest) {
            DAVRequest request = (DAVRequest) req;
            int check = checkIf(request);
            if (check == COND_FAILED) {
                request.skipBody();
                Reply reply = request.makeReply(HTTP.PRECONDITION_FAILED);
                reply.setContent("Pre-conditions failed.");
                reply.setContentMD5(null);
                return reply;
            } else if (check == COND_OK || check == 0) {
                if (isLockable(request) && isLocked(request)) {
                    request.skipBody();
                    Reply reply = request.makeReply(WEBDAV.LOCKED);
                    reply.setContent("The resource is locked");
                    return reply;
                }
            } else if ((check == COND_OK_LOCK) && (!checkLockOwner(request))) {
                Reply reply = request.makeReply(WEBDAV.LOCKED);
                reply.setContent("The resource is locked");
                return reply;
            }
        }
        return super.perform(req);
    }

    /**
     * The handler for unknown method replies with a not implemented.
     * @param request The request to handle.
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public Reply extended(Request request) throws ProtocolException, ResourceException {
        if (request instanceof DAVRequest) {
            DAVRequest davrequest = (DAVRequest) request;
            String method = davrequest.getMethod();
            Reply reply = null;
            if (method.equals("PROPFIND")) {
                reply = propfind(davrequest);
            } else if (method.equals("PROPPATCH")) {
                reply = proppatch(davrequest);
            } else if (method.equals("MKCOL")) {
                reply = mkcol(davrequest);
            } else if (method.equals("COPY")) {
                reply = copy(davrequest);
            } else if (method.equals("MOVE")) {
                reply = move(davrequest);
            } else if (method.equals("LOCK")) {
                reply = lock(davrequest);
            } else if (method.equals("UNLOCK")) {
                reply = unlock(davrequest);
            } else {
                reply = davextended(davrequest);
            }
            return reply;
        }
        String method = request.getMethod();
        Reply error = request.makeReply(HTTP.NOT_IMPLEMENTED);
        error.setContent("Method " + method + " not implemented.");
        throw new HTTPException(error);
    }

    public Reply davextended(DAVRequest request) throws ProtocolException, ResourceException {
        String method = request.getMethod();
        Reply error = request.makeReply(HTTP.NOT_IMPLEMENTED);
        error.setContent("Method " + method + " not implemented.");
        throw new HTTPException(error);
    }

    protected String decodeURL(DAVRequest request, String encoded) throws HTTPException {
        try {
            return DAVParser.decodeURL(encoded);
        } catch (HttpInvalidValueException ex) {
            Reply error = request.makeReply(HTTP.BAD_REQUEST);
            error.setContent("Invalid request: " + ex.getMessage());
            throw new HTTPException(error);
        }
    }

    protected boolean isLockable(DAVRequest request) {
        String method = request.getMethod();
        return (method.equals("PUT") || method.equals("POST") || method.equals("PROPATCH") || method.equals("LOCK") || method.equals("MOVE") || method.equals("DELETE") || method.equals("MKCOL"));
    }

    protected boolean checkLockOwner(DAVRequest request) {
        String user = (String) request.getState(AuthFilter.STATE_AUTHUSER);
        if (debug) {
            System.out.println("REQ  USER : [" + user + "]");
            System.out.println("LOCK USER : [" + getCurrentLockUsername(request) + "]");
        }
        String lockuser = getCurrentLockUsername(request);
        if (lockuser == null) {
            return true;
        }
        if (user != null) {
            return user.equals(lockuser);
        } else {
            return false;
        }
    }

    /**
     * Check the <code>If</code> condition of that request.
     * @param request The request to check.
     * @return An integer, either <code>COND_FAILED</cond> if condition
     * was checked, but failed, <code>COND_OK</code> if condition was checked
     * and succeeded, or <strong>0</strong> if the condition was not checked
     * at all (eg because the resource or the request didn't support it).
     */
    protected int checkIf(DAVRequest request) throws HTTPException {
        URL url = getURL(request);
        String surl = url.toExternalForm();
        DAVIf ifs[] = request.getIf();
        if (ifs != null) {
            if (debug) {
                System.out.println(">>> Check If Header...");
            }
            int len = ifs.length;
            boolean tagged = request.isTaggedListIfHeader();
            boolean lockchecked = false;
            edu.hkust.clap.monitor.Monitor.loopBegin(84);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(84);
{
                DAVIf dif = ifs[i];
                boolean parent = false;
                if (dif.hasResource()) {
                    URL resURL = null;
                    try {
                        resURL = new URL(url, dif.getResource());
                    } catch (MalformedURLException ex) {
                        ex.printStackTrace();
                        continue;
                    }
                    if (!resURL.equals(url)) {
                        if (surl.startsWith(resURL.toExternalForm())) {
                            parent = true;
                        } else {
                            continue;
                        }
                    }
                }
                ListIterator it = dif.getTokenListIterator();
                edu.hkust.clap.monitor.Monitor.loopBegin(85);
while (it.hasNext()) { 
edu.hkust.clap.monitor.Monitor.loopInc(85);
{
                    boolean checked = true;
                    boolean not = false;
                    LinkedList list = (LinkedList) it.next();
                    ListIterator it2 = list.listIterator(0);
                    edu.hkust.clap.monitor.Monitor.loopBegin(86);
while (it2.hasNext()) { 
edu.hkust.clap.monitor.Monitor.loopInc(86);
{
                        Object tok = it2.next();
                        if (tok instanceof DAVStateToken) {
                            DAVStateToken dst = (DAVStateToken) tok;
                            if (matchLockToken(request, dst.getStateToken())) {
                                if (dst.isNot()) {
                                    checked = false;
                                    not = false;
                                } else {
                                    if (debug) System.out.println(">>> Lock checked");
                                    lockchecked = true;
                                }
                            } else if (!dst.isNot()) {
                                checked = false;
                            }
                        } else if ((!parent) && (tok instanceof DAVEntityTag)) {
                            DAVEntityTag det = (DAVEntityTag) tok;
                            if (matchETag(det)) {
                                if (debug) System.out.println(">>> ETag checked");
                                if (det.isNot()) {
                                    checked = false;
                                    not = false;
                                }
                            } else if (!det.isNot()) {
                                checked = false;
                            }
                        }
                    }} 
edu.hkust.clap.monitor.Monitor.loopEnd(86);

                    if (!parent) {
                        if (checked && lockchecked) {
                            return COND_OK_LOCK;
                        } else if (checked) {
                            return COND_OK;
                        }
                    }
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(85);

            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(84);

            if (tagged) {
                if (lockchecked) {
                    return COND_OK_LOCK;
                } else {
                    return 0;
                }
            } else {
                return COND_FAILED;
            }
        }
        return 0;
    }

    protected boolean matchETag(HttpEntityTag retag) {
        HttpEntityTag etag = getETag();
        if (etag != null) {
            return etag.getTag().equals(retag.getTag());
        }
        return false;
    }

    protected boolean matchLockToken(DAVRequest request, String locktoken) {
        return locktoken.equals(getCurrentLockToken(request));
    }

    /**
     * The WEBDAV DELETE method, actually the resource (file, directory)
     * is moved into the trash directory which is not accessible via HTTP.
     * @param request The request to handle.
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public Reply delete(Request request) throws ProtocolException, ResourceException {
        return super.delete(request);
    }

    /**
     * Get this resource body.
     * If we are allowed to convert GET requests to POST, than we first
     * check to see if there is some search string in the request, and continue
     * with normal POST request processing.
     * <p>If there is no search string, or if we are not allowed to convert
     * GETs to POSTs, than we just invoke our <code>super</code> method,
     * which will perform the appropriate job.
     * @param request The request to handle.
     * @exception ProtocolException If request couldn't be processed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public Reply get(Request request) throws ProtocolException, ResourceException {
        return super.get(request);
    }

    /**
     * The WEBDAV PUT method.
     * @param request The request to handle.
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public Reply put(Request request) throws ProtocolException, ResourceException {
        return super.put(request);
    }

    /**
     * Perform the post method.
     * @param request The request to handle.
     * @exception ProtocolException If request couldn't be processed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public Reply post(Request request) throws ProtocolException, ResourceException {
        return super.post(request);
    }

    private HTTPException notYetImplemented(DAVRequest request) throws ProtocolException {
        Reply error = request.makeReply(HTTP.NOT_IMPLEMENTED);
        error.setContent("Method " + request.getMethod() + " not implemented.");
        throw new HTTPException(error);
    }

    protected boolean hasProperty(int idx) {
        return (getValue(idx, null) != null);
    }

    protected boolean hasStringProperty(int idx) {
        String value = (String) getValue(idx, "");
        return (value.length() > 0);
    }

    protected boolean hasLongProperty(int idx) {
        return (getLong(idx, -1) != -1);
    }

    protected boolean hasIntProperty(int idx) {
        return (getInt(idx, -1) != -1);
    }

    protected void addCreationDate(DAVProperties props) {
        Date d = new Date(getCreationDate());
        String isodate = DateParser.getIsoDateNoMillis(d);
        props.addProperty(DAVNode.CREATIONDATE_NODE, isodate);
    }

    protected void addDisplayName(DAVProperties props) {
        String title = getTitle();
        if ((title != null) && (title.length() > 0)) {
            props.addProperty(DAVNode.DISPLAYNAME_NODE, title);
        }
    }

    protected void addContentLanguage(DAVProperties props) {
        String lang = getContentLanguage();
        if (lang != null) {
            props.addProperty(DAVNode.GETCONTENTLANGUAGE_NODE, lang);
        }
    }

    protected void addContentType(DAVProperties props) {
        MimeType mime = getContentType();
        if (mime != null) {
            props.addProperty(DAVNode.GETCONTENTTYPE_NODE, mime.toString());
        }
    }

    private void addCollectionContentType(DAVProperties props) {
        props.addProperty(DAVNode.GETCONTENTTYPE_NODE, collectioncontenttype.toString());
    }

    protected void addContentLength(DAVProperties props) {
        int length = getContentLength();
        if (length >= 0) {
            props.addProperty(DAVNode.GETCONTENTLENGTH_NODE, String.valueOf(length));
        }
    }

    protected void addETag(DAVProperties props) {
        HttpEntityTag tag = getETag();
        if (tag != null) {
            props.addProperty(DAVNode.GETETAG_NODE, tag.getTag());
        }
    }

    protected void addLastModified(DAVProperties props) {
        HttpDate hdate = HttpFactory.makeDate(getLastModified());
        String httpdate = hdate.toString();
        props.addProperty(DAVNode.GETLASTMODIFIED_NODE, httpdate);
    }

    protected void addResourceType(DAVProperties props) {
        if (isCollection()) {
            props.setResourceType(DAVNode.COLLECTION_NODE);
        } else {
            props.setResourceType(null);
        }
    }

    protected void addIsCollection(DAVProperties props) {
        props.addProperty(DAVNode.ISCOLLECTION_NODE, isCollection() ? "1" : "0");
    }

    /**
     * Get the properties of our associated resource require in
     * the given DAVProperties Object.
     * @param doc the response XML Document
     * @param dp the DAVProperties node in found the request body
     * @return a DAVProperties instance 
     */
    protected DAVProperties getProperties(DAVRequest request, Document doc, DAVProperties dp) {
        DAVProperties props = DAVFactory.createProperties(doc);
        Element[] els = dp.getProperties();
        int len = els.length;
        edu.hkust.clap.monitor.Monitor.loopBegin(87);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(87);
{
            Element el = els[i];
            String ns = el.getNamespaceURI();
            String propname = el.getLocalName();
            if ((ns == null) || (ns.equals(WEBDAV.NAMESPACE_URI))) {
                if (propname.equals(DAVNode.CREATIONDATE_NODE)) {
                    addCreationDate(props);
                } else if (propname.equals(DAVNode.DISPLAYNAME_NODE)) {
                    addDisplayName(props);
                } else if (propname.equals(DAVNode.GETCONTENTLANGUAGE_NODE)) {
                    addContentLanguage(props);
                } else if (propname.equals(DAVNode.GETCONTENTTYPE_NODE)) {
                    addContentType(props);
                } else if (propname.equals(DAVNode.GETCONTENTLENGTH_NODE)) {
                    addContentLength(props);
                } else if (propname.equals(DAVNode.GETETAG_NODE)) {
                    addETag(props);
                } else if (propname.equals(DAVNode.GETLASTMODIFIED_NODE)) {
                    addLastModified(props);
                } else if (propname.equals(DAVNode.LOCKDISCOVERY_NODE)) {
                    addLockDiscovery(request, props);
                } else if (propname.equals(DAVNode.RESOURCETYPE_NODE)) {
                    addResourceType(props);
                } else if (propname.equals(DAVNode.SOURCE_NODE)) {
                } else if (propname.equals(DAVNode.SUPPORTEDLOCK_NODE)) {
                    addSupportedLock(props);
                } else {
                }
            } else {
                Document pdoc = (Document) getDeadPropertiesIndex().get(ns);
                if (pdoc != null) {
                    Element prop = pdoc.getDocumentElement();
                    DAVProperties dpns = DAVFactory.createProperties(prop);
                    Node pnode = dpns.getNodeNS(propname, ns);
                    if (pnode != null) {
                        props.addNodeNS(doc.importNode(pnode, true));
                    }
                }
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(87);

        return props;
    }

    protected DAVPropStat[] getPropStat(DAVRequest request, Document doc, DAVProperties dp) {
        DAVProperties props = DAVFactory.createProperties(doc);
        DAVProperties nfprops = null;
        Element[] els = dp.getProperties();
        int len = els.length;
        edu.hkust.clap.monitor.Monitor.loopBegin(88);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(88);
{
            Element el = els[i];
            String ns = el.getNamespaceURI();
            String propname = el.getLocalName();
            if ((ns == null) || (ns.equals(WEBDAV.NAMESPACE_URI))) {
                if (propname.equals(DAVNode.CREATIONDATE_NODE)) {
                    addCreationDate(props);
                } else if (propname.equals(DAVNode.DISPLAYNAME_NODE)) {
                    addDisplayName(props);
                } else if (propname.equals(DAVNode.GETCONTENTLANGUAGE_NODE)) {
                    addContentLanguage(props);
                } else if (propname.equals(DAVNode.GETCONTENTTYPE_NODE)) {
                    addContentType(props);
                } else if (propname.equals(DAVNode.GETCONTENTLENGTH_NODE)) {
                    addContentLength(props);
                } else if (propname.equals(DAVNode.GETETAG_NODE)) {
                    addETag(props);
                } else if (propname.equals(DAVNode.GETLASTMODIFIED_NODE)) {
                    addLastModified(props);
                } else if (propname.equals(DAVNode.LOCKDISCOVERY_NODE)) {
                    addLockDiscovery(request, props);
                } else if (propname.equals(DAVNode.RESOURCETYPE_NODE)) {
                    addResourceType(props);
                } else if (propname.equals(DAVNode.SOURCE_NODE)) {
                } else if (propname.equals(DAVNode.SUPPORTEDLOCK_NODE)) {
                    addSupportedLock(props);
                } else {
                    if (nfprops == null) {
                        nfprops = DAVFactory.createProperties(doc);
                    }
                    Element e;
                    e = doc.createElementNS("http://www.w3.org/Jigsaw/Webdav/", propname);
                    e.setPrefix("F");
                    nfprops.addNodeNS(nfprops.getNode(), (Node) e);
                }
            } else {
                Document pdoc = (Document) getDeadPropertiesIndex().get(ns);
                if (pdoc != null) {
                    Element prop = pdoc.getDocumentElement();
                    DAVProperties dpns = DAVFactory.createProperties(prop);
                    Node pnode = dpns.getNodeNS(propname, ns);
                    if (pnode != null) {
                        props.addNodeNS(doc.importNode(pnode, true));
                    }
                }
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(88);

        DAVPropStat[] dps = null;
        if (nfprops != null) {
            dps = new DAVPropStat[2];
            dps[1] = DAVFactory.createPropStat(getStatusLine(HTTP.NOT_FOUND), doc);
            dps[1].addDAVNode(nfprops);
        } else {
            dps = new DAVPropStat[1];
        }
        dps[0] = DAVFactory.createPropStat(getStatusLine(HTTP.OK), doc);
        dps[0].addDAVNode(props);
        return dps;
    }

    /**
     * Get all the properties of our associated resource.
     * @param doc the response XML Document
     * @return a DAVProperties instance 
     */
    protected DAVProperties getProperties(DAVRequest request, Document doc) {
        DAVProperties props = DAVFactory.createProperties(doc);
        addCreationDate(props);
        addDisplayName(props);
        addContentLanguage(props);
        addContentLength(props);
        addETag(props);
        addLastModified(props);
        if (isCollection()) {
            addCollectionContentType(props);
        } else {
            addContentType(props);
        }
        addLockDiscovery(request, props);
        addResourceType(props);
        addSupportedLock(props);
        addIsCollection(props);
        Hashtable dp = getDeadPropertiesIndex();
        Enumeration e = dp.keys();
        edu.hkust.clap.monitor.Monitor.loopBegin(89);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(89);
{
            String ns = (String) e.nextElement();
            Document d = (Document) dp.get(ns);
            Element el = d.getDocumentElement();
            try {
                DAVNode.exportChildren(doc, props.getNode(), el, true);
            } catch (DOMException ex) {
                ex.printStackTrace();
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(89);

        return props;
    }

    protected DAVProperties getPropertiesForCopy(Document doc) {
        DAVProperties props = DAVFactory.createProperties(doc);
        addDisplayName(props);
        addContentLanguage(props);
        addContentType(props);
        Hashtable dp = getDeadPropertiesIndex();
        Enumeration e = dp.keys();
        edu.hkust.clap.monitor.Monitor.loopBegin(89);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(89);
{
            String ns = (String) e.nextElement();
            Document d = (Document) dp.get(ns);
            Element el = d.getDocumentElement();
            try {
                DAVNode.exportChildren(doc, props.getNode(), el, true);
            } catch (DOMException ex) {
                ex.printStackTrace();
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(89);

        return props;
    }

    /**
     * Get all the property names of our associated resource.
     * @param doc the response XML Document
     * @return a DAVProperties instance 
     */
    protected DAVProperties getPropNames(DAVRequest request, Document doc) {
        DAVProperties props = DAVFactory.createProperties(doc);
        Hashtable dp = getDeadPropertiesIndex();
        Enumeration e = dp.keys();
        edu.hkust.clap.monitor.Monitor.loopBegin(90);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(90);
{
            String ns = (String) e.nextElement();
            Document d = (Document) dp.get(ns);
            Element el = d.getDocumentElement();
            try {
                DAVNode.exportChildren(doc, props.getNode(), el, false);
            } catch (DOMException ex) {
                ex.printStackTrace();
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(90);

        if (hasStringProperty(ATTR_TITLE)) {
            props.addProperty(DAVNode.DISPLAYNAME_NODE);
        }
        if (hasStringProperty(ATTR_CONTENT_LANGUAGE)) {
            props.addProperty(DAVNode.GETCONTENTLANGUAGE_NODE);
        }
        if (getETag() != null) {
            props.addProperty(DAVNode.GETETAG_NODE);
        }
        if (hasProperty(ATTR_CONTENT_TYPE)) {
            props.addProperty(DAVNode.GETCONTENTTYPE_NODE);
        }
        if (hasIntProperty(ATTR_CONTENT_LENGTH)) {
            props.addProperty(DAVNode.GETCONTENTLENGTH_NODE);
        }
        if (hasLongProperty(ATTR_CREATION_DATE)) {
            props.addProperty(DAVNode.CREATIONDATE_NODE);
        }
        if (getCurrentLockToken(request) != null) {
            props.addProperty(DAVNode.LOCKDISCOVERY_NODE);
        }
        props.addProperty(DAVNode.GETLASTMODIFIED_NODE);
        props.addProperty(DAVNode.RESOURCETYPE_NODE);
        props.addProperty(DAVNode.SUPPORTEDLOCK_NODE);
        return props;
    }

    /**
     * Get an appropriate DAV Response to the given PROPFIND request
     * @param request the DAV Request
     * @param dpf the DAVPropFind XML Node (can be null)
     * @param document the XML document
     * @return a DAVResponse
     */
    protected DAVResponse getResponse(DAVRequest request, DAVPropFind dpf, Document document) {
        if (fresource != null) {
            fresource.checkContent();
        }
        updateCachedHeaders();
        DAVProperties props = null;
        if (dpf != null) {
            if (dpf.findPropNames()) {
                props = getPropNames(request, document);
            } else if (dpf.findAllProps()) {
                props = getProperties(request, document);
            } else {
                DAVProperties dp = dpf.getProperties();
                DAVPropStat[] dps = getPropStat(request, document, dp);
                DAVResponse dr = DAVFactory.createResponse(getURL(request).getFile(), document);
                edu.hkust.clap.monitor.Monitor.loopBegin(91);
for (int i = 0; i < dps.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(91);
{
                    dr.addDAVNode(dps[i]);
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(91);

                return dr;
            }
        } else {
            props = getProperties(request, document);
        }
        return DAVFactory.createPropStatResponse(getURL(request).getFile(), getStatusLine(HTTP.OK), props, document);
    }

    /**
     * Get an appropriate DAV Response to the given PROPFIND request from
     * our children
     * @param request the DAV Request
     * @param dpf the DAVPropFind XML Node (can be null)
     * @param document the XML document
     * @param deep do it recursivly if true
     * @return a DAVResponse array
     */
    protected DAVResponse[] getChildResponses(DAVRequest request, DAVPropFind dpf, Document document, boolean deep) {
        if (resource instanceof ContainerResource) {
            Vector v = new Vector();
            ContainerResource cresource = (ContainerResource) resource;
            Enumeration e = cresource.enumerateResourceIdentifiers();
            ResourceReference rr = null;
            FramedResource fr = null;
            edu.hkust.clap.monitor.Monitor.loopBegin(92);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(92);
{
                String name = (String) e.nextElement();
                rr = cresource.lookup(name);
                if (rr != null) {
                    try {
                        fr = (FramedResource) rr.lock();
                        if (fr == resource) {
                            continue;
                        }
                        DAVFrame df = (DAVFrame) fr.getFrame(DAVFrame.class);
                        if (df != null) {
                            v.addElement(df.getResponse(request, dpf, document));
                            if (deep && (fr instanceof ContainerResource)) {
                                DAVResponse responses[] = df.getChildResponses(request, dpf, document, deep);
                                if (responses != null) {
                                    int len = responses.length;
                                    edu.hkust.clap.monitor.Monitor.loopBegin(93);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(93);
{
                                        v.addElement(responses[i]);
                                    }} 
edu.hkust.clap.monitor.Monitor.loopEnd(93);

                                }
                            }
                        } else {
                        }
                    } catch (InvalidResourceException ex) {
                    } finally {
                        rr.unlock();
                    }
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(92);

            DAVResponse responses[] = new DAVResponse[v.size()];
            v.copyInto(responses);
            return responses;
        }
        return null;
    }

    /**
     * Create a DAV reply for a specific DAV request.
     * @param request the DAVRequest
     * @param status the status of the reply
     * @param document the XML content of the reply
     * @return a Reply instance
     */
    protected Reply createDAVReply(DAVRequest request, int status, Document document) {
        Reply reply = request.makeReply(status);
        reply.setHeaderValue(Reply.H_CONTENT_TYPE, xmlcontenttype);
        reply.setDate((System.currentTimeMillis() / 1000L) * 1000L);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputFormat format = new OutputFormat(document, WEBDAV.ENCODING, true);
        format.setOmitXMLDeclaration(false);
        format.setPreserveSpace(false);
        XMLSerializer serializer = new XMLSerializer(out, format);
        try {
            serializer.serialize(document);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (debugxml) {
            serializer = new XMLSerializer(System.out, format);
            try {
                serializer.serialize(document);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        int len = out.size();
        reply.setContentLength(len);
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        reply.setStream(in);
        return reply;
    }

    protected String getStatusLine(int status) {
        return "HTTP/1.1 " + status + " " + DAVReply.getDAVReason(status);
    }

    /**
     * Handle the PROPFIND request.
     * @param request the WEBDAV request
     * @return a Reply instance
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public Reply propfind(DAVRequest request) throws ProtocolException, ResourceException {
        int depth = request.getDepth();
        DAVBody body = getBody(request);
        Document document = DAVBody.createDocument(DAVNode.MULTISTATUS_NODE);
        DAVMultiStatus dms = DAVFactory.createMultiStatus(document.getDocumentElement());
        DAVPropFind propfind = null;
        DAVResponse dr = null;
        if (body != null) {
            propfind = body.getPropFind();
        }
        switch(depth) {
            case 0:
                dr = getResponse(request, propfind, document);
                dms.addDAVNode(dr);
                break;
            case 1:
                dr = getResponse(request, propfind, document);
                dms.addDAVNode(dr);
                dms.addDAVNodes(getChildResponses(request, propfind, document, false));
                break;
            default:
                dr = getResponse(request, propfind, document);
                dms.addDAVNode(dr);
                dms.addDAVNodes(getChildResponses(request, propfind, document, true));
        }
        return createDAVReply(request, WEBDAV.MULTI_STATUS, document);
    }

    protected DAVPropStat setDAVProperties(DAVProperties props, Document document) throws DAVPropertyException {
        Element properties[] = props.getProperties();
        int len = properties.length;
        DAVProperties okdp = DAVFactory.createProperties(document);
        try {
            edu.hkust.clap.monitor.Monitor.loopBegin(94);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(94);
{
                Element el = properties[i];
                String ns = el.getNamespaceURI();
                if ((ns == null) || (ns.equals(WEBDAV.NAMESPACE_URI))) {
                    setLiveProperty(DAVFactory.createDAVNode(el), okdp, document);
                } else {
                    setDeadProperty(DAVFactory.createDAVNode(el), okdp, document);
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(94);

        } catch (DAVPropertyException ex) {
            DAVPropStat dps = (DAVPropStat) ex.getReason();
            String msg = ex.getMessage();
            DAVPropStat stats[] = null;
            if (okdp.getNode().hasChildNodes()) {
                DAVPropStat okdps = DAVFactory.createPropStat(getStatusLine(HTTP.OK), okdp, document);
                stats = new DAVPropStat[2];
                stats[0] = okdps;
                stats[1] = dps;
            } else {
                stats = new DAVPropStat[1];
                stats[0] = dps;
            }
            throw new DAVPropertyException(msg, stats);
        }
        if (okdp.getNode().hasChildNodes()) {
            return DAVFactory.createPropStat(getStatusLine(HTTP.OK), okdp, document);
        }
        return null;
    }

    protected void setDeadProperty(DAVNode node, DAVProperties okdp, Document document) throws DAVPropertyException {
        Node n = node.getNode();
        String name = n.getLocalName();
        try {
            if (n.getNodeType() == n.ELEMENT_NODE) {
                Element el = (Element) n;
                setDeadProperty(el);
                okdp.addNodeNS(document.importNode(el, false));
            } else {
                DAVPropStat dps = DAVFactory.createPropStatNS(getStatusLine(HTTP.CONFLICT), document.importNode(n, false), document);
                String msg = "Invalid XML : " + name;
                throw new DAVPropertyException(msg, dps);
            }
        } catch (DOMException ex) {
            DAVPropStat dps = DAVFactory.createPropStatNS(getStatusLine(HTTP.CONFLICT), document.importNode(n, false), document);
            throw new DAVPropertyException(ex.getMessage(), dps);
        }
    }

    protected void setLiveProperty(DAVNode node, DAVProperties okdp, Document document) throws DAVPropertyException {
        String name = node.getNode().getLocalName();
        String value = node.getTextValue();
        if (name.equals(DAVNode.DISPLAYNAME_NODE)) {
            setValue(ATTR_TITLE, value);
            okdp.addProperty(name);
        } else if (name.equals(DAVNode.GETCONTENTLANGUAGE_NODE)) {
            setValue(ATTR_CONTENT_LANGUAGE, value);
            okdp.addProperty(name);
        } else if (name.equals(DAVNode.SOURCE_NODE)) {
        } else if (name.equals(DAVNode.RESOURCETYPE_NODE)) {
        } else if (name.equals(DAVNode.GETCONTENTTYPE_NODE)) {
            try {
                MimeType mime = new MimeType(value);
                setValue(ATTR_CONTENT_TYPE, mime);
                okdp.addProperty(name);
            } catch (MimeTypeFormatException ex) {
                DAVPropStat dps = DAVFactory.createPropStat(getStatusLine(HTTP.CONFLICT), name, document);
                String msg = "Invalid value for " + name + " : " + value;
                throw new DAVPropertyException(msg, dps);
            }
        } else if (isReadOnly(name)) {
            DAVPropStat dps = DAVFactory.createPropStat(getStatusLine(HTTP.FORBIDDEN), name, document);
            throw new DAVPropertyException(name + " cannot be modified", dps);
        } else {
            setDeadProperty(node, okdp, document);
        }
    }

    protected DAVPropStat removeDAVProperties(DAVProperties props, Document document) throws DAVPropertyException {
        Element properties[] = props.getProperties();
        int len = properties.length;
        DAVProperties okdp = DAVFactory.createProperties(document);
        try {
            edu.hkust.clap.monitor.Monitor.loopBegin(95);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(95);
{
                Element el = properties[i];
                String ns = el.getNamespaceURI();
                if ((ns == null) || (ns.equals(WEBDAV.NAMESPACE_URI))) {
                    removeLiveProperty(DAVFactory.createDAVNode(el), okdp, document);
                } else {
                    removeDeadProperty(DAVFactory.createDAVNode(el), okdp, document);
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(95);

        } catch (DAVPropertyException ex) {
            DAVPropStat dps = (DAVPropStat) ex.getReason();
            String msg = ex.getMessage();
            DAVPropStat stats[] = null;
            if (okdp.getNode().hasChildNodes()) {
                DAVPropStat okdps = DAVFactory.createPropStat(getStatusLine(HTTP.OK), okdp, document);
                stats = new DAVPropStat[2];
                stats[0] = okdps;
                stats[1] = dps;
            } else {
                stats = new DAVPropStat[1];
                stats[0] = dps;
            }
            throw new DAVPropertyException(msg, stats);
        }
        if (okdp.getNode().hasChildNodes()) {
            return DAVFactory.createPropStat(getStatusLine(HTTP.OK), okdp, document);
        }
        return null;
    }

    protected void removeLiveProperty(DAVNode node, DAVProperties okdp, Document document) throws DAVPropertyException {
        String name = node.getNode().getLocalName();
        String value = node.getTextValue();
        if (name.equals(DAVNode.DISPLAYNAME_NODE)) {
            setValue(ATTR_TITLE, "");
            okdp.addProperty(name);
        } else if (name.equals(DAVNode.GETCONTENTLANGUAGE_NODE)) {
            setValue(ATTR_CONTENT_LANGUAGE, null);
            okdp.addProperty(name);
        } else {
            try {
                removeDeadProperty(node, okdp, document);
            } catch (DAVPropertyException ex) {
                DAVPropStat edps = DAVFactory.createPropStat(getStatusLine(HTTP.CONFLICT), name, document);
                throw new DAVPropertyException(name + " cannot be removed", edps);
            }
        }
    }

    protected void removeDeadProperty(DAVNode node, DAVProperties okdp, Document document) throws DAVPropertyException {
        Node n = node.getNode();
        String name = n.getLocalName();
        try {
            if (n.getNodeType() == n.ELEMENT_NODE) {
                Element el = (Element) n;
                removeDeadProperty(el);
                okdp.addNodeNS(document.importNode(el, false));
            } else {
                DAVPropStat dps = DAVFactory.createPropStatNS(getStatusLine(HTTP.CONFLICT), document.importNode(n, false), document);
                String msg = "Invalid XML : " + name;
                throw new DAVPropertyException(msg, dps);
            }
        } catch (DOMException ex) {
            DAVPropStat dps = DAVFactory.createPropStatNS(getStatusLine(HTTP.CONFLICT), document.importNode(n, false), document);
            throw new DAVPropertyException(ex.getMessage(), dps);
        }
    }

    /**
     * Handle the PROPPATCH request.
     * @param request the WEBDAV request
     * @return a Reply instance
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public Reply proppatch(DAVRequest request) throws ProtocolException, ResourceException {
        Object oldvalues[] = this.values;
        DAVBody body = getBody(request);
        Document document = DAVBody.createDocument(DAVNode.MULTISTATUS_NODE);
        DAVResponse dr = DAVFactory.createResponse(getURL(request).getFile(), document);
        document.getDocumentElement().appendChild(dr.getNode());
        if (body != null) {
            DAVPropertyUpdate dpu = body.getPropertyUpdate();
            if (dpu != null) {
                DAVPropAction dpas[] = dpu.getActions();
                DAVPropStat dps = null;
                int len = dpas.length;
                try {
                    edu.hkust.clap.monitor.Monitor.loopBegin(96);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(96);
{
                        DAVPropAction dpa = dpas[i];
                        switch(dpa.getAction()) {
                            case DAVPropAction.SET:
                                dps = setDAVProperties(dpa.getProperties(), document);
                                break;
                            case DAVPropAction.REMOVE:
                                dps = removeDAVProperties(dpa.getProperties(), document);
                                break;
                            default:
                                Reply error = request.makeReply(HTTP.BAD_REQUEST);
                                error.setContent("Invalid request");
                                throw new HTTPException(error);
                        }
                        if (dps != null) {
                            dr.addDAVNode(dps);
                        }
                    }} 
edu.hkust.clap.monitor.Monitor.loopEnd(96);

                    saveDeadProperties();
                } catch (DAVPropertyException ex) {
                    reloadDeadProperties();
                    this.values = oldvalues;
                    DAVPropStat dpss[] = (DAVPropStat[]) ex.getReason();
                    edu.hkust.clap.monitor.Monitor.loopBegin(97);
for (int j = 0; j < dpss.length; j++) { 
edu.hkust.clap.monitor.Monitor.loopInc(97);
{
                        dr.addDAVNode(dpss[j]);
                    }} 
edu.hkust.clap.monitor.Monitor.loopEnd(97);

                    dr.setDescription(ex.getMessage());
                }
                return createDAVReply(request, WEBDAV.MULTI_STATUS, document);
            }
        }
        Reply error = request.makeReply(HTTP.BAD_REQUEST);
        error.setContent("Invalid request");
        throw new HTTPException(error);
    }

    /**
     * Handle the MKCOL request.
     * @param request the WEBDAV request
     * @return a Reply instance
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public Reply mkcol(DAVRequest request) throws ProtocolException, ResourceException {
        if (dresource != null) {
            String newcol = (String) request.getState(REMAINING_PATH);
            if (newcol != null) {
                if (dresource.getExtensibleFlag() && getPutableFlag()) {
                    ResourceReference rr = dresource.createDirectoryResource(newcol);
                    if (rr == null) {
                        Reply error = request.makeReply(HTTP.UNSUPPORTED_MEDIA_TYPE);
                        error.setContent("Failed to create collection " + newcol);
                        throw new HTTPException(error);
                    } else {
                        Reply reply = request.makeReply(HTTP.CREATED);
                        reply.setContent("<P>Collection " + newcol + " succesfully created");
                        return reply;
                    }
                } else {
                    Reply error = request.makeReply(HTTP.FORBIDDEN);
                    error.setContent("The server doen't allow collection " + "creation here.");
                    throw new HTTPException(error);
                }
            } else {
                Reply error = request.makeReply(HTTP.NOT_ALLOWED);
                error.setContent(dresource.getIdentifier() + " already exists!");
                throw new HTTPException(error);
            }
        } else {
            Reply error = request.makeReply(HTTP.NOT_ALLOWED);
            error.setContent(dresource.getIdentifier() + " already exists!");
            throw new HTTPException(error);
        }
    }

    protected org.w3c.www.protocol.webdav.DAVRequest createRequest(DAVRequest req, String mtd, URL url, Hashtable headers, InputStream in) {
        org.w3c.www.protocol.webdav.DAVRequest request = null;
        request = manager.createDAVRequest();
        request.setURL(url);
        request.setMethod(mtd);
        if (req.hasAuthorization()) {
            request.setAuthorization(req.getAuthorization());
        }
        if (headers != null) {
            Enumeration keys = headers.keys();
            edu.hkust.clap.monitor.Monitor.loopBegin(98);
while (keys.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(98);
{
                String header = (String) keys.nextElement();
                String value = (String) headers.get(header);
                request.setValue(header, value);
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(98);

        }
        if (in != null) {
            request.setOutputStream(in);
        }
        return request;
    }

    protected org.w3c.www.protocol.webdav.DAVRequest createRequest(DAVRequest request, String mtd, URL url, Hashtable headers) {
        return createRequest(request, mtd, url, headers, (InputStream) null);
    }

    protected org.w3c.www.protocol.webdav.DAVRequest createRequest(DAVRequest request, String mtd, URL url, Hashtable headers, Document document) {
        if (document != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            OutputFormat format = new OutputFormat(document, WEBDAV.ENCODING, true);
            format.setOmitXMLDeclaration(false);
            format.setPreserveSpace(false);
            XMLSerializer serializer = new XMLSerializer(out, format);
            try {
                serializer.serialize(document);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            int len = out.size();
            if (headers == null) {
                headers = new Hashtable();
            }
            headers.put("Content-Length", String.valueOf(len));
            headers.put("Content-Type", xmlcontenttype.toString());
            InputStream in = new ByteArrayInputStream(out.toByteArray());
            return createRequest(request, mtd, url, headers, in);
        } else {
            return createRequest(request, mtd, url, headers, (InputStream) null);
        }
    }

    protected DAVRequest createInternalRequest(DAVRequest request, String mtd, URL url, Hashtable headers, Document document) {
        if (document != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            OutputFormat format = new OutputFormat(document, WEBDAV.ENCODING, true);
            format.setOmitXMLDeclaration(false);
            format.setPreserveSpace(false);
            XMLSerializer serializer = new XMLSerializer(out, format);
            try {
                serializer.serialize(document);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            int len = out.size();
            if (headers == null) {
                headers = new Hashtable();
            }
            headers.put("Content-Length", String.valueOf(len));
            headers.put("Content-Type", xmlcontenttype.toString());
            InputStream in = new ByteArrayInputStream(out.toByteArray());
            return createInternalRequest(request, mtd, url, headers, in);
        } else {
            return createInternalRequest(request, mtd, url, headers, (InputStream) null);
        }
    }

    /**
     * Should be called to purge the reply, otherwise the connection won't
     * be marked idle and won't be reused.
     * @param reply the reply to purge.
     */
    protected void skipBody(org.w3c.www.protocol.webdav.DAVReply reply) {
        try {
            if (reply.hasInputStream()) {
                InputStream in = reply.getInputStream();
                byte buffer[] = new byte[256];
                try {
                    if (debug) {
                        System.out.println(">>> skipping body <<<<");
                        int nb = -1;
                        edu.hkust.clap.monitor.Monitor.loopBegin(99);
while ((nb = in.read(buffer)) != -1) { 
edu.hkust.clap.monitor.Monitor.loopInc(99);
{
                            System.out.print(new String(buffer, 0, nb));
                        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(99);

                        System.out.println(">>> end body <<<<\n");
                    } else {
                        edu.hkust.clap.monitor.Monitor.loopBegin(100);
while (in.read(buffer) != -1) { 
edu.hkust.clap.monitor.Monitor.loopInc(100);
;} 
edu.hkust.clap.monitor.Monitor.loopEnd(100);

                    }
                } catch (IOException ioex) {
                    try {
                        in.close();
                    } catch (IOException ioex2) {
                    }
                }
            }
        } catch (IOException ex) {
        }
    }

    protected void closeInternalReply(DAVReply reply) {
        if (reply.hasStream()) {
            try {
                reply.openStream().close();
                reply.setStream((InputStream) null);
                reply.setContentLength(0);
            } catch (IOException ex) {
            }
        }
    }

    protected DAVRequest createInternalRequest(DAVRequest req, String mtd, URL url, Hashtable headers, InputStream in) {
        DAVRequest request;
        request = (DAVRequest) req.getClone();
        request.setURL(url);
        request.setMethod(mtd);
        if (req.hasAuthorization()) {
            request.setAuthorization(req.getAuthorization());
        }
        if (headers != null) {
            Enumeration keys = headers.keys();
            edu.hkust.clap.monitor.Monitor.loopBegin(98);
while (keys.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(98);
{
                String header = (String) keys.nextElement();
                String value = (String) headers.get(header);
                request.setValue(header, value);
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(98);

        }
        if (in != null) {
            request.setStream(in);
        }
        request.setInternal(true);
        return request;
    }

    protected DAVRequest createInternalRequest(DAVRequest req, String mtd, URL url, Hashtable headers) {
        return createInternalRequest(req, mtd, url, headers, (InputStream) null);
    }

    private Reply internalCopy(DAVRequest request, URL dst) throws ProtocolException, ResourceException {
        DAVRequest req;
        DAVReply rep;
        req = createInternalRequest(request, "HEAD", dst, null);
        try {
            rep = (DAVReply) getServer().perform(req);
            closeInternalReply(rep);
        } catch (Exception ex) {
            Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
            error.setContent(ex.getMessage());
            throw new HTTPException(error);
        }
        boolean overwrite = false;
        if (rep.getStatus() != HTTP.NOT_FOUND) {
            overwrite = request.getOverwrite();
            if (!overwrite) {
                String msg = "The state of the destination is non-null";
                Reply error = request.makeReply(HTTP.PRECONDITION_FAILED);
                error.setContent(msg);
                return error;
            } else {
                req = createInternalRequest(request, "DELETE", dst, null);
                try {
                    rep = (DAVReply) getServer().perform(req);
                    closeInternalReply(rep);
                } catch (Exception ex) {
                    Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
                    error.setContent(ex.getMessage());
                    throw new HTTPException(error);
                }
            }
        }
        if (isCollection()) {
            return internalCopyCollection(dst, request, overwrite);
        } else {
            return internalCopyResource(dst, request, overwrite);
        }
    }

    /**
     * Handle the COPY request.
     * @param request the WEBDAV request
     * @return a Reply instance
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public Reply copy(DAVRequest request) throws ProtocolException, ResourceException {
        String dest = request.getDestination();
        URL src = getURL(request);
        URL dst = null;
        try {
            dst = new URL(dest);
        } catch (MalformedURLException ex) {
            Reply error = request.makeReply(HTTP.BAD_REQUEST);
            error.setContent("Invalid destination URL");
            throw new HTTPException(error);
        }
        if (dst.equals(src)) {
            Reply error = request.makeReply(HTTP.FORBIDDEN);
            String msg = "The source and destination URIs are the same";
            error.setContent(msg);
            return error;
        }
        if (URLUtils.equalsProtocolHostPort(src, dst)) {
            return internalCopy(request, dst);
        }
        org.w3c.www.protocol.webdav.DAVRequest crequest = null;
        org.w3c.www.protocol.webdav.DAVReply creply = null;
        crequest = createRequest(request, "HEAD", dst, null);
        try {
            creply = manager.runDAVRequest(crequest);
            skipBody(creply);
        } catch (org.w3c.www.protocol.http.HttpException ex) {
            Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
            error.setContent(ex.getMessage());
            throw new HTTPException(error);
        }
        boolean overwrite = false;
        if (creply.getStatus() != HTTP.NOT_FOUND) {
            overwrite = request.getOverwrite();
            if (!overwrite) {
                String msg = "The state of the destination is non-null";
                Reply error = request.makeReply(HTTP.PRECONDITION_FAILED);
                error.setContent(msg);
                return error;
            } else {
                crequest = createRequest(request, "DELETE", dst, null);
                try {
                    creply = manager.runDAVRequest(crequest);
                    skipBody(creply);
                } catch (org.w3c.www.protocol.http.HttpException ex) {
                    Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
                    error.setContent(ex.getMessage());
                    throw new HTTPException(error);
                }
            }
        }
        if (isCollection()) {
            return copyCollection(dst, request, overwrite);
        } else {
            return copyResource(dst, request, overwrite);
        }
    }

    /**
     * Kind of proxying the reply but only for headers.
     */
    protected Reply dupReply(DAVRequest request, org.w3c.www.protocol.webdav.DAVReply rep, boolean created) {
        Reply reply = null;
        if (created) {
            reply = request.makeReply(HTTP.NO_CONTENT);
            return reply;
        } else {
            reply = request.makeReply(rep.getStatus());
        }
        reply.setHeaderValue(Reply.H_SERVER, null);
        Enumeration e = rep.enumerateHeaderDescriptions();
        edu.hkust.clap.monitor.Monitor.loopBegin(101);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(101);
{
            HeaderDescription d = (HeaderDescription) e.nextElement();
            HeaderValue v = rep.getHeaderValue(d);
            if (v != null) reply.setHeaderValue(d, v);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(101);

        reply.setContentLength(0);
        reply.setHeaderValue(Reply.H_CONNECTION, null);
        reply.setHeaderValue(Reply.H_PROXY_CONNECTION, null);
        reply.setHeaderValue(Reply.H_PUBLIC, null);
        reply.setHeaderValue(Reply.H_TRANSFER_ENCODING, null);
        reply.setHeaderValue(Reply.H_UPGRADE, null);
        reply.setHeaderValue(Reply.H_CONTENT_TYPE, null);
        reply.removeHeader("keep-alive");
        return reply;
    }

    /**
     * Handle the COPY request for a collection
     * @param dst the destination URL
     * @param request the WEBDAV request
     * @return a Reply instance
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    protected Reply copyCollection(URL destination, DAVRequest request, boolean overwrite) throws ProtocolException, ResourceException {
        DAVBody body = getBody(request);
        Document document = DAVBody.createDocument(DAVNode.MULTISTATUS_NODE);
        Reply reply = null;
        try {
            return copyCollection(destination, request, overwrite, body, document);
        } catch (MultiStatusException ex) {
            return createDAVReply(request, WEBDAV.MULTI_STATUS, ex.getDocument());
        }
    }

    /**
     * Handle the COPY request for a collection
     * @param dst the destination URL
     * @param request the WEBDAV request
     * @return a Reply instance
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    private Reply internalCopyCollection(URL destination, DAVRequest request, boolean overwrite) throws ProtocolException, ResourceException {
        DAVBody body = getBody(request);
        Document document = DAVBody.createDocument(DAVNode.MULTISTATUS_NODE);
        Reply reply = null;
        try {
            return internalCopyCollection(destination, request, overwrite, body, document);
        } catch (MultiStatusException ex) {
            return createDAVReply(request, WEBDAV.MULTI_STATUS, ex.getDocument());
        }
    }

    protected Reply copyCollection(URL destination, DAVRequest request, boolean overwrite, DAVBody body, Document document) throws ProtocolException, ResourceException, MultiStatusException {
        org.w3c.www.protocol.webdav.DAVRequest crequest = null;
        org.w3c.www.protocol.webdav.DAVReply creply = null;
        crequest = createRequest(request, "MKCOL", destination, null);
        try {
            creply = manager.runDAVRequest(crequest);
            skipBody(creply);
        } catch (org.w3c.www.protocol.http.HttpException ex) {
            Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
            error.setContent(ex.getMessage());
            throw new HTTPException(error);
        }
        if (creply.getStatus() != HTTP.CREATED) {
            return dupReply(request, creply, false);
        }
        org.w3c.www.protocol.webdav.DAVReply mkcolreply = creply;
        copyProperties(request, destination, body);
        if (request.getDepth() == 0) {
            return dupReply(request, creply, overwrite);
        } else {
            DAVMultiStatus dms = DAVFactory.createMultiStatus(document.getDocumentElement());
            boolean multistatus = false;
            Enumeration e = dresource.enumerateResourceIdentifiers();
            ResourceReference rr = null;
            FramedResource fr = null;
            edu.hkust.clap.monitor.Monitor.loopBegin(102);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(102);
{
                String name = (String) e.nextElement();
                rr = dresource.lookup(name);
                if (rr != null) {
                    try {
                        fr = (FramedResource) rr.lock();
                        if (fr == resource) {
                            continue;
                        }
                        DAVFrame df = (DAVFrame) fr.getFrame(DAVFrame.class);
                        URL dest = null;
                        if (df != null) {
                            try {
                                dest = computeDestURL(destination, df);
                            } catch (MalformedURLException ex) {
                                Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
                                String msg = "Can't build destination URI : " + ex.getMessage();
                                error.setContent(msg);
                                throw new HTTPException(error);
                            }
                            if (df.isCollection()) {
                                try {
                                    df.copyCollection(dest, request, overwrite, body, document);
                                } catch (MultiStatusException ex) {
                                    multistatus = true;
                                    ex.printStackTrace();
                                    ex.addResponses(document, dms);
                                }
                            } else {
                                try {
                                    df.copyDAVResource(request, df.getURL(request), dest, body);
                                } catch (MultiStatusException ex) {
                                    multistatus = true;
                                    ex.printStackTrace();
                                    ex.addResponses(document, dms);
                                }
                            }
                        } else {
                        }
                    } catch (InvalidResourceException ex) {
                    } finally {
                        rr.unlock();
                    }
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(102);

            if (multistatus) {
                throw new MultiStatusException(document);
            } else {
                return dupReply(request, mkcolreply, overwrite);
            }
        }
    }

    protected Reply internalCopyCollection(URL destination, DAVRequest request, boolean overwrite, DAVBody body, Document document) throws ProtocolException, ResourceException, MultiStatusException {
        DAVRequest crequest = null;
        DAVReply creply = null;
        crequest = createInternalRequest(request, "MKCOL", destination, null);
        try {
            creply = (DAVReply) getServer().perform(crequest);
            closeInternalReply(creply);
        } catch (Exception ex) {
            Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
            error.setContent(ex.getMessage());
            throw new HTTPException(error);
        }
        if (creply.getStatus() != HTTP.CREATED) {
            return creply;
        }
        DAVReply mkcolreply = creply;
        internalCopyProperties(request, destination, body);
        if (request.getDepth() == 0) {
            if (overwrite) {
                creply.setStatus(HTTP.NO_CONTENT);
            }
            return creply;
        } else {
            DAVMultiStatus dms = DAVFactory.createMultiStatus(document.getDocumentElement());
            boolean multistatus = false;
            Enumeration e = dresource.enumerateResourceIdentifiers();
            ResourceReference rr = null;
            FramedResource fr = null;
            edu.hkust.clap.monitor.Monitor.loopBegin(103);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(103);
{
                String name = (String) e.nextElement();
                rr = dresource.lookup(name);
                if (rr != null) {
                    try {
                        fr = (FramedResource) rr.lock();
                        if (fr == resource) {
                            continue;
                        }
                        DAVFrame df = (DAVFrame) fr.getFrame(DAVFrame.class);
                        URL dest = null;
                        if (df != null) {
                            try {
                                dest = computeDestURL(destination, df);
                            } catch (MalformedURLException ex) {
                                Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
                                String msg = "Can't build destination URI : " + ex.getMessage();
                                error.setContent(msg);
                                throw new HTTPException(error);
                            }
                            if (df.isCollection()) {
                                try {
                                    df.internalCopyCollection(dest, request, overwrite, body, document);
                                } catch (MultiStatusException ex) {
                                    multistatus = true;
                                    ex.printStackTrace();
                                    ex.addResponses(document, dms);
                                }
                            } else {
                                try {
                                    df.internalCopyDAVResource(request, df.getURL(request), dest, body);
                                } catch (MultiStatusException ex) {
                                    multistatus = true;
                                    ex.printStackTrace();
                                    ex.addResponses(document, dms);
                                }
                            }
                        } else {
                        }
                    } catch (InvalidResourceException ex) {
                    } finally {
                        rr.unlock();
                    }
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(103);

            if (multistatus) {
                throw new MultiStatusException(document);
            } else {
                if (overwrite) {
                    mkcolreply.setStatus(HTTP.NO_CONTENT);
                }
                return mkcolreply;
            }
        }
    }

    protected URL computeDestURL(URL parent, DAVFrame df) throws MalformedURLException {
        String file = parent.getFile();
        StringBuffer buffer = new StringBuffer(file);
        if (!file.endsWith("/")) {
            buffer.append("/");
        }
        buffer.append(df.getResource().getIdentifier());
        if (df.isCollection()) {
            buffer.append("/");
        }
        return new URL(parent, buffer.toString());
    }

    /**
     * Handle the COPY request for a simple resource (not a collection)
     * @param dst the destination URL
     * @param request the WEBDAV request
     * @return a Reply instance
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    protected Reply copyResource(URL dst, DAVRequest request, boolean overwrite) throws ProtocolException, ResourceException {
        try {
            URL src = getURL(request);
            org.w3c.www.protocol.webdav.DAVReply reply = copyDAVResource(request, src, dst, getBody(request));
            if (overwrite) {
                reply.setStatus(HTTP.NO_CONTENT);
            }
            return dupReply(request, reply, overwrite);
        } catch (MultiStatusException ex) {
            return createDAVReply(request, WEBDAV.MULTI_STATUS, ex.getDocument());
        }
    }

    protected org.w3c.www.protocol.webdav.DAVReply copyDAVResource(DAVRequest req, URL source, URL destination, DAVBody body) throws MultiStatusException {
        DAVResponse dr = null;
        org.w3c.www.protocol.webdav.DAVRequest request = null;
        org.w3c.www.protocol.webdav.DAVReply reply = null;
        Hashtable headers = new Hashtable();
        headers.put("Content-Length", String.valueOf(getContentLength()));
        headers.put("Content-Type", getContentType().toString());
        InputStream in = null;
        try {
            in = new FileInputStream(fresource.getFile());
        } catch (IOException ex) {
            String msg = "Cannot read source file";
            throw new MultiStatusException(destination.toExternalForm(), getStatusLine(HTTP.INTERNAL_SERVER_ERROR), msg);
        }
        request = createRequest(req, "PUT", destination, headers, in);
        try {
            reply = manager.runDAVRequest(request);
            skipBody(reply);
        } catch (org.w3c.www.protocol.http.HttpException ex) {
            throw new MultiStatusException(destination.toExternalForm(), getStatusLine(HTTP.INTERNAL_SERVER_ERROR), ex.getMessage());
        }
        int putstatus = reply.getStatus();
        org.w3c.www.protocol.webdav.DAVReply putreply = reply;
        if (putstatus / 100 != 2) {
            throw new MultiStatusException(destination.toExternalForm(), getStatusLine(putstatus));
        }
        copyProperties(req, destination, body);
        return putreply;
    }

    /**
     * Handle the COPY request for a simple resource (not a collection)
     * @param dst the destination URL
     * @param request the WEBDAV request
     * @return a Reply instance
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    protected Reply internalCopyResource(URL dst, DAVRequest request, boolean overwrite) throws ProtocolException, ResourceException {
        try {
            URL src = getURL(request);
            DAVReply reply = internalCopyDAVResource(request, src, dst, getBody(request));
            if (overwrite) {
                reply.setStatus(HTTP.NO_CONTENT);
            }
            return reply;
        } catch (MultiStatusException ex) {
            return createDAVReply(request, WEBDAV.MULTI_STATUS, ex.getDocument());
        }
    }

    protected DAVReply internalCopyDAVResource(DAVRequest req, URL source, URL destination, DAVBody body) throws MultiStatusException {
        DAVResponse dr = null;
        DAVRequest request = null;
        DAVReply reply = null;
        Hashtable headers = new Hashtable();
        headers.put("Content-Length", String.valueOf(getContentLength()));
        headers.put("Content-Type", getContentType().toString());
        InputStream in = null;
        try {
            in = new FileInputStream(fresource.getFile());
        } catch (IOException ex) {
            String msg = "Cannot read source file";
            throw new MultiStatusException(destination.toExternalForm(), getStatusLine(HTTP.INTERNAL_SERVER_ERROR), msg);
        }
        request = createInternalRequest(req, "PUT", destination, headers, in);
        try {
            reply = (DAVReply) getServer().perform(request);
            closeInternalReply(reply);
        } catch (Exception ex) {
            throw new MultiStatusException(destination.toExternalForm(), getStatusLine(HTTP.INTERNAL_SERVER_ERROR), ex.getMessage());
        }
        int putstatus = reply.getStatus();
        DAVReply putreply = reply;
        if (putstatus / 100 != 2) {
            throw new MultiStatusException(destination.toExternalForm(), getStatusLine(putstatus));
        }
        internalCopyProperties(req, destination, body);
        return putreply;
    }

    protected void copyProperties(DAVRequest req, URL destination, DAVBody body) throws MultiStatusException {
        org.w3c.www.protocol.webdav.DAVRequest request = null;
        org.w3c.www.protocol.webdav.DAVReply reply = null;
        Document document = DAVBody.createDocument(DAVNode.PROPERTYUPDATE_NODE);
        DAVPropAction dpa = DAVFactory.createPropAction(DAVPropAction.SET, getPropertiesForCopy(document), document);
        document.getDocumentElement().appendChild(dpa.getNode());
        request = createRequest(req, "PROPPATCH", destination, null, document);
        try {
            reply = manager.runDAVRequest(request);
            skipBody(reply);
        } catch (org.w3c.www.protocol.http.HttpException ex) {
            throw new MultiStatusException(destination.toExternalForm(), getStatusLine(HTTP.INTERNAL_SERVER_ERROR), ex.getMessage());
        }
        if (body != null) {
            DAVPropertyBehavior dpb = body.getPropertyBehavior();
            if ((dpb != null) && (!dpb.omit())) {
                if (dpb.keepaliveAll()) {
                } else {
                    String properties[] = dpb.getHrefs();
                }
            } else {
            }
        }
    }

    protected void internalCopyProperties(DAVRequest req, URL destination, DAVBody body) throws MultiStatusException {
        DAVRequest request = null;
        DAVReply reply = null;
        Document document = DAVBody.createDocument(DAVNode.PROPERTYUPDATE_NODE);
        DAVPropAction dpa = DAVFactory.createPropAction(DAVPropAction.SET, getPropertiesForCopy(document), document);
        document.getDocumentElement().appendChild(dpa.getNode());
        request = createInternalRequest(req, "PROPPATCH", destination, null, document);
        try {
            reply = (DAVReply) getServer().perform(request);
            closeInternalReply(reply);
        } catch (Exception ex) {
            throw new MultiStatusException(destination.toExternalForm(), getStatusLine(HTTP.INTERNAL_SERVER_ERROR), ex.getMessage());
        }
        if (body != null) {
            DAVPropertyBehavior dpb = body.getPropertyBehavior();
            if ((dpb != null) && (!dpb.omit())) {
                if (dpb.keepaliveAll()) {
                } else {
                    String properties[] = dpb.getHrefs();
                }
            } else {
            }
        }
    }

    /**
     * Handle the MOVE request.
     * @param request the WEBDAV request
     * @return a Reply instance
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public Reply move(DAVRequest request) throws ProtocolException, ResourceException {
        if (getAllowDeleteFlag()) {
            Reply reply = copy(request);
            int status = reply.getStatus();
            if (status == HTTP.NO_CONTENT || status == HTTP.CREATED) {
                delete(request);
                return reply;
            } else {
                return reply;
            }
        }
        Reply error = request.makeReply(HTTP.NOT_ALLOWED);
        error.setContent("Method MOVE not allowed.");
        error.setHeaderValue(Reply.H_ALLOW, getAllow());
        throw new HTTPException(error);
    }

    protected synchronized String getNewLockToken() {
        return "opaquelocktoken:" + (new org.w3c.util.UUID()).toString();
    }

    /**
     * Handle the LOCK request.
     * @param request the WEBDAV request
     * @return a Reply instance
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public synchronized Reply lock(DAVRequest request) throws ProtocolException, ResourceException {
        DAVBody body = getBody(request);
        if (body == null) {
            if (debug) {
                System.out.println(">>> Refreshing lock...");
            }
            refreshLock(request.getTimeout());
            Document doc = DAVBody.createDocument(DAVNode.PROP_NODE);
            addLockDiscovery(request, doc.getDocumentElement());
            Reply ok = createDAVReply(request, HTTP.OK, doc);
            return ok;
        } else {
            String timeouts[] = request.getTimeout();
            DAVLockInfo lockinfo = body.getLockInfo();
            if (lockinfo != null) {
                Node owner = lockinfo.getOwner();
                lock(getNewLockToken(), request.getDepth(), request.getTimeout(), (String) request.getState(AuthFilter.STATE_AUTHUSER), owner);
                Document doc = DAVBody.createDocument(DAVNode.PROP_NODE);
                addLockDiscovery(request, doc.getDocumentElement());
                Reply ok = createDAVReply(request, HTTP.OK, doc);
                return ok;
            }
            Reply error = request.makeReply(HTTP.BAD_REQUEST);
            error.setContent("Invalid request: missing lockinfo");
            throw new HTTPException(error);
        }
    }

    /**
     * Handle the UNLOCK request.
     * @param request the WEBDAV request
     * @return a Reply instance
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public synchronized Reply unlock(DAVRequest request) throws ProtocolException, ResourceException {
        if (debug) {
            System.out.println(">>> UNLOCK");
        }
        if (!isLocked(request)) {
            return request.makeReply(HTTP.NO_CONTENT);
        }
        String token = request.getLockToken();
        if (token != null) {
            String mytoken = getCurrentLockToken(request);
            String rtoken = decodeURL(request, token);
            if (debug) {
                System.out.println("REQ  TOKEN : [" + rtoken + "]");
                System.out.println("LOCK TOKEN : [" + mytoken + "]");
            }
            if (rtoken.equals(mytoken) && checkLockOwner(request)) {
                if (definesAttribute(ATTR_LOCK_TOKEN)) {
                    unlock();
                } else {
                    ResourceReference rr = (ResourceReference) request.getState(LOCKED_REREFENCE);
                    if (rr == null) {
                        Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
                        error.setContent("Unable to unlock, no request state");
                        throw new HTTPException(error);
                    } else {
                        try {
                            DAVFrame fr = (DAVFrame) rr.lock();
                            fr.unlock();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
                            error.setContent(ex.getMessage());
                            throw new HTTPException(error);
                        } finally {
                            rr.unlock();
                        }
                    }
                }
                return request.makeReply(HTTP.NO_CONTENT);
            } else {
                Reply reply = request.makeReply(WEBDAV.LOCKED);
                reply.setContent("The resource is locked");
                return reply;
            }
        }
        Reply error = request.makeReply(HTTP.FORBIDDEN);
        error.setContent("You are not allowed to unlock this resource");
        throw new HTTPException(error);
    }

    private void updateStates(DAVRequest request) {
        if (getCurrentLockDepth() == WEBDAV.DEPTH_INFINITY) {
            request.setState(LOCKED_REREFENCE, getResourceReference());
            request.setState(LOCK_OWNER, getCurrentLockOwner(null));
            request.setState(LOCK_TOKEN, getCurrentLockToken(null));
            request.setState(LOCK_USERNAME, getCurrentLockUsername(null));
            request.setState(LOCK_EXPIRE, new Long(getTokenExpirationDate(null)));
            request.setState(LOCK_TIMEOUT, getValue(ATTR_LOCK_TIMEOUT, DEFAULT_LOCK_TIMEOUT));
        }
    }

    /**
     * Lookup the target resource (dispath to more specific lookup methods).
     * @param ls The current lookup state
     * @param lr The result
     * @return true if lookup is done.
     * @exception ProtocolException If an error relative to the protocol occurs
     * @see #lookupDirectory
     * @see #lookupFile
     * @see #lookupOther
     */
    protected boolean lookupResource(LookupState ls, LookupResult lr) throws ProtocolException {
        DAVRequest request = (DAVRequest) ls.getRequest();
        if ((request != null) && (isLocked(null))) {
            updateLockDate(request);
        }
        return super.lookupResource(ls, lr);
    }

    /**
     * Lookup the target resource when associated with a DirectoryResource.
     * @param ls The current lookup state
     * @param lr The result
     * @return true if lookup is done.
     * @exception ProtocolException If an error relative to the protocol
     * occurs
     */
    protected boolean lookupDirectory(LookupState ls, LookupResult lr) throws ProtocolException {
        DAVRequest request = (DAVRequest) ls.getRequest();
        if (request == null) {
            return super.lookupDirectory(ls, lr);
        }
        if (isLocked(null)) {
            updateStates(request);
        }
        if (ls.hasMoreComponents()) {
            if (request.getMethod().equals("PUT")) {
                String name = ls.peekNextComponent();
                ResourceReference rr = dresource.lookup(name);
                if ((rr == null) && dresource.getExtensibleFlag() && getPutableFlag()) {
                    if (ls.countRemainingComponents() == 1) {
                        rr = dresource.createResource(name, request);
                        if (rr == null) {
                            Reply error = request.makeReply(HTTP.UNSUPPORTED_MEDIA_TYPE);
                            error.setContent("Failed to create resource " + name + " : " + "Unable to create the appropriate file:" + request.getURLPath() + " this media type is not supported");
                            throw new HTTPException(error);
                        }
                    } else {
                        Reply error = request.makeReply(HTTP.CONFLICT);
                        error.setContent(name + " does not exists!");
                        throw new HTTPException(error);
                    }
                } else if (rr == null) {
                    Reply error = request.makeReply(HTTP.FORBIDDEN);
                    error.setContent("You are not allowed to create resource " + name + " : " + dresource.getIdentifier() + " is not extensible.");
                    throw new HTTPException(error);
                }
            } else if (request.getMethod().equals("MKCOL")) {
                String name = ls.peekNextComponent();
                ResourceReference rr = dresource.lookup(name);
                if (rr == null) {
                    if (ls.countRemainingComponents() == 1) {
                        request.setState(REMAINING_PATH, name);
                        return true;
                    } else {
                        Reply error = request.makeReply(HTTP.CONFLICT);
                        error.setContent("Can't create " + ls.getRemainingPath(true));
                        throw new HTTPException(error);
                    }
                }
            }
        }
        if (super.lookupOther(ls, lr)) {
            if (!ls.isDirectory() && !ls.isInternal()) {
                if (request == null) {
                    lr.setTarget(null);
                    return true;
                } else if (!acceptRedirect(request)) {
                    return true;
                }
                URL url = null;
                try {
                    if ((request != null) && request.hasState(Request.ORIG_URL_STATE)) {
                        URL oldurl;
                        oldurl = (URL) request.getState(Request.ORIG_URL_STATE);
                        url = new URL(oldurl, oldurl.getFile() + "/");
                    } else {
                        url = (ls.hasRequest() ? getURL(request) : new URL(getServer().getURL(), resource.getURLPath()));
                    }
                } catch (MalformedURLException ex) {
                    getServer().errlog(this, "unable to build full URL.");
                    throw new HTTPException("Internal server error");
                }
                String msg = "Invalid requested URL: the directory resource " + " you are trying to reach is available only through " + " its full URL: <a href=\"" + url + "\">" + url + "</a>.";
                if (getRelocateFlag()) {
                    Reply reloc = request.makeReply(HTTP.FOUND);
                    reloc.setContent(msg);
                    reloc.setLocation(url);
                    lr.setTarget(null);
                    lr.setReply(reloc);
                    return true;
                } else {
                    Reply error = request.makeReply(HTTP.NOT_FOUND);
                    error.setContent(msg);
                    lr.setTarget(null);
                    lr.setReply(error);
                    return true;
                }
            } else if (!ls.isInternal() && acceptRedirect(request)) {
                request.setState(STATE_CONTENT_LOCATION, "true");
                String indexes[] = getIndexes();
                if (indexes != null) {
                    edu.hkust.clap.monitor.Monitor.loopBegin(104);
for (int i = 0; i < indexes.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(104);
{
                        String index = indexes[i];
                        if (index != null && index.length() > 0) {
                            DirectoryResource dir = (DirectoryResource) resource;
                            ResourceReference rr = dir.lookup(index);
                            if (rr != null) {
                                try {
                                    FramedResource rindex = (FramedResource) rr.lock();
                                    return rindex.lookup(ls, lr);
                                } catch (InvalidResourceException ex) {
                                } finally {
                                    rr.unlock();
                                }
                            }
                        }
                    }} 
edu.hkust.clap.monitor.Monitor.loopEnd(104);

                }
            }
            return true;
        }
        return false;
    }

    /**
     * companion to initialize, called after the register
     */
    public void registerResource(FramedResource resource) {
        super.registerResource(resource);
        ObservableProperties props = getResource().getServer().getProperties();
        manager = org.w3c.www.protocol.webdav.DAVManager.getDAVManager(props);
    }

    public void initialize(Object values[]) {
        super.initialize(values);
        if (getCreationDate() == -1) {
            setValue(ATTR_CREATION_DATE, new Long(System.currentTimeMillis()));
        }
    }
}
