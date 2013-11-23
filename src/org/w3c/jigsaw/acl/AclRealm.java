package org.w3c.jigsaw.acl;

import java.security.Principal;
import java.security.acl.AclEntry;
import java.security.acl.LastOwnerException;
import java.security.acl.NotOwnerException;
import java.security.acl.Permission;
import java.util.Enumeration;
import java.util.Vector;
import org.w3c.jigsaw.auth.AuthRealm;
import org.w3c.jigsaw.auth.AuthUser;
import org.w3c.jigsaw.auth.AuthFilter;
import org.w3c.jigsaw.auth.IPMatcher;
import org.w3c.jigsaw.auth.RealmsCatalog;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.httpd;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.StringAttribute;
import org.w3c.tools.resources.StringArrayAttribute;

/**
 * @version $Revision: 1.2 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class AclRealm extends JAcl {

    /**
     * Attribute index - The realm name for this ACL.
     */
    protected static int ATTR_REALM = -1;

    /**
     * Attribute index - The list of allowed users.
     */
    protected static int ATTR_ALLOWED_USERS = -1;

    /**
     * Attribute index - The methods protected by the filter.
     */
    protected static int ATTR_METHODS = -1;

    static {
        Attribute a = null;
        Class c = null;
        try {
            c = Class.forName("org.w3c.jigsaw.acl.AclRealm");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        a = new StringAttribute("realm", null, Attribute.EDITABLE | Attribute.MANDATORY);
        ATTR_REALM = AttributeRegistry.registerAttribute(c, a);
        a = new StringArrayAttribute("users", null, Attribute.EDITABLE);
        ATTR_ALLOWED_USERS = AttributeRegistry.registerAttribute(c, a);
        a = new StringArrayAttribute("methods", null, Attribute.EDITABLE);
        ATTR_METHODS = AttributeRegistry.registerAttribute(c, a);
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

    protected Vector entries = null;

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
     * Get the list of allowed users.
     */
    public String[] getAllowedUsers() {
        return (String[]) getValue(ATTR_ALLOWED_USERS, null);
    }

    /**
     * Get a pointer to our realm, and initialize our ipmatcher.
     */
    protected synchronized void acquireRealm() {
        entries = new Vector(10);
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
                edu.hkust.clap.monitor.Monitor.loopBegin(472);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(472);
{
                    String uname = (String) e.nextElement();
                    ResourceReference rr_user = realm.loadUser(uname);
                    try {
                        AuthUser user = (AuthUser) rr_user.lock();
                        createEntry(user);
                    } catch (InvalidResourceException ex) {
                        System.out.println("Invalid user reference : " + uname);
                    } finally {
                        rr_user.unlock();
                    }
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(472);

            } catch (InvalidResourceException ex) {
            } finally {
                rr_realm.unlock();
            }
        }
    }

    /**
     * Is this user allowed in the realm ?
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

        } else {
            return true;
        }
        return false;
    }

    protected void createEntry(AuthUser user) {
        if (checkUser(user)) entries.addElement(new AuthUserPrincipal(user, getName()));
    }

    protected boolean hasPrincipal(Principal p) {
        int idx = entries.indexOf(p);
        return (idx != -1);
    }

    public boolean addOwner(Principal caller, Principal owner) throws NotOwnerException {
        throw new NotOwnerException();
    }

    public boolean deleteOwner(Principal caller, Principal owner) throws NotOwnerException, LastOwnerException {
        throw new NotOwnerException();
    }

    public boolean isOwner(Principal owner) {
        return false;
    }

    public void setName(Principal caller, String name) throws NotOwnerException {
        throw new NotOwnerException();
    }

    public String getName() {
        return getRealm();
    }

    public boolean addEntry(Principal caller, AclEntry entry) throws NotOwnerException {
        throw new NotOwnerException();
    }

    public boolean removeEntry(Principal caller, AclEntry entry) throws NotOwnerException {
        throw new NotOwnerException();
    }

    public Enumeration getPermissions(Principal user) {
        return null;
    }

    public Enumeration entries() {
        return null;
    }

    public boolean checkPermission(Principal principal, Permission permission) {
        acquireRealm();
        String methods[] = getMethods();
        boolean methodprotected = false;
        if (methods != null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(473);
for (int i = 0; i < methods.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(473);
{
                if (permission.equals(methods[i])) methodprotected = true;
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(473);

        } else {
            methodprotected = true;
        }
        if (!methodprotected) return true;
        boolean granted = hasPrincipal(principal);
        if (granted) {
            String username = principal.getName();
            if (username != null) {
                try {
                    HTTPPrincipal htp = (HTTPPrincipal) principal;
                    Request request = htp.getRequest();
                    if (username != null) {
                        request.setState(AuthFilter.STATE_AUTHUSER, username);
                    }
                } catch (Exception ex) {
                }
            }
        }
        return granted;
    }

    public String toString() {
        return getName();
    }

    /**
     * Initialize the Acl.
     */
    public void initialize(Object values[]) {
        super.initialize(values);
    }
}
