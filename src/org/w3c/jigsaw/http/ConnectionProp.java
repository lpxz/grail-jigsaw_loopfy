package org.w3c.jigsaw.http;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.jigsaw.config.PropertySet;

/**
 * A wrapper class to give access to editable properties through a resource.
 * This class allows to reuse entirely the generic resource editor to
 * edit the properties of the server.
 */
class ConnectionProp extends PropertySet {

    private static String title = "Connection properties";

    static {
        Class c = null;
        Attribute a = null;
        try {
            c = Class.forName("org.w3c.jigsaw.http.ConnectionProp");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        a = new BooleanAttribute(httpd.KEEP_ALIVE_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new IntegerAttribute(httpd.CLIENT_PRIORITY_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new IntegerAttribute(httpd.CLIENT_BUFSIZE_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new BooleanAttribute(httpd.CLIENT_DEBUG_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new IntegerAttribute(httpd.REQUEST_TIMEOUT_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
    }

    /**
     * Get this property set title.
     * @return A String encoded title.
     */
    public String getTitle() {
        return title;
    }

    ConnectionProp(String name, httpd server) {
        super(name, server);
    }
}
