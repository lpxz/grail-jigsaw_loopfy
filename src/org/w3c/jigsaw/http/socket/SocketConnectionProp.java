package org.w3c.jigsaw.http.socket;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.StringAttribute;
import org.w3c.jigsaw.http.httpd;
import org.w3c.jigsaw.config.PropertySet;

public class SocketConnectionProp extends PropertySet {

    private static String title = "Socket connection properties";

    static {
        Class c = null;
        Attribute a = null;
        try {
            c = Class.forName("org.w3c.jigsaw.http.socket.SocketConnectionProp");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        a = new IntegerAttribute(SocketClientFactory.MINSPARE_FREE_P, new Integer(SocketClientFactory.MINSPARE_FREE), Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new IntegerAttribute(SocketClientFactory.MAXSPARE_FREE_P, new Integer(SocketClientFactory.MAXSPARE_FREE), Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new IntegerAttribute(SocketClientFactory.MAXSPARE_IDLE_P, new Integer(SocketClientFactory.MAXSPARE_IDLE), Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new IntegerAttribute(SocketClientFactory.MAXCLIENTS_P, new Integer(SocketClientFactory.MAXCLIENTS), Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new IntegerAttribute(SocketClientFactory.IDLETO_P, new Integer(SocketClientFactory.IDLETO), Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new IntegerAttribute(SocketClientFactory.MAXTHREADS_P, new Integer(SocketClientFactory.MAXTHREADS), Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new StringAttribute(SocketClientFactory.BINDADDR_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
    }

    /**
     * Get this property set title.
     * @return A String giving the title for this property set.
     */
    public String getTitle() {
        return title;
    }

    SocketConnectionProp(String name, httpd server) {
        super(name, server);
    }
}
