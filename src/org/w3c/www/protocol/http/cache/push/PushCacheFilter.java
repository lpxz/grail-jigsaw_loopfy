package org.w3c.www.protocol.http.cache.push;

import java.net.URL;
import org.w3c.www.http.HTTP;
import org.w3c.www.protocol.http.Reply;
import org.w3c.www.protocol.http.Request;
import org.w3c.www.protocol.http.HttpException;
import org.w3c.www.protocol.http.HttpManager;
import org.w3c.www.protocol.http.PropRequestFilterException;
import org.w3c.www.protocol.http.cache.CacheFilter;
import org.w3c.www.protocol.http.cache.CacheSweeper;
import org.w3c.www.protocol.http.cache.CachedResource;
import org.w3c.www.protocol.http.cache.EntityCachedResource;
import org.w3c.www.protocol.http.cache.InvalidCacheException;
import org.w3c.www.protocol.http.cache.CacheState;
import org.w3c.www.protocol.http.cache.CacheValidator;
import org.w3c.www.protocol.http.cache.CacheSerializer;
import org.w3c.www.protocol.http.cache.ActiveStream;

/**
 * PushCacheFilter
 * Based heavily on (much code stolen from) CacheFilter
 * The important differences are in the initialization where the 
 * PushCacheListener is started, and in ingoingFilter where if 
 * the requested resource is present in the cache and is a PUSH
 * resource, then the resource is returned immediately without
 * checking for expiry etc.  This allows us to insert pages from
 * "virtual" web sites such as http://www.push.data/sensor1.html
 *
 * @author Paul Henshaw, The Fantastic Corporation, Paul.Henshaw@fantastic.com
 * @version $Revision: 1.2 $
 * $Id: PushCacheFilter.java,v 1.2 2010/06/15 17:53:11 smhuang Exp $
 */
public class PushCacheFilter extends CacheFilter {

    /**
     * Property name used to acquire port number for {@link PushCacheListener}
     * value is "org.w3c.www.protocol.http.cache.push.portnumber";
     */
    public static final String PORT_NUM_P = "org.w3c.www.protocol.http.cache.push.portnumber";

    /**
     * Default port number to use if property value is not supplied 
     * value is 9876
     */
    public static final int DEFAULT_PORT_NUM = 9876;

    /**
     * Access to PushCacheStore
     */
    public PushCacheStore getPushCacheStore() {
        return ((PushCacheStore) super.getStore());
    }

    /**
     * check if we can use the cache or not for this request
     * It marks the request as being not cachable if false.
     * @param a request, the incoming client-side request
     * @return a boolean, true if we can use the cache
     */
    public boolean canUseCache(Request req) {
        return true;
    }

    /**
     * The request pre-processing hook.
     * Before each request is launched, all filters will be called back through
     * this method. They will generally set up additional request header
     * fields to enhance the request.
     * @param request The request that is about to be launched.
     * @return An instance of Reply if the filter could handle the request,
     * or <strong>null</strong> if processing should continue normally.
     * @exception HttpException If the filter is supposed to fulfill the
     * request, but some error happened during that processing.
     */
    public Reply ingoingFilter(Request request) throws HttpException {
        if (!canUseCache(request)) {
            if (debug) {
                trace(request, "*** Can't use cache");
            }
            if (connected) {
                CachedResource res = null;
                EntityCachedResource invalidRes = null;
                try {
                    String requrl = request.getURL().toExternalForm();
                    res = store.getCachedResourceReference(requrl);
                    if (res != null) {
                        invalidRes = (EntityCachedResource) res.lookupResource(request);
                    }
                } catch (InvalidCacheException ex) {
                    invalidRes = null;
                }
                if (invalidRes != null) {
                    invalidRes.setWillRevalidate(true);
                }
                request.setState(STATE_NOCACHE, Boolean.TRUE);
                return null;
            } else {
                Reply reply = request.makeReply(HTTP.GATEWAY_TIMEOUT);
                reply.setContent("The cache cannot be use for " + "<p><code>" + request.getMethod() + "</code> " + "<strong>" + request.getURL() + "</strong>" + ". <p>It is disconnected.");
                return reply;
            }
        }
        String requrl = request.getURL().toExternalForm();
        if (precache.containsKey(requrl)) {
            if (debug) System.out.println("*** Already downloading: " + requrl);
            try {
                CachedResource cr = (CachedResource) precache.get(requrl);
                return cr.perform(request);
            } catch (Exception ex) {
            }
        }
        CachedResource res = null;
        try {
            res = store.getCachedResourceReference(requrl);
        } catch (InvalidCacheException ex) {
            res = null;
        }
        try {
            if (PushCacheManager.instance().isPushResource(res)) {
                EntityCachedResource ecr = (EntityCachedResource) res.lookupResource(request);
                if (ecr != null) {
                    Reply reply = ecr.perform(request);
                    return reply;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (request.checkOnlyIfCached() || !connected) {
            EntityCachedResource ecr = null;
            if (res != null) {
                ecr = (EntityCachedResource) res.lookupResource(request);
            }
            if ((res == null) || (ecr == null)) {
                if (debug) trace(request, "unavailable (disconnected).");
                Reply reply = request.makeReply(HTTP.GATEWAY_TIMEOUT);
                reply.setContent("The cache doesn't have an entry for " + "<p><strong>" + request.getURL() + "</strong>" + ". <p>And it is disconnected.");
                return reply;
            }
            if (debug) {
                trace(request, (connected) ? " hit - only if cached" : " hit while disconneced");
            }
            if (!validator.isValid(ecr, request)) {
                addWarning(request, WARN_STALE);
            }
            addWarning(request, WARN_DISCONNECTED);
            Reply reply = ecr.perform(request);
            setWarnings(request, reply);
            return reply;
        }
        if (res != null) {
            if ((res.getLoadState() == CachedResource.STATE_LOAD_PARTIAL) || (res.getLoadState() == CachedResource.STATE_LOAD_ERROR)) {
                setRequestRevalidation(res, request);
                return null;
            }
            if (validator.isValid(res, request)) {
                try {
                    store.updateResourceGeneration(res);
                } catch (InvalidCacheException ex) {
                }
                Reply rep = res.perform(request);
                return rep;
            } else {
                if (debug) {
                    System.out.println("*** Revalidation asked for " + requrl);
                }
                setRequestRevalidation(res, request);
                return null;
            }
        }
        edu.hkust.clap.monitor.Monitor.loopBegin(579);
while (uritable.containsKey(requrl)) { 
edu.hkust.clap.monitor.Monitor.loopInc(579);
{
            synchronized (uritable) {
                try {
                    uritable.wait();
                } catch (InterruptedException ex) {
                }
            }
            if (precache.containsKey(requrl)) {
                if (debug) System.out.println("*** Already downloading: " + requrl);
                CachedResource cr = (CachedResource) precache.get(requrl);
                return cr.perform(request);
            }
            uritable.put(requrl, requrl);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(579);

        return null;
    }

    /**
     * Almost identical to CacheFilter.initialize, but creates a
     * PushCacheStore instead of a CacheStore and additionaly 
     * starts the PushCacheListener
     */
    public void initialize(HttpManager manager) throws PropRequestFilterException {
        try {
            String validator_c;
            String sweeper_c;
            String serializer_c;
            props = manager.getProperties();
            shared = props.getBoolean(SHARED_P, false);
            connected = props.getBoolean(CACHE_CONNECTED_P, true);
            debug = props.getBoolean(DEBUG_P, false);
            validator_c = props.getString(VALIDATOR_P, "org.w3c.www.protocol.http.cache.SimpleCacheValidator");
            sweeper_c = props.getString(SWEEPER_P, "org.w3c.www.protocol.http.cache.SimpleCacheSweeper");
            serializer_c = props.getString(SERIALIZER_P, "org.w3c.www.protocol.http.cache.SimpleCacheSerializer");
            try {
                Class c;
                c = Class.forName(validator_c);
                validator = (CacheValidator) c.newInstance();
                validator.initialize(this);
                c = Class.forName(sweeper_c);
                sweeper = (CacheSweeper) c.newInstance();
                sweeper.initialize(this);
                c = Class.forName(serializer_c);
                serializer = (CacheSerializer) c.newInstance();
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new PropRequestFilterException("Unable to start cache");
            }
            store = new PushCacheStore();
            try {
                store.initialize(this);
            } catch (InvalidCacheException ex) {
                if (debug) {
                    ex.printStackTrace();
                }
            }
            sweeper.start();
            ActiveStream.initialize();
            props.registerObserver(this);
            manager.setFilter(this);
            int portNum = props.getInteger(PORT_NUM_P, DEFAULT_PORT_NUM);
            PushCacheListener listener = new PushCacheListener(portNum);
            listener.start();
            PushCacheManager.instance().registerFilter(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
