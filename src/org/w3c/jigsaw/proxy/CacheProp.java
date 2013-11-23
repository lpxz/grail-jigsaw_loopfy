package org.w3c.jigsaw.proxy;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.DoubleAttribute;
import org.w3c.tools.resources.FileAttribute;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.LongAttribute;
import org.w3c.tools.resources.StringAttribute;
import org.w3c.jigsaw.http.httpd;
import org.w3c.jigsaw.config.PropertySet;
import org.w3c.www.protocol.http.HttpManager;
import org.w3c.www.protocol.http.cache.CacheFilter;
import org.w3c.www.protocol.http.cache.CacheStore;

class CacheProp extends PropertySet {

    private static String title = "Cache properties";

    static {
        Class c = null;
        Attribute a = null;
        try {
            c = Class.forName("org.w3c.jigsaw.proxy.CacheProp");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        a = new FileAttribute(CacheStore.CACHE_DIRECTORY_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new LongAttribute(CacheFilter.CACHE_SIZE_P, new Long(20971520), Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new LongAttribute(CacheStore.STORE_SIZE_P, new Long(23068672), Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new BooleanAttribute(CacheFilter.DEBUG_P, Boolean.FALSE, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new BooleanAttribute(CacheFilter.SHARED_P, Boolean.TRUE, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new BooleanAttribute(CacheFilter.CACHE_CONNECTED_P, Boolean.TRUE, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new BooleanAttribute(CacheStore.GARBAGE_COLLECTION_ENABLED_P, Boolean.TRUE, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new DoubleAttribute(CacheStore.FILE_SIZE_RATIO_P, new Double((double) 0.1), Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new DoubleAttribute(CacheStore.GARBAGE_COLLECTION_THRESHOLD_P, new Double((double) 0.8), Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new LongAttribute(CacheStore.SYNCHRONIZATION_DELAY_P, new Long(60000), Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new LongAttribute(CacheStore.GENERATION_COMPACT_DELAY_P, new Long(60000), Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new IntegerAttribute(CacheStore.MAX_CACHED_RESOURCES_P, new Integer(50000), Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new IntegerAttribute(CacheStore.MAX_GENERATIONS_P, new Integer(10), Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new StringAttribute(CacheFilter.VALIDATOR_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new StringAttribute(CacheFilter.SWEEPER_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new StringAttribute(CacheFilter.SERIALIZER_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
    }

    /**
     * Get this property set title.
     * @return A String encoded title.
     */
    public String getTitle() {
        return title;
    }

    CacheProp(String name, httpd server) {
        super(name, server);
    }
}
