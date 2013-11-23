package org.w3c.jigsaw.acl;

import java.net.InetAddress;
import java.security.Principal;
import java.util.Hashtable;
import org.w3c.jigsaw.auth.AuthUser;
import org.w3c.jigsaw.auth.IPMatcher;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class AuthUserPrincipal implements AclPrincipal {

    protected String name = null;

    protected String password = null;

    protected String realm = null;

    protected Hashtable values = null;

    protected IPMatcher ipmatcher = null;

    public boolean equals(Object another) {
        if (another instanceof AuthUserPrincipal) {
            return toString().equals(another.toString());
        } else {
            return another.equals(this);
        }
    }

    public String toString() {
        if (password == null) return name; else return name + ":" + password;
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public String getName() {
        return name;
    }

    public String getRealm() {
        return realm;
    }

    public String getPassword() {
        return password;
    }

    public void setValue(String name, Object value) {
        values.put(name, value);
    }

    public Object getValue(String name) {
        return values.get(name);
    }

    public boolean matchIP(InetAddress adr) {
        return (ipmatcher.lookup(adr) == Boolean.TRUE);
    }

    public AuthUserPrincipal(AuthUser user, String realm) {
        this.name = user.getName();
        this.password = user.getPassword();
        this.realm = realm;
        this.ipmatcher = new IPMatcher();
        this.values = new Hashtable();
        short ips[][] = user.getIPTemplates();
        if (ips != null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(578);
for (int i = 0; i < ips.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(578);
ipmatcher.add(ips[i], Boolean.TRUE);} 
edu.hkust.clap.monitor.Monitor.loopEnd(578);

        }
    }
}
