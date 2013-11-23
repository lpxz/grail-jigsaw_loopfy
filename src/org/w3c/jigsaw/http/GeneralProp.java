package org.w3c.jigsaw.http;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.FileAttribute;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.StringArrayAttribute;
import org.w3c.tools.resources.StringAttribute;
import org.w3c.jigsaw.config.PropertySet;

/**
 * A wrapper class to give access to editable properties through a resource.
 * This class allows to reuse entirely the generic resource editor to
 * edit the properties of the server.
 */
class GeneralProp extends PropertySet {

    private static final String title = "General properties";

    protected static int ATTR_PORT = -1;

    static {
        Class c = null;
        Attribute a = null;
        try {
            c = Class.forName("org.w3c.jigsaw.http.GeneralProp");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        a = new StringAttribute(httpd.SERVER_SOFTWARE_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new BooleanAttribute(httpd.FS_SENSITIVITY, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new FileAttribute(httpd.ROOT_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new StringAttribute(httpd.HOST_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new IntegerAttribute(httpd.PORT_P, null, Attribute.EDITABLE);
        ATTR_PORT = AttributeRegistry.registerAttribute(c, a);
        a = new StringAttribute(httpd.ROOT_NAME_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new StringArrayAttribute(httpd.PUBLIC_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new BooleanAttribute(httpd.TRACE_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new StringAttribute(httpd.DOCURL_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new StringAttribute(httpd.CHECKURL_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new StringAttribute(httpd.TRASHDIR_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new StringAttribute(httpd.SERIALIZER_CLASS_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new BooleanAttribute(httpd.DISPLAY_URL_ON_ERROR_P, new Boolean(false), Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new BooleanAttribute(httpd.LENIENT_P, new Boolean(true), Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
    }

    /**
     * This property set's title.
     * @return A String encoded title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set value forwards the effectation to the properties.
     * @param idx The attribute (property in that case) being set.
     * @param value The new value for that property.
     */
    public synchronized void setValue(int idx, Object value) {
        super.setValue(idx, value);
    }

    GeneralProp(String name, httpd server) {
        super(name, server);
    }
}
