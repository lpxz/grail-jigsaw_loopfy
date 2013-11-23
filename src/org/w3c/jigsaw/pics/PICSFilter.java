package org.w3c.jigsaw.pics;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.FileAttribute;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFilter;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpBag;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpRequestMessage;
import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.html.HtmlGenerator;

/** 
 * This package implements a PICS filter. The PICS filters allows server
 * administrator to rate the documents they deliver. The references for this
 * protocol is <a href="http://www.w3.org/hypertext/WWW/PICS/">here</a>.</p>
 * <p>The PICS filter defines the following attributes:</p>
 * <table border>
 * <caption>The list of parameters</caption>
 * <tr> 
 * <th align=left>Parameter name</th> 
 * <th align=left>Semantics</th>
 * <th align=left>Default value</th> 
 * <th align=left>Type</th>
 * </tr>
 * <tr> 
 * <th align=left>bureau</th> 
 * <th align=left>The label bureau to query for this entity labels.</th>
 * <th align=left><em>none</em></th> 
 * <th align=left>java.lang.String</th>
 * </tr>
 * </table>
 */
public class PICSFilter extends ResourceFilter {

    /**
     * Attribute index - The identifier of our bureau.
     */
    protected static int ATTR_BUREAU_IDENTIFIER = -1;

    static {
        Attribute a = null;
        Class cls = null;
        try {
            cls = Class.forName("org.w3c.jigsaw.pics.PICSFilter");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        a = new FileAttribute("bureau", null, Attribute.EDITABLE | Attribute.MANDATORY);
        ATTR_BUREAU_IDENTIFIER = AttributeRegistry.registerAttribute(cls, a);
    }

    /**
     * Our loaded lable bureau.
     */
    protected LabelBureauInterface bureau = null;

    /**
     * Get our label bureau identifier.
     */
    public File getBureauIdentifier() {
        return (File) getValue(ATTR_BUREAU_IDENTIFIER, null);
    }

    /**
     * Make sure our label bureau is loaded.
     */
    protected final void acquireBureau() {
        if (bureau != null) return;
        File file = getBureauIdentifier();
        if (file == null) return;
        bureau = LabelBureauFactory.getLabelBureau(file);
    }

    /**
     * Check the query to examine if it requires some PICS handling. 
     * If this is the case, it returns a <em>Bag</em> object
     * corresponding to the part of the <em>Accept-Protocol</em> header that
     * relates with PICS.
     * @param request The request to be checked.
     * @return A Bag object if PICS handling required, <string>null</strong>
     *         otherwise.
     * @exception HTTPException if processing the request failed.
     */
    protected HttpBag isPICSQuery(Request request) throws HTTPException {
        HttpBag requested = request.getProtocolRequest();
        if (requested == null) return null;
        if (!requested.hasBag(PICS.PICS_PROTOCOL_ID)) return null;
        HttpBag pics = null;
        try {
            pics = requested.getBag(PICS.PICS_PROTOCOL_ID);
        } catch (ClassCastException e) {
            return null;
        }
        return pics;
    }

    /**
     * The outgoingFilter method.
     * This method is the one that gets called by Jigsaw core. By default it
     * will call the simpler <code>outgoingFilter</code> method that takes
     * only the request and the reply as parameters.
     * @param request The request that has been processed.
     * @param reply The original reply as emitted by the resource.
     * @param filters The whole filter that applies to the resource.
     * @param i The current index of filters. The <em>i</em> filter is ourself,
     * filters with lower indexes have already been applied, and filters with
     * greater indexes are still to be applied.
     * @return A Reply instance, if that filter know how to complete the
     * request processing, or <strong>null</strong> if reminaing filters
     * are to be called by Jigsaw engine.
     * @exception HTTPException If processing should be interrupted,
     * because an abnormal situation occured.
     */
    public ReplyInterface outgoingFilter(RequestInterface req, ReplyInterface rep) throws HTTPException {
        Request request = (Request) req;
        Reply reply = (Reply) rep;
        HttpBag pics = isPICSQuery(request);
        if (pics == null) return reply;
        HttpBag params = pics.getBag("params");
        HttpBag services = params.getBag("services");
        URL url = request.getURL();
        int format = LabelBureauInterface.FMT_MINIMAL;
        if (params.hasItem("minimal")) {
            format = LabelBureauInterface.FMT_MINIMAL;
        } else if (params.hasItem("short")) {
            format = LabelBureauInterface.FMT_SHORT;
        } else if (params.hasItem("full")) {
            format = LabelBureauInterface.FMT_FULL;
        } else if (params.hasItem("signed")) {
            format = LabelBureauInterface.FMT_SIGNED;
        } else {
            Reply error = request.makeReply(HTTP.BAD_REQUEST);
            error.setContent("Invalid label format: " + format);
            throw new HTTPException(error);
        }
        StringBuffer sb = new StringBuffer(128);
        Enumeration e = services.keys();
        sb.append("(" + PICS.PICS_PROTOCOL_ID);
        edu.hkust.clap.monitor.Monitor.loopBegin(348);
        sloop: 
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(348);
{
            String n = (String) e.nextElement();
            LabelServiceInterface s = bureau.getLabelService(n);
            if (s == null) {
                sb.append(" error " + "(service-unavailable \"unknown service\")");
                continue sloop;
            }
            s.dump(sb, format);
            LabelInterface l = s.getSpecificLabel(url);
            if ((l == null) && ((l = s.getGenericLabel(url)) == null)) {
                sb.append(" error (not-labeled \"" + url + "\")");
            } else {
                sb.append(" labels ");
                l.dump(sb, format);
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(348);

        sb.append(")");
        reply.setProtocol(PICS.PICS_EXTENSION);
        reply.setValue("PICS-label", sb.toString());
        return reply;
    }

    public void initialize(Object values[]) {
        super.initialize(values);
        acquireBureau();
    }
}
