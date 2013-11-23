package org.w3c.www.protocol.http.cache;

import java.net.URL;
import org.w3c.util.ArrayDictionary;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpEntityTag;
import org.w3c.www.protocol.http.Reply;
import org.w3c.www.protocol.http.Request;

public class SimpleCacheValidator extends CacheValidator {

    private static final boolean debug = false;

    /**
     * Check if the request is stale or not
     * @return a boolean, false if the resource is still valid
     * true it if needs a revalidation.
     */
    public boolean checkStaleness(CachedResource cr) {
        return (cr.getCurrentAge() >= cr.getFreshnessLifetime());
    }

    /**
     * Is the currently cached version usable to answer the given request ?
     * @return A boolean, <strong>true</strong> if we are able to generate
     * a valid answer to this request by the <code>perform</code> method,
     * <strong>false</strong> otherwise (the resource needs to be refreshed).
     */
    public boolean isValid(CachedResource cr, Request request) {
        EntityCachedResource rcr;
        rcr = (EntityCachedResource) cr.lookupResource(request);
        if ((rcr == null) || rcr.getWillRevalidate()) {
            return false;
        }
        if (request.getMaxAge() == 0) {
            rcr.setWillRevalidate(true);
            return false;
        }
        String vary[] = cr.getVary();
        if (vary != null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(475);
for (int i = 0; i < vary.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(475);
{
                if (vary[i].equals("*")) {
                    return false;
                }
                ArrayDictionary a = cr.getConnegHeaders();
                String rh = null;
                String crh = null;
                String lowh = null;
                if (a == null) {
                    return false;
                }
                lowh = vary[i].toLowerCase();
                crh = (String) a.get(lowh);
                rh = request.getValue(vary[i]);
                if (crh == null) {
                    if (rh == null) {
                        continue;
                    }
                    return false;
                } else if (rh == null) {
                    return false;
                }
                if (!rh.equals(crh)) {
                    return false;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(475);

        }
        int maxage = request.getMaxAge();
        int minfresh = request.getMinFresh();
        int maxstale = request.getMaxStale();
        int currage = rcr.getCurrentAge();
        int freshtime = rcr.getFreshnessLifetime();
        if (debug) {
            System.out.println("* Maxage    :" + maxage);
            System.out.println("* MinFresh  :" + minfresh);
            System.out.println("* MaxStale  :" + maxstale);
            System.out.println("* CurrAge   :" + currage);
            System.out.println("* Freshtime :" + freshtime);
        }
        if (maxage == -1) {
            maxage = freshtime;
        }
        if (currage < maxage) {
            if (minfresh != -1) {
                if (currage + minfresh > freshtime) {
                    return false;
                }
            }
            return true;
        }
        if ((maxstale != -1) && (minfresh == -1)) {
            if (currage < maxage + maxstale) {
                return true;
            }
        }
        return false;
    }

    /**
     * Update the expiration information on a cached resource, even if it was
     * not used. Note that it is the right place to update also information
     * for other cache behaviour used by the sweeper.
     * @param cr, the CachedResource we are upgrading.
     * @param request, the Request
     * @param reply, the Reply
     */
    public void updateExpirationInfo(CachedResource cr, Request request, Reply reply) {
        if (!(cr instanceof EntityCachedResource)) return;
        EntityCachedResource ecr = (EntityCachedResource) cr;
        int age_value;
        long date_value;
        long request_time;
        long response_time;
        long now = System.currentTimeMillis();
        int apparent_age;
        int corrected_received_age;
        int response_delay;
        int corrected_initial_age;
        age_value = reply.getAge();
        date_value = reply.getDate();
        ecr.setDate(date_value);
        request_time = request.getEmitDate();
        response_time = now;
        if (date_value == -1) {
            date_value = now / 2 + request_time / 2;
        }
        apparent_age = (int) Math.max(0, (response_time - date_value) / 1000);
        corrected_received_age = Math.max(apparent_age, age_value);
        response_delay = (int) ((response_time - request_time) / 1000);
        corrected_initial_age = corrected_received_age + response_delay;
        ecr.setInitialAge(corrected_initial_age);
        ecr.setResponseTime(response_time);
        int freshness_lifetime = -1;
        int s_maxage = reply.getSMaxAge();
        if (filter.isShared() && (s_maxage != -1)) {
            freshness_lifetime = s_maxage;
            ecr.setRevalidate(true);
        } else {
            int maxage = reply.getMaxAge();
            if (maxage < 0) {
                long expires = reply.getExpires();
                if (expires > 0) {
                    freshness_lifetime = (int) Math.max(0, (expires - date_value) / 1000);
                    if ((freshness_lifetime > 31536000) && (reply.getMinorVersion() == 1) && (reply.getMajorVersion() == 1)) {
                        freshness_lifetime = 31536000;
                    }
                } else if (reply.hasHeader(reply.H_EXPIRES)) {
                    freshness_lifetime = 0;
                } else {
                    if (reply.hasHeader(reply.H_LAST_MODIFIED)) {
                        long last_mod = reply.getLastModified();
                        int difftime = (int) Math.max(0, (now - last_mod) / 1000);
                        freshness_lifetime = Math.min(86400, difftime / 10);
                    } else {
                        int cr_lifetime = ecr.getFreshnessLifetime();
                        if (cr_lifetime == -1) {
                            URL requrl = request.getURL();
                            if (requrl != null) {
                                String surl = requrl.toExternalForm();
                                if ((surl.indexOf('?') == -1) && (surl.indexOf("cgi") == -1)) {
                                    freshness_lifetime = 300;
                                } else {
                                    freshness_lifetime = 0;
                                }
                            }
                        } else {
                            freshness_lifetime = cr_lifetime;
                        }
                    }
                }
            } else {
                freshness_lifetime = maxage;
            }
        }
        if (freshness_lifetime != -1) {
            ecr.setFreshnessLifetime(freshness_lifetime);
        }
        if (reply.checkMustRevalidate()) {
            ecr.setRevalidate(true);
        } else if (filter.isShared() && reply.checkProxyRevalidate()) {
            ecr.setRevalidate(true);
        }
    }

    private void checkConsistency(CachedResource cr, Request request, Reply reply) {
        String cmd5 = cr.getContentMD5();
        String rmd5 = reply.getContentMD5();
        if (cmd5 == null) {
            if (rmd5 != null) {
                cr.setWillRevalidate(true);
            }
        } else {
            if ((rmd5 != null) && (!rmd5.equals(cmd5))) {
                cr.setWillRevalidate(true);
            }
        }
        int ccl = cr.getContentLength();
        int rcl = reply.getContentLength();
        if (rcl >= 0) {
            if (ccl != rcl) {
                cr.setWillRevalidate(true);
            }
        }
        HttpEntityTag rtag = reply.getETag();
        if (rtag != null) {
            String retag = rtag.toString();
            String cetag = cr.getETag();
            if ((cetag == null) || !retag.equals(cetag)) {
                cr.setWillRevalidate(true);
            }
        }
        long rlmt = reply.getLastModified();
        if (rlmt >= 0) {
            long clmt = cr.getLastModified();
            if (clmt != rlmt) {
                cr.setWillRevalidate(true);
            }
        }
    }

    /**
     * reset all the ages after a revalidation
     * @param cr, the CachedResource we are upgrading.
     * @param request, the Request
     * @param reply, the Reply
     */
    public void revalidateResource(CachedResource cr, Request request, Reply reply) {
        cr.setWillRevalidate(false);
        if (reply.getStatus() == HTTP.NOT_MODIFIED) {
            updateExpirationInfo(cr, request, reply);
        } else {
        }
        checkConsistency(cr, request, reply);
    }
}
