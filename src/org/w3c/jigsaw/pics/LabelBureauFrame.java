package org.w3c.jigsaw.pics;

import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.www.http.HTTP;
import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.forms.URLDecoder;
import org.w3c.jigsaw.forms.URLDecoderException;
import org.w3c.jigsaw.frames.HTTPFrame;
import org.w3c.jigsaw.frames.PostableFrame;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class LabelBureauFrame extends PostableFrame {

    protected LabelBureauResource labelb = null;

    /**
     * Register our resource. Must be an instance of ServletWrapper.
     */
    public void registerResource(FramedResource resource) {
        super.registerOtherResource(resource);
        if (resource instanceof LabelBureauResource) labelb = (LabelBureauResource) resource;
    }

    /**
     * Parse the URLs given in the URLDecoder, as the <strong>u</strong> field.
     * This method just unquote any quoted URLs, before sending them back.
     * @param data The URLDecoder to get data from.
     * @return An array of String, properly parsed.
     */
    protected String[] parseURLs(String us[]) {
        if (us[0] == null) return null;
        String urls[] = new String[us.length];
        edu.hkust.clap.monitor.Monitor.loopBegin(974);
for (int i = 0; i < us.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(974);
{
            if (us[i].charAt(0) == '"') {
                int ulen = us[i].length();
                if (us[i].charAt(ulen - 1) == '"') urls[i] = us[i].substring(1, ulen - 1);
            } else {
                urls[i] = us[i];
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(974);

        return urls;
    }

    /**
     * Get the integer code for the String based PICS format.
     * @param request The request to be handled.
     * @param format The string representation of the format.
     * @return An LabelBureauInterface defined format code.
     * @exception HTTPException if processing the request failed.
     * @see org.w3c.jigsaw.pics.LabelBureauInterface
     */
    protected int parseFormat(Request request, String format) throws HTTPException {
        if (format == null) {
            return LabelBureauInterface.FMT_FULL;
        } else if (format.equals("minimal")) {
            return LabelBureauInterface.FMT_MINIMAL;
        } else if (format.equals("short")) {
            return LabelBureauInterface.FMT_SHORT;
        } else if (format.equals("full")) {
            return LabelBureauInterface.FMT_FULL;
        } else if (format.equals("signed")) {
            return LabelBureauInterface.FMT_SIGNED;
        }
        Reply error = request.makeReply(HTTP.BAD_REQUEST);
        error.setContent("Unknown label format: " + format);
        throw new HTTPException(error);
    }

    /**
     * Handle the request.
     * @param request The request to be handled.
     * @param data The URLDecoder
     * @return A Reply instance.
     * @exception ProtocolException if processing the request failed.
     * @see org.w3c.jigsaw.pics.LabelBureauInterface
     */
    public Reply handle(Request request, URLDecoder data) throws ProtocolException {
        if (labelb == null) {
            Reply reply = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
            reply.setContent("LabelBureauFrame not configured properly: " + "must be attached to a LabelBureauResource.");
            throw new HTTPException(reply);
        }
        String opt = data.getValue("opt");
        String urls[] = parseURLs(data.getMultipleValues("u"));
        int iformat = parseFormat(request, data.getValue("format"));
        String services[] = parseURLs(data.getMultipleValues("s"));
        if (services == null) services = labelb.getServices();
        if ((services == null) || (services.length == 0)) return labelb.makePICSErrorReply(request, "no-ratings \"unknown service\"");
        if (labelb.getDebugFlag()) {
            System.out.println("******** PICS REQUEST ********");
            System.out.println("opt      : " + opt);
            System.out.println("format   : " + data.getValue("format"));
            System.out.print("services : ");
            edu.hkust.clap.monitor.Monitor.loopBegin(975);
for (int i = 0; i < services.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(975);
System.out.println(services[i]);} 
edu.hkust.clap.monitor.Monitor.loopEnd(975);

            System.out.print("urls     : ");
            edu.hkust.clap.monitor.Monitor.loopBegin(976);
for (int i = 0; i < urls.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(976);
System.out.println(urls[i]);} 
edu.hkust.clap.monitor.Monitor.loopEnd(976);

            System.out.println("******************************");
        }
        if (opt.equals("generic")) {
            return labelb.getGenericLabels(request, iformat, urls, services, data);
        } else if (opt.equals("normal")) {
            return labelb.getNormalLabels(request, iformat, urls, services, data);
        } else if (opt.equals("tree")) {
            return labelb.getTreeLabels(request, iformat, urls, services, data);
        } else if (opt.equals("generic+tree")) {
            return labelb.getGenericTreeLabels(request, iformat, urls, services, data);
        } else {
            Reply error = request.makeReply(HTTP.BAD_REQUEST);
            error.setContent("Invalid label bureau query, bad option: " + opt);
            throw new HTTPException(error);
        }
    }
}
