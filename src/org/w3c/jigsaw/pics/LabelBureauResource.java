package org.w3c.jigsaw.pics;

import java.io.File;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.FileAttribute;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.StringArrayAttribute;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpMessage;
import org.w3c.jigsaw.forms.URLDecoder;
import org.w3c.jigsaw.forms.URLDecoderException;
import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

/**
 * An HTTP interface to query a Label bureau.
 * This conforms to the
 * <a href="http://www.w3.org/hypertext/WWW/PICS/labels.html">PICS 
 * protocol specification</a>.
 * <p>This entity defines the following parameter:</p>
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
 * <th align=left>Name of the LabelBureau database</th>
 * <th align=left><em>none</em></th> 
 * <th align=left>java.lang.String</th>
 * </tr>
 * </table>
 * <p>This entity also inherits the parameters from the PostableResource.</p>
 */
public class LabelBureauResource extends FramedResource {

    /**
     * Attribute index - Our label bureau identifier.
     */
    protected static int ATTR_BUREAU_IDENTIFIER = -1;

    /**
     * Attribute index - Our label bureau identifier.
     */
    protected static int ATTR_BUREAU_SERVICES = -1;

    /**
     * Attribute index - debug flag.
     */
    protected static int ATTR_BUREAU_DEBUG = -1;

    static {
        Attribute a = null;
        Class cls = null;
        try {
            cls = Class.forName("org.w3c.jigsaw.pics.LabelBureauResource");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        a = new FileAttribute("bureau", null, Attribute.EDITABLE | Attribute.MANDATORY);
        ATTR_BUREAU_IDENTIFIER = AttributeRegistry.registerAttribute(cls, a);
        a = new StringArrayAttribute("services", null, Attribute.EDITABLE);
        ATTR_BUREAU_SERVICES = AttributeRegistry.registerAttribute(cls, a);
        a = new BooleanAttribute("debug", Boolean.FALSE, Attribute.EDITABLE);
        ATTR_BUREAU_DEBUG = AttributeRegistry.registerAttribute(cls, a);
    }

    /**
     * Our loaded label bureau:
     */
    protected LabelBureauInterface bureau = null;

    /**
     * Get our label bureau identifier.
     */
    public File getBureauIdentifier() {
        return (File) getValue(ATTR_BUREAU_IDENTIFIER, null);
    }

    /**
     * Get the service list
     */
    public String[] getServices() {
        return (String[]) getValue(ATTR_BUREAU_SERVICES, null);
    }

    /**
     *
     */
    public boolean getDebugFlag() {
        return getBoolean(ATTR_BUREAU_DEBUG, false);
    }

    /**
     * Check that we have loaded our bureau, or load it.
     */
    protected final void acquireBureau() {
        if (bureau != null) return;
        File repository = getBureauIdentifier();
        if (repository != null) bureau = LabelBureauFactory.getLabelBureau(getBureauIdentifier());
        return;
    }

    /**
     * Build a PICS reply out of the given content.
     * @param request The request to reply to.
     * @param sb The StringBuffer containing the content to send back.
     */
    protected Reply makePICSReply(Request request, StringBuffer buffer) {
        String content = buffer.toString();
        Reply reply = request.makeReply(HTTP.OK);
        reply.setHeaderValue(reply.H_CONTENT_TYPE, PICS.APPLICATION_PICSLABEL);
        reply.setContent(content);
        if (getDebugFlag()) {
            System.out.println("********* PICS REPLY *********");
            System.out.println(content);
            System.out.println("******************************");
        }
        return reply;
    }

    protected Reply makePICSErrorReply(Request request, String msg) {
        Reply reply = request.makeReply(HTTP.OK);
        reply.setHeaderValue(reply.H_CONTENT_TYPE, PICS.APPLICATION_PICSLABEL);
        String content = "(" + PICS.PICS_PROTOCOL_ID + " error " + "(" + msg + "))";
        reply.setContent(content);
        if (getDebugFlag()) {
            System.out.println("********* PICS REPLY *********");
            System.out.println(content);
            System.out.println("******************************");
        }
        return reply;
    }

    /**
     * Get the generic labels for a set of service and a set of urls.
     * @param request the incomminmg request
     * @param format the pics format
     * @param urls the urls to label
     * @param services the services to ask
     * @param data the URLDecoder
     * @return A Reply instance
     * @exception HTTPException if processing the request failed.
     */
    protected Reply getGenericLabels(Request request, int format, String urls[], String services[], URLDecoder data) throws HTTPException {
        StringBuffer sb = new StringBuffer(128);
        sb.append("(" + PICS.PICS_PROTOCOL_ID);
        edu.hkust.clap.monitor.Monitor.loopBegin(569);
        sloop:
for (int is = 0; is < services.length; is++) { 
edu.hkust.clap.monitor.Monitor.loopInc(569);
{
            LabelServiceInterface s = bureau.getLabelService(services[is]);
            if (s == null) {
                sb.append(" error " + "(no-ratings \"unknown service\")");
                continue sloop;
            }
            s.dump(sb, format);
            sb.append(" labels ");
            try {
            	edu.hkust.clap.monitor.Monitor.loopBegin(570);
                uloop: 
for (int iu = 0; iu < urls.length; iu++) { 
edu.hkust.clap.monitor.Monitor.loopInc(570);
{
                    LabelInterface l = s.getGenericLabel(new URL(urls[iu]));
                    if (l == null) {
                        sb.append(" error " + "(not-labeled \"" + urls[iu] + "\")");
                        continue uloop;
                    }
                    if (iu != 0) sb.append(" ");
                    l.dump(sb, format);
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(570);

            } catch (MalformedURLException e) {
                Reply error = request.makeReply(HTTP.BAD_REQUEST);
                error.setContent("You are requesting an invalid URL,");
                throw new HTTPException(error);
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(569);

        sb.append(")");
        return makePICSReply(request, sb);
    }

    /**
     * Get the normal (generic or specific) labels.
     * @param request the incomminmg request
     * @param format the pics format
     * @param urls the urls to label
     * @param services the services to ask
     * @param data the URLDecoder
     * @return A Reply instance
     * @exception HTTPException if processing the request failed.
     */
    protected Reply getNormalLabels(Request request, int format, String urls[], String services[], URLDecoder data) throws HTTPException {
        StringBuffer sb = new StringBuffer(128);
        sb.append("(" + PICS.PICS_PROTOCOL_ID);
        edu.hkust.clap.monitor.Monitor.loopBegin(571);
        sloop: 
for (int is = 0; is < services.length; is++) { 
edu.hkust.clap.monitor.Monitor.loopInc(571);
{
            LabelServiceInterface s = bureau.getLabelService(services[is]);
            if (s == null) {
                sb.append(" error " + "(no-ratings \"unknown service\")");
                continue sloop;
            }
            s.dump(sb, format);
            sb.append(" labels ");
            try {
            	edu.hkust.clap.monitor.Monitor.loopBegin(572);
                uloop: 
for (int iu = 0; iu < urls.length; iu++) { 
edu.hkust.clap.monitor.Monitor.loopInc(572);
{
                    URL u = new URL(urls[iu]);
                    LabelInterface l = s.getSpecificLabel(u);
                    if ((l == null) && ((l = s.getGenericLabel(u)) == null)) {
                        sb.append(" error " + "(not-labeled \"" + urls[iu] + "\")");
                        continue uloop;
                    }
                    if (iu != 0) sb.append(" ");
                    l.dump(sb, format);
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(572);

            } catch (MalformedURLException e) {
                Reply error = request.makeReply(HTTP.BAD_REQUEST);
                error.setContent("You are requesting an invalid URL.");
                throw new HTTPException(error);
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(571);

        sb.append(")");
        return makePICSReply(request, sb);
    }

    /**
     * Get tree labels.
     * @param request the incomminmg request
     * @param format the pics format
     * @param urls the urls to label
     * @param services the services to ask
     * @param data the URLDecoder
     * @return A Reply instance
     * @exception HTTPException if processing the request failed.
     */
    protected Reply getTreeLabels(Request request, int format, String urls[], String services[], URLDecoder data) throws HTTPException {
        StringBuffer sb = new StringBuffer(128);
        sb.append("(" + PICS.PICS_PROTOCOL_ID);
        edu.hkust.clap.monitor.Monitor.loopBegin(573);
        sloop: 
for (int is = 0; is < services.length; is++) { 
edu.hkust.clap.monitor.Monitor.loopInc(573);
{
            LabelServiceInterface s = bureau.getLabelService(services[is]);
            if (s == null) {
                sb.append(" error " + "(no-ratings \"unknown service\")");
                continue sloop;
            }
            s.dump(sb, format);
            sb.append(" labels ");
            try {
            	edu.hkust.clap.monitor.Monitor.loopBegin(574);
                uloop: 
for (int iu = 0; iu < urls.length; iu++) { 
edu.hkust.clap.monitor.Monitor.loopInc(574);
{
                    boolean spaced = false;
                    LabelInterface l[] = s.getTreeLabels(new URL(urls[iu]));
                    if (l == null) {
                        sb.append(" error " + "(not-labeled \"" + urls[iu] + "\")");
                        continue uloop;
                    }
                    sb.append((iu == 0) ? "(" : " (");
                    edu.hkust.clap.monitor.Monitor.loopBegin(575);
for (int il = 0; il < l.length; il++) { 
edu.hkust.clap.monitor.Monitor.loopInc(575);
{
                        if (il != 0) sb.append(" ");
                        l[il].dump(sb, format);
                    }} 
edu.hkust.clap.monitor.Monitor.loopEnd(575);

                    sb.append(")");
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(574);

            } catch (MalformedURLException e) {
                Reply error = request.makeReply(HTTP.BAD_REQUEST);
                error.setContent("You are requesting an invalid URL.");
                throw new HTTPException(error);
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(573);

        sb.append(")");
        return makePICSReply(request, sb);
    }

    /**
     * Get generic tree labels.
     * @param request the incomminmg request
     * @param format the pics format
     * @param urls the urls to label
     * @param services the services to ask
     * @param data the URLDecoder
     * @return A Reply instance
     * @exception HTTPException if processing the request failed.
     */
    protected Reply getGenericTreeLabels(Request request, int format, String urls[], String services[], URLDecoder data) throws HTTPException {
        StringBuffer sb = new StringBuffer(128);
        sb.append("(" + PICS.PICS_PROTOCOL_ID);
        edu.hkust.clap.monitor.Monitor.loopBegin(576);
        sloop:
for (int is = 0; is < services.length; is++) { 
edu.hkust.clap.monitor.Monitor.loopInc(576);
{
            LabelServiceInterface s = bureau.getLabelService(services[is]);
            if (s == null) {
                sb.append(" error " + "(no-ratings \"unknown service\")");
                continue sloop;
            }
            s.dump(sb, format);
            sb.append(" labels ");
            try {
            	edu.hkust.clap.monitor.Monitor.loopBegin(577);
                uloop: 
for (int iu = 0; iu < urls.length; iu++) { 
edu.hkust.clap.monitor.Monitor.loopInc(577);
{
                    URL u = new URL(urls[iu]);
                    LabelInterface l[] = s.getGenericTreeLabels(u);
                    if (l == null) {
                        sb.append(" error " + "(not-labeled \"" + urls[iu] + "\")");
                        continue uloop;
                    }
                    sb.append((iu == 0) ? "(" : " (");
                    edu.hkust.clap.monitor.Monitor.loopBegin(575);
for (int il = 0; il < l.length; il++) { 
edu.hkust.clap.monitor.Monitor.loopInc(575);
{
                        if (il != 0) sb.append(" ");
                        l[il].dump(sb, format);
                    }} 
edu.hkust.clap.monitor.Monitor.loopEnd(575);

                    sb.append(")");
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(577);

            } catch (MalformedURLException e) {
                Reply error = request.makeReply(HTTP.BAD_REQUEST);
                error.setContent("You are requesting an invalid URL.");
                throw new HTTPException(error);
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(576);

        sb.append(")");
        return makePICSReply(request, sb);
    }

    public void setValue(int idx, Object value) {
        super.setValue(idx, value);
        if (idx == ATTR_BUREAU_DEBUG) {
            PICS.setDebug(getDebugFlag());
        }
    }

    public void initialize(Object values[]) {
        super.initialize(values);
        acquireBureau();
        try {
            registerFrameIfNone("org.w3c.jigsaw.pics.LabelBureauFrame", "label-bureau-frame");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        PICS.setDebug(getDebugFlag());
    }
}
