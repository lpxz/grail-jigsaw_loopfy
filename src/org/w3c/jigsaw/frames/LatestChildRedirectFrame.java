package org.w3c.jigsaw.frames;

import java.util.Enumeration;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.html.HtmlGenerator;
import org.w3c.tools.resources.ContainerResource;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.www.http.HTTP;

/**
 * do an automatic redirect to the most recently modified
 * container child
 */
public class LatestChildRedirectFrame extends HTTPFrame {

    /**
     * The default GET method for other king of associated resource
     * @param request The request to handle.
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    protected Reply getOtherResource(Request request) throws ProtocolException, ResourceException {
        long lmbest = 0;
        String best = null;
        ResourceReference rr = getResource().getParent();
        if (rr != null) {
            try {
                FramedResource p = (FramedResource) rr.lock();
                if (p instanceof ContainerResource) {
                    ContainerResource cr = (ContainerResource) p;
                    Enumeration res_enum;
                    res_enum = cr.enumerateResourceIdentifiers(false);
                    String childname;
                    ResourceReference childrr;
                    Class http_class = null;
                    ResourceReference framrr;
                    edu.hkust.clap.monitor.Monitor.loopBegin(747);
while (res_enum.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(747);
{
                        childname = (String) res_enum.nextElement();
                        childrr = cr.lookup(childname);
                        try {
                            FramedResource cp = (FramedResource) childrr.lock();
                            if (cp instanceof ContainerResource) {
                                long lm = cp.getLastModified();
                                if ((lmbest == 0) || (lm > lmbest)) {
                                    lmbest = lm;
                                    best = childname;
                                }
                            }
                        } catch (InvalidResourceException ex) {
                        } finally {
                            childrr.unlock();
                        }
                    }} 
edu.hkust.clap.monitor.Monitor.loopEnd(747);

                }
            } catch (InvalidResourceException ex) {
                throw new ResourceException("Invalid parent (latest):" + ex.getMessage());
            } finally {
                rr.unlock();
            }
            if (lmbest != 0) {
                Reply reply = request.makeReply(HTTP.FOUND);
                reply.setLocation(best + "/");
                HtmlGenerator g = new HtmlGenerator("Moved");
                g.append("<P>The freshest child is available from " + "<A HREF=\"" + best + "/\">" + best + "</A>");
                reply.setStream(g);
                return reply;
            }
        }
        Reply reply = request.makeReply(HTTP.FOUND);
        reply.setLocation("./");
        HtmlGenerator g = new HtmlGenerator("Moved");
        g.append("<P>No child available, see parent: " + "<A HREF=\"./\">parent</A>");
        reply.setStream(g);
        return reply;
    }
}
