package org.w3c.jigsaw.auth;

import java.util.Date;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.StringArrayAttribute;
import org.w3c.tools.resources.StringAttribute;
import org.w3c.jigsaw.http.Client;
import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.httpd;
import org.w3c.jigsaw.html.HtmlGenerator;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpChallenge;
import org.w3c.www.http.HttpCredential;
import org.w3c.www.http.HttpFactory;
import org.w3c.jigsaw.frames.HTTPFrame;
import org.w3c.util.StringUtils;
import org.w3c.tools.resources.ProtocolException;

/**
 * Internal exception class
 */
class DigestAuthFilterException extends Exception {

    DigestAuthFilterException(String msg) {
        super(msg);
    }
}

/**
 * DigestAuthFilter provides for both IP and digest authentication.
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
 * However, this provides for the digest functionnalities.
 */
public class DigestAuthFilter extends AuthFilter {

    public class DigestAuthContext {

        String dac_user = null;

        String dac_realm = null;

        String dac_nonce = null;

        String dac_uri = null;

        String dac_response = null;

        String dac_algorithm = null;

        String dac_method = null;

        boolean stale = false;

        DigestAuthContext(Request request) throws DigestAuthFilterException, ProtocolException {
            HttpCredential credential = null;
            credential = (request.isProxy() ? request.getProxyAuthorization() : request.getAuthorization());
            if (!credential.getScheme().equalsIgnoreCase("Digest")) {
                String msg = ("Invalid authentication scheme \"" + credential.getScheme() + " expecting \"Digest\"");
                throw new DigestAuthFilterException(msg);
            }
            dac_user = credential.getAuthParameter("username");
            dac_uri = credential.getAuthParameter("uri");
            dac_response = credential.getAuthParameter("response");
            dac_realm = credential.getAuthParameter("realm");
            dac_method = request.getMethod();
            dac_nonce = credential.getAuthParameter("nonce");
            if (dac_user == null || dac_uri == null || dac_response == null || dac_realm == null) {
                String msg = ("Invalid authentication header");
                throw new DigestAuthFilterException(msg);
            }
        }

        boolean authenticate(String username, String realm, String passwd) {
            stale = false;
            if (!dac_user.equals(username)) return false;
            if (!dac_realm.equals(realm)) return false;
            if (dac_algorithm != null && !dac_algorithm.equals(getAlgorithm())) return false;
            if (!dac_nonce.equals(nonce)) {
                if (!dac_nonce.equals(old_nonce)) {
                    String a1, a2, ha1, ha2;
                    a1 = username + ":" + realm + ":" + passwd;
                    a2 = dac_method + ":" + dac_uri;
                    MessageDigest md = null;
                    try {
                        md = MessageDigest.getInstance(getAlgorithm());
                    } catch (NoSuchAlgorithmException algex) {
                        return false;
                    }
                    md.update(a1.getBytes());
                    ha1 = StringUtils.toHexString(md.digest());
                    md.reset();
                    md.update(a2.getBytes());
                    ha2 = StringUtils.toHexString(md.digest());
                    md.reset();
                    String kd, hkd;
                    kd = ha1 + ":" + dac_nonce + ":" + ha2;
                    md.update(kd.getBytes());
                    hkd = StringUtils.toHexString(md.digest());
                    stale = hkd.equals(dac_response);
                    return false;
                } else stale = true;
            }
            String a1, a2, ha1, ha2;
            a1 = username + ":" + realm + ":" + passwd;
            a2 = dac_method + ":" + dac_uri;
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance(getAlgorithm());
            } catch (NoSuchAlgorithmException algex) {
                return false;
            }
            md.update(a1.getBytes());
            ha1 = StringUtils.toHexString(md.digest());
            md.reset();
            md.update(a2.getBytes());
            ha2 = StringUtils.toHexString(md.digest());
            md.reset();
            String kd, hkd;
            if (stale) kd = ha1 + ":" + old_nonce + ":" + ha2; else kd = ha1 + ":" + nonce + ":" + ha2;
            md.update(kd.getBytes());
            hkd = StringUtils.toHexString(md.digest());
            if (!hkd.equals(dac_response)) return false;
            return true;
        }
    }

    /**
     * Attribute index - The list of allowed users.
     */
    protected static int ATTR_ALLOWED_USERS = -1;

    /**
     * Attribute index - The list of allowed groups.
     */
    protected static int ATTR_ALLOWED_GROUPS = -1;

    /**
     * Attribute index - The algorithm used
     */
    protected static int ATTR_ALGORITHM = -1;

    /**
     * Attribute index - The nonce time to live (in seconds)
     */
    protected static int ATTR_NONCE_TTL = -1;

    static {
        Attribute a = null;
        Class c = null;
        try {
            c = Class.forName("org.w3c.jigsaw.auth.DigestAuthFilter");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        a = new StringArrayAttribute("users", null, Attribute.EDITABLE);
        ATTR_ALLOWED_USERS = AttributeRegistry.registerAttribute(c, a);
        a = new StringArrayAttribute("groups", null, Attribute.EDITABLE);
        ATTR_ALLOWED_GROUPS = AttributeRegistry.registerAttribute(c, a);
        a = new StringAttribute("algorithm", null, Attribute.EDITABLE);
        ATTR_ALGORITHM = AttributeRegistry.registerAttribute(c, a);
        a = new IntegerAttribute("nonce_ttl", new Integer(300), Attribute.EDITABLE);
        ATTR_NONCE_TTL = AttributeRegistry.registerAttribute(c, a);
    }

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
     * The challenge to issue to any client for Digest Authentication.
     */
    protected HttpChallenge challenge = null;

    /**
     * The nonce value of the digest, changed every X mn
     */
    protected String nonce = null;

    /**
     * The previous nonce value of the digest, changed every X mn
     */
    protected String old_nonce = null;

    private long prev_date = 0;

    private int nonce_ttl = 600;

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
        loaded_realm = name;
    }

    /**
     * Check that our realm does exist.
     * Otherwise we are probably being initialized, and we don't authenticate
     * yet.
     * @return A boolean <strong>true</strong> if realm can be initialized.
     */
    protected synchronized boolean checkRealm() {
        acquireRealm();
        return true;
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
     * Get the algorithm used
     */
    public String getAlgorithm() {
        return (String) getValue(ATTR_ALGORITHM, "MD5");
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
            challenge = HttpFactory.makeChallenge("Digest");
            challenge.setAuthParameter("realm", getRealm());
        }
        if (idx == ATTR_NONCE_TTL) {
            if (value instanceof Integer) nonce_ttl = ((Integer) value).intValue();
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
        Date d = new Date();
        if ((d.getTime() - prev_date) / 1000 > nonce_ttl) {
            prev_date = d.getTime();
            updateNonce();
        }
        DigestAuthContext dac = null;
        if ((request.hasAuthorization() && !request.isProxy()) || (request.isProxy() && request.hasProxyAuthorization())) {
            try {
                dac = new DigestAuthContext(request);
            } catch (DigestAuthFilterException ex) {
                dac = null;
            }
            if (dac != null) {
                ResourceReference rr_user = (ResourceReference) lookupUser(dac.dac_user);
                try {
                    AuthUser user = (AuthUser) rr_user.lock();
                    if (user != null) {
                        if (user.definesAttribute("password")) {
                            if (dac.authenticate(user.getName(), loaded_realm, user.getPassword())) {
                                request.setState(STATE_AUTHUSER, dac.dac_user);
                                request.setState(STATE_AUTHTYPE, "Digest");
                                request.setState(STATE_AUTHCONTEXT, dac);
                                return;
                            }
                        }
                    }
                } catch (InvalidResourceException ex) {
                } finally {
                    rr_user.unlock();
                }
            }
        }
        Reply e = null;
        HttpChallenge new_c;
        if (dac != null && dac.stale) {
            new_c = challenge.getClone();
            if (new_c != null) new_c.setAuthParameter("stale", "true", false); else new_c = challenge;
        } else new_c = challenge;
        if (request.isProxy()) {
            e = request.makeReply(HTTP.PROXY_AUTH_REQUIRED);
            e.setProxyAuthenticate(new_c);
        } else {
            e = request.makeReply(HTTP.UNAUTHORIZED);
            e.setWWWAuthenticate(new_c);
        }
        HtmlGenerator g = new HtmlGenerator("Unauthorized");
        g.append("<h1>Unauthorized access</h1>" + "<p>You are denied access to this resource.");
        e.setStream(g);
        throw new HTTPException(e);
    }

    /**
     * update the nonce string
     */
    private void updateNonce() {
        updateNonce(getResource());
    }

    private synchronized void updateNonce(FramedResource fr) {
        HTTPFrame htf;
        if (fr instanceof HTTPFrame) {
            htf = (HTTPFrame) fr;
            try {
                MessageDigest md = MessageDigest.getInstance(getAlgorithm());
                md.update((new Date()).toString().getBytes());
                try {
                    md.update(htf.getETag().getTag().getBytes());
                } catch (Exception ex) {
                    md.update(htf.getURLPath().getBytes());
                }
                byte b[] = md.digest();
                if (nonce != null) old_nonce = nonce;
                nonce = StringUtils.toHexString(b);
                challenge.setAuthParameter("nonce", nonce);
            } catch (NoSuchAlgorithmException algex) {
            }
        }
    }

    /**
     * Add the appropriate cache control directives on the way back.
     * @param request The request that has been processed.
     * @param reply The original reply.
     * @return Always <strong>null</strong>.
     */
    public ReplyInterface outgoingFilter(RequestInterface request, ReplyInterface reply) {
        Request req = (Request) request;
        Reply rep = (Reply) reply;
        if (getPrivateCachability()) {
            rep.setMustRevalidate(true);
        } else if (getSharedCachability()) {
            rep.setProxyRevalidate(true);
        } else if (getPublicCachability()) {
            rep.setPublic(true);
        }
        if (req.hasState(AuthFilter.STATE_AUTHCONTEXT)) {
            DigestAuthContext dac;
            dac = (DigestAuthContext) req.getState(AuthFilter.STATE_AUTHCONTEXT);
            if (dac.stale) {
                rep.addAuthenticationInfo("nextnonce", nonce);
            }
        }
        return null;
    }

    /**
     * Initialize the filter.
     */
    public void initialize(Object values[]) {
        super.initialize(values);
        if (getRealm() != null) {
            challenge = HttpFactory.makeChallenge("Digest");
            challenge.setAuthParameter("realm", getRealm());
            updateNonce();
            challenge.setAuthParameter("domain", getURLPath());
            challenge.setAuthParameter("algorithm", getAlgorithm(), false);
        }
    }
}
