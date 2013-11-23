package org.w3c.tools.resources;

import java.util.Vector;

/**
 * This object keeps the state info around while looking up an entity.
 */
public class LookupState {

    private int index;

    private String components[];

    private String componentstype[];

    private RequestInterface request;

    private boolean is_directory = false;

    private boolean is_internal = false;

    private String uri = null;

    private String query = null;

    private String fragment = null;

    private String type = null;

    /**
     * Unescape a escaped string
     * @param s The string to be unescaped
     * @return the unescaped string.
     */
    public static String unescape(String s) {
        int l = s.length();
        boolean work = false;
        edu.hkust.clap.monitor.Monitor.loopBegin(844);
for (int i = 0; i < l; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(844);
{
            char c = s.charAt(i);
            if ((c == '%') || (c == '+')) {
                work = true;
                break;
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(844);

        if (work) {
            char cbuf[] = new char[l];
            int pos = 0;
            int ch = -1;
            edu.hkust.clap.monitor.Monitor.loopBegin(845);
for (int i = 0; i < l; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(845);
{
                switch(ch = s.charAt(i)) {
                    case '%':
                        ch = s.charAt(++i);
                        int hb = (Character.isDigit((char) ch) ? ch - '0' : 10 + Character.toLowerCase((char) ch) - 'a') & 0xF;
                        ch = s.charAt(++i);
                        int lb = (Character.isDigit((char) ch) ? ch - '0' : 10 + Character.toLowerCase((char) ch) - 'a') & 0xF;
                        if (((hb << 4) | lb) > 0) {
                            cbuf[pos++] = (char) ((hb << 4) | lb);
                        }
                        break;
                    case '+':
                        cbuf[pos++] = ' ';
                        break;
                    default:
                        cbuf[pos++] = (char) ch;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(845);

            return new String(cbuf, 0, pos);
        }
        return s;
    }

    /**
     * Parse the given URI into an array of hierarchical components.
     * The optional query string and an optional fragment are recorded into
     * the request as new fields.
     * <p>The query string and the fragment are recorded into the request
     * as the <strong>query</strong> and <strong>frag</strong> attributes.
     * @exception ProtocolException if unable to parse
     */
    protected void parseURI() throws ProtocolException {
        int urilen = uri.length();
        int start = 0;
        int slash = -1;
        int t = -1;
        Vector comps = new Vector(8);
        int q = uri.indexOf('?', start);
        int f = uri.indexOf('#', start);
        int stop = -1;
        if ((q >= 0) && (f >= 0)) {
            stop = Math.min(q, f);
        } else if (q >= 0) {
            stop = q;
        } else if (f >= 0) {
            stop = f;
        } else {
            stop = urilen;
        }
        if (stop < 0) stop = urilen;
        this.uri = uri;
        edu.hkust.clap.monitor.Monitor.loopBegin(846);
        loop: 
while (true) { 
edu.hkust.clap.monitor.Monitor.loopInc(846);
{
            slash = uri.indexOf('/', start);
            if ((slash >= stop) || (slash < 0)) {
                break loop;
            } else if (slash == start) {
                start = slash + 1;
                continue loop;
            } else if (slash > 0) {
                String part = unescape(uri.substring(start, slash));
                t = part.indexOf(';');
                if (t == -1) {
                    if (part.indexOf('/') != -1) {
                        String spa = uri.substring(start, slash);
                        comps.addElement(spa);
                    } else {
                        comps.addElement(part);
                    }
                } else {
                    int sl = part.indexOf('/');
                    if (sl >= 0) {
                        if (t < sl) {
                            comps.addElement(part);
                        } else {
                            comps.addElement((uri.substring(start, slash)));
                        }
                    }
                }
                start = slash + 1;
                continue loop;
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(846);

        if ((q >= 0) || (f >= 0)) {
            if ((q >= 0) && (f > q)) {
                if (q + 1 < f) this.query = uri.substring(q + 1, f);
                if (f + 1 < urilen) this.fragment = uri.substring(f + 1, urilen);
            } else if ((f >= 0) && (q > f)) {
                if (f + 1 < q) this.fragment = uri.substring(f + 1, q);
                if (q + 1 < urilen) this.query = uri.substring(q + 1, urilen);
            } else if (f >= 0) {
                if (f + 1 < urilen) this.fragment = uri.substring(f + 1, urilen);
            } else if (q >= 0) {
                if (q + 1 < urilen) this.query = uri.substring(q + 1, urilen);
            }
        }
        if (request != null) {
            if (query != null) request.setState("query", query);
            if (fragment != null) request.setState("frag", fragment);
        }
        if (start < stop) {
            comps.addElement(unescape(uri.substring(start, stop)));
        }
        if (--stop >= 0) is_directory = (uri.charAt(stop) == '/');
        components = new String[comps.size()];
        componentstype = new String[comps.size()];
        comps.copyInto(components);
        edu.hkust.clap.monitor.Monitor.loopBegin(847);
for (int i = 0; i < components.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(847);
{
            t = components[i].indexOf(';');
            if (t >= 0) {
                componentstype[i] = components[i].substring(t + 1);
                components[i] = components[i].substring(0, t);
            }
            if (components[i].indexOf('/') >= 0) {
                throw new ProtocolException("encoded % in URI are forbidden" + " on this server");
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(847);

        index = 0;
    }

    /**
     * Get the fragment part of the URL, if any.
     * The fragment is anything beyond the # character in a URL.
     * @return A String instance, or <strong>null</strong>.
     */
    public String getFragment() {
        return fragment;
    }

    /**
     * Get the query part of the URL, if any.
     * The query is anything beyond a ? character in a URL.
     * @return A String instance, or <strong>null</strong>.
     */
    public String getQuery() {
        return query;
    }

    /**
     * Get the type part of the URL, if any.
     * The type is anything beyond a ; character in a URL.
     * @return A String instance, or <strong>null</strong>.
     */
    public String getType() {
        return componentstype[index];
    }

    /**
     * Is the requested URI a directory URI ?
     * @return A boolean <strong>true</strong> if the requested URI ends with
     * a slash, <strong>false</strong> otherwise.
     */
    public boolean isDirectory() {
        return is_directory;
    }

    /**
     * Get this lookpu state full URI.
     */
    public String getURI() {
        return uri;
    }

    /**
     * Get next part of the URL to be look for.
     * @return A String giving the next component.
     */
    public final String getNextComponent() {
        if (request != null) {
            request.setState("type", componentstype[index]);
        }
        return components[index++];
    }

    /**
     * Get the next component, without consuming it.
     * @return A String giving the next component, or <strong>null</strong>
     *    if none is available.
     */
    public final String peekNextComponent() {
        if (index < components.length) return components[index];
        return null;
    }

    /**
     * Get the remaining path.
     * @param consume If <strong>true</strong>, consume the components, 
     * otherwise, just peek them.
     * @return A String giving the remaining URL.
     */
    public final String getRemainingPath(boolean consume) {
        StringBuffer sb = new StringBuffer();
        edu.hkust.clap.monitor.Monitor.loopBegin(848);
for (int i = index; i < components.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(848);
{
            sb.append("/" + components[i]);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(848);

        if (consume) index = components.length;
        return sb.toString();
    }

    /**
     * Get the remaiing path, without consuming it.
     * @return The remaining path.
     */
    public final String getRemainingPath() {
        return getRemainingPath(false);
    }

    /**
     * Does this look up state has more components to be looked for.
     * @return <strong>true</strong> if more components are to be looked for.
     */
    public boolean hasMoreComponents() {
        return index < components.length;
    }

    /**
     * How much components have not yet been looked up in this state.
     */
    public int countRemainingComponents() {
        return components.length - index;
    }

    /**
     * Get this lookup state request.
     * @return An instance of RequestInterface, or <strong>null</strong> 
     * if this is an internal request.
     */
    public final RequestInterface getRequest() {
        return request;
    }

    /**
     * Is this lookup state object associated with a request ?
     * @return A boolean <strong>true</strong> if a request is associated.
     */
    public boolean hasRequest() {
        return (request != null);
    }

    /**
     * Mark this lookup state as being done internally.
     * This allows lookup methods to be more kind (for example, not throwing
     * redirections error, etc).
     */
    public void markInternal() {
        is_internal = true;
    }

    /**
     * Is this lookup state internal to the server.
     * Internal lookup state may not have an associated request.
     * @return A boolean <strong>true</strong> if this is an internal request.
     */
    public boolean isInternal() {
        return is_internal;
    }

    /**
     * Create a lookup state to handle the given request on behalf of client.
     * @param client The client that issued the request.
     * @param request The request whose URI is to bee looked up.
     * @exception ProtocolException if an error relative to the protocol occurs
     */
    public LookupState(RequestInterface request) throws ProtocolException {
        this.request = request;
        this.uri = request.getURLPath();
        this.is_internal = request.isInternal();
        if (uri == null) {
            ReplyInterface reply = request.makeBadRequestReply();
            reply.setContent("Invalid URI (unparsable)");
            throw new ProtocolException(reply);
        }
        parseURI();
    }

    /**
     * Construct a lookup state to be resolved internnaly by the server.
     * This method allows for internal lookup of object, even if there is no
     * real client making the request.
     * @param uri The URI to be looked up.
     * @exception ProtocolException if an error relative to the protocol occurs
     */
    public LookupState(String uri) throws ProtocolException {
        this.request = null;
        this.is_internal = true;
        this.uri = uri;
        parseURI();
    }
}
