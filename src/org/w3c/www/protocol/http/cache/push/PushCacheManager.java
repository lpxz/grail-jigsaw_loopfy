package org.w3c.www.protocol.http.cache.push;

import org.w3c.util.ArrayDictionary;
import org.w3c.www.protocol.http.Request;
import org.w3c.www.protocol.http.HttpManager;
import org.w3c.www.protocol.http.cache.CachedResource;
import org.w3c.www.protocol.http.cache.CacheGeneration;
import java.net.URL;
import java.util.Enumeration;

/**
 * Singleton class to manage push cache.
 * @see PushCacheHandler
 *
 * @author Paul Henshaw, The Fantastic Corporation, Paul.Henshaw@fantastic.com
 * @version $Revision: 1.1 $
 * $Id: PushCacheManager.java,v 1.1 2010/06/15 12:25:43 smhuang Exp $
 */
public class PushCacheManager {

    /**
     * Additional header field and value to identify push resources
     */
    public static final String HEADER_FIELD = "tfc_from_push_stream";

    public static final String HEADER_VALUE = "yes";

    private static PushCacheManager _instance = null;

    private PushCacheFilter _filter = null;

    /**
     * Access to manager
     */
    public static PushCacheManager instance() {
        if (_instance == null) {
            _instance = new PushCacheManager();
        }
        return (_instance);
    }

    /**
     * <bold>true</bold> iff res contains extra header identifying it as
     * having been inserted directly into the cache from a PUSH source
     */
    public boolean isPushResource(CachedResource res) {
        if (res != null) {
            ArrayDictionary dict = res.getExtraHeaders();
            String v = (String) dict.get(HEADER_FIELD);
            if (v != null && v.equalsIgnoreCase(HEADER_VALUE)) {
                return (true);
            }
        }
        return (false);
    }

    /**
     * Store a push reply in the cache
     */
    public void storeReply(PushReply reply) {
        try {
            String url = reply.getUrl();
            Request request = HttpManager.getManager().createRequest();
            request.setMethod("GET");
            request.setURL(new URL(url));
            removeURL(url);
            CachedResource res = new PushEntityCachedResource(_filter, request, reply);
            getStore().storeCachedResource(res, res.getContentLength());
            _filter.sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove a reply from the cache
     */
    public void removeURL(String url) {
        try {
            deleteRes(getStore().getCachedResourceReference(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 
     *  True iff url is present in cache
     */
    public boolean isPresent(String url) {
        try {
            CachedResource res = getStore().getCachedResourceReference(url);
            if (res == null) {
                return (false);
            }
            if (res.getFile() == null || !res.getFile().exists()) {
                return (false);
            }
            return (true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (false);
    }

    /**
     * Remove resource from cache
     */
    protected void deleteRes(CachedResource res) {
        if (res != null) {
            res.delete();
            getStore().getState().notifyResourceDeleted(res);
            _filter.sync();
        }
    }

    /**
     * Remove all entries from the cache
     */
    public void cleanCache() {
        CacheGeneration gen = getStore().getMRUGeneration();
        edu.hkust.clap.monitor.Monitor.loopBegin(390);
while (gen != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(390);
{
            Enumeration e = gen.getCachedResources();
            edu.hkust.clap.monitor.Monitor.loopBegin(391);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(391);
{
                deleteRes((CachedResource) e.nextElement());
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(391);

            gen = getStore().getNextGeneration(gen);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(390);

    }

    /**
     * Called by PushCacheFilter on initialization
     */
    protected void registerFilter(PushCacheFilter cf) {
        _filter = cf;
    }

    /**
     * Singleton; no public constructor
     * @see #instance
     */
    protected PushCacheManager() {
    }

    /**
     * Access to filter store
     */
    protected PushCacheStore getStore() {
        return (_filter.getPushCacheStore());
    }
}
