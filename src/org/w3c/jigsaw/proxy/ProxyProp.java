package org.w3c.jigsaw.proxy;

import java.util.Enumeration;
import java.io.File;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.StringArrayAttribute;
import org.w3c.tools.resources.StringAttribute;
import org.w3c.jigsaw.http.httpd;
import org.w3c.jigsaw.config.PropertySet;
import org.w3c.util.ObservableProperties;
import org.w3c.www.protocol.http.micp.MICPProp;
import org.w3c.www.protocol.http.HttpManager;
import org.w3c.www.protocol.http.cache.CacheFilter;
import org.w3c.www.protocol.http.cache.CacheStore;

class ProxyProp extends PropertySet {

    private static String title = "Proxy properties";

    private static String MICP_PROP_NAME = "micp";

    private static String CACHE_PROP_NAME = "cache";

    private static String PROXY_DISP_PROP_NAME = "dispatcher";

    protected static int ATTR_FILTERS = -1;

    static {
        Class c = null;
        Attribute a = null;
        try {
            c = Class.forName("org.w3c.jigsaw.proxy.ProxyProp");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        a = new IntegerAttribute(HttpManager.CONN_MAX_P, new Integer(20), Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new IntegerAttribute(HttpManager.TIMEOUT_P, new Integer(300000), Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new IntegerAttribute(HttpManager.CONN_TIMEOUT_P, new Integer(1000), Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new BooleanAttribute(HttpManager.PROXY_SET_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new StringAttribute(HttpManager.PROXY_HOST_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new IntegerAttribute(HttpManager.PROXY_PORT_P, new Integer(80), Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new BooleanAttribute(HttpManager.LENIENT_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new StringArrayAttribute(HttpManager.FILTERS_PROP_P, null, Attribute.EDITABLE);
        ATTR_FILTERS = AttributeRegistry.registerAttribute(c, a);
    }

    /**
     * Get this property set title.
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
    protected String[] getFilters() {
        return (String[]) getValue(ATTR_FILTERS, null);
    }

    protected void initializeFiltersProps() {
        String flt[] = getFilters();
        if (flt == null) return;
        PropertySet p = null;
        edu.hkust.clap.monitor.Monitor.loopBegin(962);
for (int i = 0; i < flt.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(962);
{
            if (flt[i].equals("org.w3c.www.protocol.http.micp.MICPFilter")) {
                if (server.getPropertySet(MICP_PROP_NAME) == null) {
                    p = new MICPProp(MICP_PROP_NAME, server);
                    server.registerPropertySet(p);
                }
            } else if (flt[i].equals("org.w3c.www.protocol.http.cache.CacheFilter")) {
                if (server.getPropertySet(CACHE_PROP_NAME) == null) {
                    p = new CacheProp(CACHE_PROP_NAME, server);
                    server.registerPropertySet(p);
                    ObservableProperties props = server.getProperties();
                    File c = props.getFile(CacheStore.CACHE_DIRECTORY_P, null);
                    if (c == null) {
                        c = new File(server.getConfigDirectory(), "cache");
                        props.putValue(CacheStore.CACHE_DIRECTORY_P, c.getAbsolutePath());
                    }
                }
            } else if (flt[i].equals("org.w3c.www.protocol.http.proxy.ProxyDispatcher")) {
                if (server.getPropertySet(PROXY_DISP_PROP_NAME) == null) {
                    p = new ProxyDispatcherProp(PROXY_DISP_PROP_NAME, server);
                    server.registerPropertySet(p);
                }
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(962);

    }

    public synchronized void setValue(int idx, Object value) {
        super.setValue(idx, value);
        if (idx == ATTR_FILTERS) initializeFiltersProps();
    }

    ProxyProp(String name, httpd server) {
        super(name, server);
        initializeFiltersProps();
    }
}
