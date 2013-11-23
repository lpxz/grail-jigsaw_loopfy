package org.w3c.jigsaw.auth;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.LookupResult;
import org.w3c.tools.resources.LookupState;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFilter;
import org.w3c.tools.resources.StringArrayAttribute;
import org.w3c.tools.resources.StringAttribute;
import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;
import org.w3c.tools.resources.ProtocolException;

/**
 * General authentication filters.
 */
public class AuthFilter extends ResourceFilter {

    /**
     * The user as authentified from the request.
     * FIXME
     */
    public static final String STATE_AUTHUSER = "org.w3c.jigsaw.auth.AuthFilter.user";

    /**
     * The authentication method used to authenticate above user.
     * FIXME
     */
    public static final String STATE_AUTHTYPE = "org.w3c.jigsaw.auth.AuthFilter.type";

    /**
     * The authentication context used to authenticate the above user
     * FIXME
     */
    public static final String STATE_AUTHCONTEXT = "org.w3c.jigsaw.auth.AuthFilter.context";

    /**
     * Attribute index - The methods protected by the filter.
     */
    protected static int ATTR_METHODS = -1;

    /**
     * Attribute index - The realm name for this filter.
     */
    protected static int ATTR_REALM = -1;

    /**
     * Attribute index - Is caching allowed by a shared cache ?
     */
    protected static int ATTR_SHARED_CACHABILITY = -1;

    /**
     * Attribute index - Is caching allowed in private cache ?
     */
    protected static int ATTR_PRIVATE_CACHABILITY = -1;

    /**
     * Attribute index - Is public caching of protected documents allowed ?
     */
    protected static int ATTR_PUBLIC_CACHABILITY = -1;

    static {
        Attribute a = null;
        Class c = null;
        try {
            c = Class.forName("org.w3c.jigsaw.auth.AuthFilter");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        a = new StringArrayAttribute("methods", null, Attribute.EDITABLE);
        ATTR_METHODS = AttributeRegistry.registerAttribute(c, a);
        a = new StringAttribute("realm", null, Attribute.EDITABLE | Attribute.MANDATORY);
        ATTR_REALM = AttributeRegistry.registerAttribute(c, a);
        a = new BooleanAttribute("shared-cachability", Boolean.FALSE, Attribute.EDITABLE);
        ATTR_SHARED_CACHABILITY = AttributeRegistry.registerAttribute(c, a);
        a = new BooleanAttribute("private-cachability", Boolean.FALSE, Attribute.EDITABLE);
        ATTR_PRIVATE_CACHABILITY = AttributeRegistry.registerAttribute(c, a);
        a = new BooleanAttribute("public-cachability", Boolean.FALSE, Attribute.EDITABLE);
        ATTR_PUBLIC_CACHABILITY = AttributeRegistry.registerAttribute(c, a);
    }

    /**
     * Get the list of methods that this filter protect
     * @return An array of String giving the name of the protected methods,
     *    or <strong>null</strong>, in wich case <em>all</em> methods are
     *    to be protected.
     */
    public String[] getMethods() {
        return (String[]) getValue(ATTR_METHODS, null);
    }

    /**
     * Get the realm of this filter.
     */
    public String getRealm() {
        return (String) getValue(ATTR_REALM, null);
    }

    /**
     * Is this document publicly cachable ?
     * @return A boolean.
     */
    public boolean getPublicCachability() {
        return getBoolean(ATTR_PUBLIC_CACHABILITY, false);
    }

    /**
     * Is this document cachable in private caches ?
     * @return A boolean.
     */
    public boolean getPrivateCachability() {
        return getBoolean(ATTR_PRIVATE_CACHABILITY, false);
    }

    /**
     * Is this document cachable in shared caches ?
     * @return A boolean.
     */
    public boolean getSharedCachability() {
        return getBoolean(ATTR_SHARED_CACHABILITY, false);
    }

    /**
     * Authenticate the request from the given client.
     * An authentication filter should only override this method.
     * @param request The request to be handled.
     * @exception ProtocolException If authentication failed.
     */
    public void authenticate(Request request) throws ProtocolException {
        Reply error = request.makeReply(HTTP.UNAUTHORIZED);
        error.setContent("<p>Invalid server configuration</p>" + "<p>The server defines an authentication filter " + " with no <strong>authenticate</strong> method.");
        throw new HTTPException(error);
    }

    /**
     * Authenticate the given request for the given client. 
     * This method is invoked prior to any request handling on its target
     * entity. If the used authentication method allows so, AuthFilters 
     * should set the <strong>authuser</strong> attribute of the request.
     * @param request The request.
     * @exception ProtocolException If authentication failed.
     */
    public boolean lookup(LookupState ls, LookupResult lr) throws ProtocolException {
        Request request = (Request) (ls.hasRequest() ? ls.getRequest() : null);
        if ((request == null) || ls.isInternal()) return false;
        String method = request.getMethod();
        String methods[] = getMethods();
        if (methods == null) {
            authenticate(request);
            return false;
        } else {
            edu.hkust.clap.monitor.Monitor.loopBegin(307);
for (int i = 0; i < methods.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(307);
{
                if (methods[i].equalsIgnoreCase(method)) {
                    authenticate(request);
                    return false;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(307);

        }
        return false;
    }

    /**
     * Add the appropriate cache control directives on the way back.
     * @param request The request that has been processed.
     * @param reply The original reply.
     * @return Always <strong>null</strong>.
     */
    public ReplyInterface outgoingFilter(RequestInterface request, ReplyInterface reply) {
        Reply rep = (Reply) reply;
        if (getPrivateCachability()) {
            rep.setMustRevalidate(true);
        } else if (getSharedCachability()) {
            rep.setProxyRevalidate(true);
        } else if (getPublicCachability()) {
            rep.setPublic(true);
        }
        return null;
    }
}
