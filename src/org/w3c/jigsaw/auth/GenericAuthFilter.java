package org.w3c.jigsaw.auth;

import java.io.PrintStream;
import java.util.Enumeration;
import java.net.InetAddress;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFilter;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.StringArrayAttribute;
import org.w3c.jigsaw.http.Client;
import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.httpd;
import org.w3c.jigsaw.html.HtmlGenerator;
import org.w3c.tools.codec.Base64Decoder;
import org.w3c.tools.codec.Base64FormatException;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpChallenge;
import org.w3c.www.http.HttpCredential;
import org.w3c.www.http.HttpFactory;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;
import org.w3c.tools.resources.ProtocolException;

/**
 * A generic authentication filter.
 * This filter will use both IP and basic authentication to try to authenticate
 * incomming request. It should not be use for big user's database (typically
 * the ones that have more than 1000 entries).
 */
class BasicAuthContextException extends Exception {

    BasicAuthContextException(String msg) {
        super(msg);
    }
}

class BasicAuthContext {

    String user = null;

    String password = null;

    String cookie = null;

    public String toString() {
        return user + ":" + password;
    }

    BasicAuthContext(Request request) throws BasicAuthContextException, ProtocolException {
        HttpCredential credential = null;
        credential = (request.isProxy() ? request.getProxyAuthorization() : request.getAuthorization());
        if (!credential.getScheme().equalsIgnoreCase("Basic")) {
            String msg = ("Invalid authentication scheme \"" + credential.getScheme() + " expecting \"Basic\"");
            throw new BasicAuthContextException(msg);
        }
        String decoded = null;
        this.cookie = credential.getAuthParameter("cookie");
        try {
            Base64Decoder b = new Base64Decoder(cookie);
            decoded = b.processString();
        } catch (Base64FormatException e) {
            String msg = "Invalid BASE64 encoding of credentials.";
            throw new BasicAuthContextException(msg);
        }
        int icolon = decoded.indexOf(':');
        if ((icolon > 0) && (icolon + 1 < decoded.length())) {
            this.user = decoded.substring(0, icolon);
            this.password = decoded.substring(icolon + 1);
        } else {
            String msg = "Invalid credentials syntax in " + decoded;
            throw new BasicAuthContextException(msg);
        }
    }
}

/**
 * GenericAuthFilter provides for both IP and basic authentication.
 * This is really a first implementation. It looses on several points:
 * <ul>
 * <li>AuthUser instances, being a subclass of resource dump their classes
 * along with their attributes, although here we know that they will all
 * be instances of AuthUser.
 * <li>The way the ipmatcher is maintained doesn't make much sense.
 * <li>The way groups are handled is no good.
 * <li>The SimpleResourceStore is not an adequat store for the user database,
 * it should rather use the jdbmResourceStore (not written yet).
 * </ul>
 * However, this provides for the basic functionnalities.
 */
public class GenericAuthFilter extends AuthFilter {

    /**
     * Attribute index - The list of allowed users.
     */
    protected static int ATTR_ALLOWED_USERS = -1;

    /**
     * Attribute index - The list of allowed groups.
     */
    protected static int ATTR_ALLOWED_GROUPS = -1;

    static {
        Attribute a = null;
        Class c = null;
        try {
            c = Class.forName("org.w3c.jigsaw.auth.GenericAuthFilter");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        a = new StringArrayAttribute("users", null, Attribute.EDITABLE);
        ATTR_ALLOWED_USERS = AttributeRegistry.registerAttribute(c, a);
        a = new StringArrayAttribute("groups", null, Attribute.EDITABLE);
        ATTR_ALLOWED_GROUPS = AttributeRegistry.registerAttribute(c, a);
    }

    /**
     * The IPMatcher to match IP templates to user records.
     */
    protected IPMatcher ipmatcher = null;

    /**
     * The catalog of realms that make our scope.
     */
    protected RealmsCatalog catalog = null;

    /**
     * Our associated realm.
     */
    protected ResourceReference rr_realm = null;

    /**
     * The nam of the realm we cache in <code>realm</code>.
     */
    protected String loaded_realm = null;

    /**
     * The challenge to issue to any client for Basic Authentication.
     */
    protected HttpChallenge challenge = null;

    /**
     * Get a pointer to our realm, and initialize our ipmatcher.
     */
    protected synchronized void acquireRealm() {
        if (catalog == null) {
            httpd server = (httpd) ((FramedResource) getTargetResource()).getServer();
            catalog = server.getRealmsCatalog();
        }
        String name = getRealm();
        if (name == null) return;
        if ((rr_realm != null) && name.equals(loaded_realm)) return;
        rr_realm = catalog.loadRealm(name);
        if (rr_realm != null) {
            try {
                AuthRealm realm = (AuthRealm) rr_realm.lock();
                Enumeration e = realm.enumerateUserNames();
                if (e.hasMoreElements()) {
                    ipmatcher = new IPMatcher();
                }
                edu.hkust.clap.monitor.Monitor.loopBegin(1081);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(1081);
{
                    String uname = (String) e.nextElement();
                    ResourceReference rr_user = realm.loadUser(uname);
                    try {
                        AuthUser user = (AuthUser) rr_user.lock();
                        short ips[][] = user.getIPTemplates();
                        if (ips != null) {
                            edu.hkust.clap.monitor.Monitor.loopBegin(1082);
for (int i = 0; i < ips.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1082);
ipmatcher.add(ips[i], rr_user);} 
edu.hkust.clap.monitor.Monitor.loopEnd(1082);

                        }
                    } catch (InvalidResourceException ex) {
                        System.out.println("Invalid user reference : " + uname);
                    } finally {
                        rr_user.unlock();
                    }
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1081);

            } catch (InvalidResourceException ex) {
            } finally {
                rr_realm.unlock();
            }
        }
    }

    /**
     * Check that our realm does exist.
     * Otherwise we are probably being initialized, and we don't authenticate
     * yet.
     * @return A boolean <strong>true</strong> if realm can be initialized.
     */
    protected synchronized boolean checkRealm() {
        acquireRealm();
        return (ipmatcher != null);
    }

    /**
     * Get the list of allowed users.
     */
    public String[] getAllowedUsers() {
        return (String[]) getValue(ATTR_ALLOWED_USERS, null);
    }

    /**
     * Get the list of allowed groups.
     */
    public String[] getAllowedGroups() {
        return (String[]) getValue(ATTR_ALLOWED_GROUPS, null);
    }

    /**
     * Lookup a user by its IP address.
     * @param ipaddr The IP address to look for.
     * @return An AuthUser instance or <strong>null</strong>.
     */
    public synchronized ResourceReference lookupUser(InetAddress ipaddr) {
        if (ipmatcher == null) acquireRealm();
        return (ResourceReference) ipmatcher.lookup(ipaddr.getAddress());
    }

    /**
     * Lookup a user by its name.
     * @param name The user's name.
     * @return An AuthUser instance, or <strong>null</strong>.
     */
    public synchronized ResourceReference lookupUser(String name) {
        if (rr_realm == null) acquireRealm();
        try {
            AuthRealm realm = (AuthRealm) rr_realm.lock();
            return realm.loadUser(name);
        } catch (InvalidResourceException ex) {
            return null;
        } finally {
            rr_realm.unlock();
        }
    }

    /**
     * Check the given Basic context against our database.
     * @param ctxt The basic auth context to check.
     * @return A AuthUser instance if check succeeded, <strong>null</strong>
     *    otherwise.
     */
    protected ResourceReference checkBasicAuth(BasicAuthContext ctxt) {
        ResourceReference rr_user = (ResourceReference) lookupUser(ctxt.user);
        if (rr_user != null) {
            try {
                AuthUser user = (AuthUser) rr_user.lock();
                if (user == null) return null;
                if (!user.definesAttribute("password")) {
                    return rr_user;
                } else {
                    return user.getPassword().equals(ctxt.password) ? rr_user : null;
                }
            } catch (InvalidResourceException ex) {
                return null;
            } finally {
                rr_user.unlock();
            }
        }
        return null;
    }

    /**
     * Is this user allowed in the realm ?
     * First check in the list of allowed users (if any), than in the list
     * of allowed groups (if any). If no allowed users or allowed groups
     * are defined, than simply check for the existence of this user.
     * @return A boolean <strong>true</strong> if access allowed.
     */
    protected boolean checkUser(AuthUser user) {
        String allowed_users[] = getAllowedUsers();
        if (allowed_users != null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(444);
for (int i = 0; i < allowed_users.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(444);
{
                if (allowed_users[i].equals(user.getName())) return true;
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(444);

        }
        String allowed_groups[] = getAllowedGroups();
        if (allowed_groups != null) {
            String ugroups[] = user.getGroups();
            if (ugroups != null) {
                edu.hkust.clap.monitor.Monitor.loopBegin(445);
for (int i = 0; i < ugroups.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(445);
{
                    edu.hkust.clap.monitor.Monitor.loopBegin(446);
for (int j = 0; j < allowed_groups.length; j++) { 
edu.hkust.clap.monitor.Monitor.loopInc(446);
{
                        if (allowed_groups[j].equals(ugroups[i])) return true;
                    }} 
edu.hkust.clap.monitor.Monitor.loopEnd(446);

                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(445);

            }
        }
        if ((allowed_users == null) && (allowed_groups == null)) return true;
        return false;
    }

    /**
     * Catch set value on the realm, to maintain cached values.
     */
    public void setValue(int idx, Object value) {
        super.setValue(idx, value);
        if (idx == ATTR_REALM) {
            challenge = HttpFactory.makeChallenge("Basic");
            challenge.setAuthParameter("realm", getRealm());
        }
    }

    /**
     * Authenticate the given request.
     * We first check for valid authentication information. If no 
     * authentication is provided, than we try to map the IP address to some
     * of the ones we know about. If the IP address is not found, we challenge
     * the client for a password.
     * <p>If the IP address is found, than either our user entry requires an
     * extra password step (in wich case we challenge it), or simple IP
     * based authentication is enough, so we allow the request.
     * @param request The request to be authentified.
     * @exception org.w3c.tools.resources.ProtocolException if authentication
     * failed
     */
    public void authenticate(Request request) throws ProtocolException {
        if (!checkRealm()) return;
        Client client = request.getClient();
        if (client == null) return;
        boolean ipchecked = false;
        ResourceReference rr_user = lookupUser(client.getInetAddress());
        if (rr_user != null) {
            try {
                AuthUser user = (AuthUser) rr_user.lock();
                if (user != null) {
                    ipchecked = true;
                    if (!user.definesAttribute("password") && checkUser(user)) {
                        request.setState(STATE_AUTHUSER, user.getName());
                        request.setState(STATE_AUTHTYPE, "ip");
                        return;
                    }
                }
            } catch (InvalidResourceException ex) {
            } finally {
                rr_user.unlock();
            }
        }
        if ((request.hasAuthorization() && !request.isProxy()) || (request.isProxy() && request.hasProxyAuthorization())) {
            BasicAuthContext ctxt = null;
            try {
                ctxt = new BasicAuthContext(request);
            } catch (BasicAuthContextException ex) {
                ctxt = null;
            }
            if (ctxt != null) {
                rr_user = checkBasicAuth(ctxt);
                if (rr_user != null) {
                    try {
                        AuthUser user = (AuthUser) rr_user.lock();
                        if ((user != null) && checkUser(user)) {
                            boolean iprequired = user.definesAttribute("ipaddress");
                            if ((!iprequired) || ipchecked) {
                                request.setState(STATE_AUTHUSER, ctxt.user);
                                request.setState(STATE_AUTHTYPE, "Basic");
                                return;
                            }
                        }
                    } catch (InvalidResourceException ex) {
                    } finally {
                        rr_user.unlock();
                    }
                }
            }
        }
        Reply e = null;
        if (request.isProxy()) {
            e = request.makeReply(HTTP.PROXY_AUTH_REQUIRED);
            e.setProxyAuthenticate(challenge);
        } else {
            e = request.makeReply(HTTP.UNAUTHORIZED);
            e.setWWWAuthenticate(challenge);
        }
        HtmlGenerator g = new HtmlGenerator("Unauthorized");
        g.append("<h1>Unauthorized access</h1>" + "<p>You are denied access to this resource.");
        e.setStream(g);
        throw new HTTPException(e);
    }

    /**
     * Initialize the filter.
     */
    public void initialize(Object values[]) {
        super.initialize(values);
        if (getRealm() != null) {
            challenge = HttpFactory.makeChallenge("Basic");
            challenge.setAuthParameter("realm", getRealm());
        }
    }
}
