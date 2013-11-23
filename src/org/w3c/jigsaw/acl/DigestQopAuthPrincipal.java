package org.w3c.jigsaw.acl;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.w3c.jigsaw.http.Request;
import org.w3c.www.http.HttpCredential;
import org.w3c.util.StringUtils;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class DigestQopAuthPrincipal extends DigestAuthPrincipal {

    String dac_opaque = null;

    String dac_cnonce = null;

    String dac_nc = null;

    String dac_qop = null;

    /**
     * Check that the challenge matches with the provided nonce
     * @return true if it matches
     */
    private boolean checkDigest2617(String username, String realm, String password, String nonce) {
        String a1, a2, ha1, ha2;
        StringBuffer sb = new StringBuffer(256);
        sb.append(username).append(':').append(realm);
        sb.append(':').append(password);
        a1 = sb.toString();
        sb = new StringBuffer(256);
        sb.append(dac_method).append(':').append(dac_uri);
        a2 = sb.toString();
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(this.algo);
        } catch (NoSuchAlgorithmException algex) {
            return false;
        }
        try {
            md.update(a1.getBytes("ISO-8859-1"));
            ha1 = StringUtils.toHexString(md.digest());
            md.reset();
            md.update(a2.getBytes("ISO-8859-1"));
            ha2 = StringUtils.toHexString(md.digest());
            md.reset();
            String kd, hkd;
            sb = new StringBuffer(256);
            sb.append(ha1).append(':').append(dac_nonce).append(':');
            sb.append(dac_nc).append(':').append(dac_cnonce).append(':');
            sb.append(dac_qop).append(':').append(ha2);
            kd = sb.toString();
            md.update(kd.getBytes("ISO-8859-1"));
            hkd = StringUtils.toHexString(md.digest());
            return hkd.equals(dac_response);
        } catch (Exception ex) {
        }
        return false;
    }

    public boolean equals(Object another) {
        if (no_user) return false;
        if (another instanceof AclPrincipal) {
            AclPrincipal aclp = (AclPrincipal) another;
            String username = aclp.getName();
            String realm = aclp.getRealm();
            String passwd = aclp.getPassword();
            if (!dac_user.equals(username)) return false;
            if (!dac_realm.equals(realm)) return false;
            if (dac_algorithm != null && !dac_algorithm.equals(this.algo)) return false;
            if (!dac_nonce.equals(this.nonce)) {
                if (dac_nonce.equals(this.old_nonce)) {
                    if (checkDigest2617(username, realm, passwd, old_nonce)) {
                        stale = true;
                        return true;
                    }
                } else {
                    if (checkDigest2617(username, realm, passwd, dac_nonce)) {
                        System.out.println("** stale!");
                        stale = true;
                    }
                }
                return false;
            }
            return checkDigest2617(username, realm, passwd, nonce);
        } else if (another instanceof DigestQopAuthPrincipal) {
            return false;
        }
        return false;
    }

    public DigestQopAuthPrincipal(Request request, String nonce, String old_nonce, String algo) throws InvalidAuthException {
        super(request, algo);
        this.request = request;
        HttpCredential credential = (request.isProxy() ? request.getProxyAuthorization() : request.getAuthorization());
        if ((credential == null) || (!credential.getScheme().equalsIgnoreCase("Digest"))) {
            no_user = true;
        } else {
            no_user = false;
            dac_user = credential.getAuthParameter("username");
            dac_uri = credential.getAuthParameter("uri");
            dac_response = credential.getAuthParameter("response");
            dac_realm = credential.getAuthParameter("realm");
            dac_method = request.getMethod();
            dac_nonce = credential.getAuthParameter("nonce");
            dac_opaque = credential.getAuthParameter("opaque");
            dac_cnonce = credential.getAuthParameter("cnonce");
            dac_nc = credential.getAuthParameter("nc");
            dac_qop = credential.getAuthParameter("qop");
            if (dac_qop == null) {
                dac_qop = "auth";
            } else {
                if (!dac_qop.equals("auth")) {
                    String msg = "qop value not supported";
                    throw new InvalidAuthException(msg);
                }
            }
            this.nonce = nonce;
            this.old_nonce = old_nonce;
            this.algo = algo;
            if (dac_user == null || dac_uri == null || dac_response == null || dac_realm == null || dac_cnonce == null) {
                String msg = ("Invalid authentication header");
                throw new InvalidAuthException(msg);
            }
        }
    }

    public DigestQopAuthPrincipal(Request request) throws InvalidAuthException {
        super(request);
        throw new InvalidAuthException("Bad call for authentification");
    }
}
