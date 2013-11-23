package org.w3c.jigsaw.acl;

import java.security.acl.Permission;
import org.w3c.jigsaw.http.Request;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class HTTPPermission implements Permission {

    protected String method = null;

    protected String getMethod() {
        return method;
    }

    public HTTPPermission(Request request) {
        this.method = request.getMethod();
    }

    public boolean equals(Object another) {
        if (another instanceof HTTPPermission) {
            return method.equals(((HTTPPermission) another).getMethod());
        } else {
            return method.equals(another.toString());
        }
    }

    public String toString() {
        return method + " permission";
    }
}
