package org.w3c.www.protocol.http.cache.push;

import org.w3c.www.protocol.http.Request;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.w3c.www.protocol.http.cache.CachedResource;
import org.w3c.www.protocol.http.cache.EntityCachedResource;

/**
 * PushEntityCachedResource
 * EntityCachedResource that reads data from the file rather than attempting
 * to use ActiveStream to tee output to a client that is not there.
 *
 * @author Paul Henshaw, The Fantastic Corporation, Paul.Henshaw@fantastic.com
 * @version $Revision: 1.1 $
 * $Id: PushEntityCachedResource.java,v 1.1 2010/06/15 12:25:45 smhuang Exp $
 */
public class PushEntityCachedResource extends EntityCachedResource {

    /**
     * This constructor required to handle startup when cache already
     * contains PushEntityCachedResources
     */
    public PushEntityCachedResource() {
        super();
    }

    /**
     * Construct a PushEntityCachedResource 
     *
     * Used by the PushCacheManager to actually store a PUSH resource.
     * Note PushEntityCachedResource are used only when saving 
     * resources.  When extracting resources from the cache, 
     * EntityCachedResources are used.
     * 
     * @param filter  the PushCacheFilter that in fact has not done 
     *                anything yet, but which knows how to handle a 
     *                PUSHed resource
     * @param req     the forged request for a URL
     * @param rep     the forged reply for the URL
     */
    protected PushEntityCachedResource(PushCacheFilter filter, Request req, PushReply rep) {
        try {
            invalidated = false;
            setValue(ATTR_IDENTIFIER, req.getURL().toExternalForm());
            this.filter = filter;
            updateInfo(req, rep);
            filter.getValidator().updateExpirationInfo(this, req, rep);
            java.io.File outfile = filter.getPushCacheStore().getNewEntryFile();
            FileOutputStream os = new FileOutputStream(outfile);
            FileInputStream is = rep.getStream();
            byte[] buffer = new byte[4096];
            int wrote = 0;
            int wantedsize = rep.getContentLength();
            int thistime;
            edu.hkust.clap.monitor.Monitor.loopBegin(273);
while (wrote < wantedsize) { 
edu.hkust.clap.monitor.Monitor.loopInc(273);
{
                thistime = Math.min(is.available(), 4096);
                is.read(buffer, 0, thistime);
                os.write(buffer, 0, thistime);
                wrote += thistime;
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(273);

            os.close();
            setFile(outfile);
            setLoadState(STATE_LOAD_COMPLETE);
            setCurrentLength(wrote);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
