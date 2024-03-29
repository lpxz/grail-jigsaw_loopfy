package org.w3c.jigsaw.tutorials;

import java.util.Date;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.DirectoryResource;
import org.w3c.tools.resources.FileResource;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.StringAttribute;
import org.w3c.jigsaw.frames.HTTPFrame;
import org.w3c.jigsaw.html.HtmlGenerator;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.www.http.HTTP;

/**
 * @version $Revision: 1.2 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class FancyFrame extends HTTPFrame {

    /**
     * Attribute index - Message to display
     */
    protected static int ATTR_MESSAGE = -1;

    static {
        Attribute a = null;
        Class cls = null;
        try {
            cls = Class.forName("org.w3c.jigsaw.tutorials.FancyFrame");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        a = new StringAttribute("message", "Hello", Attribute.EDITABLE);
        ATTR_MESSAGE = AttributeRegistry.registerAttribute(cls, a);
    }

    /**
     * Get the message.
     * @return A String instance.
     */
    public String getMessage() {
        return getString(ATTR_MESSAGE, null);
    }

    /**
     * Display the Frame message and some attributes of our
     * associated FileResource. This method is called only if
     * our associated resource *is* a FileResource.
     * @param request The request to handle.
     * @return A Reply instance.
     * @exception ProtocolException if processing the request failed
     * @exception ResourceException if an internal error occurs
     */
    protected Reply getFileResource(Request request) throws ProtocolException, ResourceException {
        FileResource fres = getFileResource();
        HtmlGenerator g = new HtmlGenerator("FancyFrame");
        g.append("<h1>FancyFrame output</h1>");
        g.append("<p>", getMessage(), "</p>");
        g.append("<h2> FileResource associated : </h2>");
        g.append("<ul><li>Identifier : ", fres.getIdentifier());
        g.append("<li>File : " + fres.getFile());
        g.append("<li>Last Modified Time : ", new Date(fres.getLastModified()).toString(), "</ul>");
        Reply reply = createDefaultReply(request, HTTP.OK);
        reply.setStream(g);
        return reply;
    }

    /**
     * Display the Frame message and some attributes of our
     * associated DirectoryResource. This method is called only if
     * our associated resource *is* a DirectoryResource.
     * @param request The request to handle.
     * @return A Reply instance.
     * @exception ProtocolException if processing the request failed
     * @exception ResourceException if an internal error occurs
     */
    protected Reply getDirectoryResource(Request request) throws ProtocolException, ResourceException {
        DirectoryResource dres = getDirectoryResource();
        HtmlGenerator g = new HtmlGenerator("FancyFrame");
        g.append("<h1>FancyFrame output</h1>");
        g.append("<p>", getMessage(), "</p>");
        g.append("<h2> DirectoryResource associated : </h2>");
        g.append("<ul><li>Identifier : ", dres.getIdentifier());
        g.append("<li>Directory : " + dres.getDirectory());
        g.append("<li>Last Modified Time : ", new Date(dres.getLastModified()).toString(), "</ul>");
        Reply reply = createDefaultReply(request, HTTP.OK);
        reply.setStream(g);
        return reply;
    }

    /**
     * Display the Frame message and some attributes of our
     * associated Resource. This method is called if the associated
     * resource has been registered with <strong>registerOtherResource</strong>
     * or if it's not a usual resource (FileResource, DirectoryResource)
     * @param request The request to handle.
     * @return A Reply instance.
     * @exception ProtocolException if processing the request failed
     * @exception ResourceException if an internal error occurs
     */
    protected Reply getOtherResource(Request request) throws ProtocolException, ResourceException {
        FramedResource res = getResource();
        HtmlGenerator g = new HtmlGenerator("FancyFrame");
        g.append("<h1>FancyFrame output</h1>");
        g.append("<p>", getMessage(), "</p>");
        g.append("<h2> Resource associated : </h2>");
        g.append("<ul><li>Identifier : ", res.getIdentifier());
        g.append("<li>Last Modified Time : ", new Date(res.getLastModified()).toString(), "</ul>");
        Reply reply = createDefaultReply(request, HTTP.OK);
        reply.setStream(g);
        return reply;
    }
}
