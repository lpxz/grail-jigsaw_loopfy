package org.w3c.jigsaw.status;

import java.util.Hashtable;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.www.http.HTTP;
import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.frames.HTTPFrame;
import org.w3c.jigsaw.html.HtmlGenerator;

/**
 * The server thread status.
 * This ought to be the client status, it will shortly (FIXME). It should uses
 * a two frame display, one listing the clients, and the other one listing per
 * client informations.
 * <p>By the Way, this uses the nasty refresh stuff from netscape. It should
 * perhaps be a servlet.
 */
public class ThreadStatFrame extends HTTPFrame {

    protected static Integer REFRESH_DEFAULT = new Integer(5);

    /**
     * Attribute index - Our refresh interval.
     */
    protected static int ATTR_REFRESH = -1;

    static {
        Attribute a = null;
        Class cls = null;
        try {
            cls = Class.forName("org.w3c.jigsaw.status.ThreadStatFrame");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        a = new IntegerAttribute("refresh", new Integer(5), Attribute.EDITABLE);
        ATTR_REFRESH = AttributeRegistry.registerAttribute(cls, a);
    }

    Runtime runtime = null;

    public void registerResource(FramedResource resource) {
        super.registerOtherResource(resource);
    }

    /**
     * Dump the currenr threads into an HTML page.
     * @param request The request we are to reply to.
     */
    public Reply listThreads(Request request) {
        int tcount = Thread.activeCount();
        HtmlGenerator g = new HtmlGenerator("Thread status");
        g.meta("Refresh", getValue(ATTR_REFRESH, REFRESH_DEFAULT).toString());
        addStyleSheet(g);
        Thread tarray[] = new Thread[tcount];
        Thread.enumerate(tarray);
        g.append("<h1>Thread dump</h1>");
        g.append("<ul>");
        String rname = getResource().getIdentifier();
        edu.hkust.clap.monitor.Monitor.loopBegin(552);
for (int i = 0; i < tcount; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(552);
{
            String name = tarray[i].getName();
            g.append("<li>" + "<a href=\"" + rname + "?" + name + "\">" + name + "</a>" + (tarray[i].isDaemon() ? "D " : "  ") + (tarray[i].isAlive() ? "R " : "  ") + " Prio: " + (tarray[i].getPriority()));
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(552);

        g.append("</ul>");
        g.append("<p>To kill a thread, add a <b>?</b><em>thread-name</em> " + "to the current URL.</p>");
        g.append("<h2>Misc informations</h2>");
        g.append("<p>Total free memory: " + runtime.freeMemory());
        g.append("<p>Toal memory      : " + runtime.totalMemory());
        g.close();
        Reply reply = request.makeReply(HTTP.OK);
        reply.setStream(g);
        return reply;
    }

    protected Reply dumpThread(Request request) throws HTTPException {
        int tcount = Thread.activeCount();
        Thread tarray[] = new Thread[tcount];
        Thread.enumerate(tarray);
        String tname = request.getQueryString();
        Thread queried = null;
        edu.hkust.clap.monitor.Monitor.loopBegin(553);
for (int i = 0; i < tcount; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(553);
{
            if (tarray[i].getName().equals(tname)) {
                queried = tarray[i];
                break;
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(553);

        if (queried == null) {
            Reply reply = request.makeReply(HTTP.NOT_FOUND);
            reply.setContent("Thread " + tname + " not found.");
            return reply;
        }
        queried.destroy();
        return listThreads(request);
    }

    /**
     * Get the threads.
     * If a search string is present, kill the indicated thread, otherwise
     * list the currently running threads.
     * @param request The request to handle.
     * @exception HTTPException If processing the request failed.
     */
    public Reply get(Request request) throws HTTPException {
        if (request.hasQueryString()) {
            return dumpThread(request);
        } else {
            return listThreads(request);
        }
    }

    /**
     * Initialize the thread lister.
     * Just get a pointer to our runtime object.
     * @param values The default attribute values.
     */
    public void initialize(Object values[]) {
        super.initialize(values);
        runtime = Runtime.getRuntime();
    }
}
