package org.w3c.www.protocol.http.cache;

import java.io.IOException;
import org.w3c.www.protocol.http.Reply;
import org.w3c.www.protocol.http.Request;

/** 
 * The factory for cache entries.
 * All cache entries have to be sub-classes of CachedResource, that's the
 * only limitation to the fun you can have down here.
 */
public class CachedResourceFactory {

    /**
     * Create a suitable instance of some subclass of CachedResource.
     * @param filter The cache filter that ones to create a new entry.
     * @param request The original request we emitted.
     * @param reply The reply we got from the origin server.
     * @return An instance of CachedResource, or <strong>null</strong>
     * if no resource was created.
     */
    public static CachedResource createResource(CacheFilter filter, Request request, Reply reply) throws IOException {
        CachedResource r = null;
        String v[] = reply.getVary();
        if (v == null) {
            r = new EntityCachedResource(filter, request, reply);
        } else {
            r = new EntityCachedResource(filter, request, reply);
        }
        return r;
    }
}
