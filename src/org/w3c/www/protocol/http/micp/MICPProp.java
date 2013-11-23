package org.w3c.www.protocol.http.micp;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.StringAttribute;
import org.w3c.jigsaw.http.httpd;
import org.w3c.jigsaw.config.PropertySet;
import org.w3c.www.protocol.http.HttpManager;

public class MICPProp extends PropertySet {

    private static String title = "mICP properties";

    static {
        Class c = null;
        Attribute a = null;
        try {
            c = Class.forName("org.w3c.www.protocol.http.micp.MICPProp");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        a = new IntegerAttribute(MICPFilter.PORT_P, new Integer(2005), Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new StringAttribute(MICPFilter.ADDRESS_P, "224.0.2.67", Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new IntegerAttribute(MICPFilter.TIMEOUT_P, new Integer(300), Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new BooleanAttribute(MICPFilter.DISABLE_CACHE_P, new Boolean(false), Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new BooleanAttribute(MICPFilter.DEBUG_P, new Boolean(false), Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
    }

    /**
     * Get this property set title.
     * @return A String encoded title.
     */
    public String getTitle() {
        return title;
    }

    public MICPProp(String name, httpd server) {
        super(name, server);
    }
}
