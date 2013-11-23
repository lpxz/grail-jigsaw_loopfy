package org.w3c.jigedit.tools;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.TimeZone;
import java.util.Vector;
import org.w3c.tools.resources.AbstractContainer;
import org.w3c.tools.resources.DirectoryResource;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.sorter.Sorter;
import org.w3c.jigsaw.forms.URLDecoder;
import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.html.HtmlGenerator;
import org.w3c.jigsaw.frames.HTTPFrame;
import org.w3c.jigsaw.frames.PostableFrame;
import org.w3c.jigedit.cvs2.CvsFrame;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpRequestMessage;
import org.w3c.tools.resources.event.StructureChangedEvent;
import org.w3c.tools.resources.event.StructureChangedListener;

/**
 * Emit the content of its parent directory.
 */
public class ToolsListerFrame extends PostableFrame implements StructureChangedListener {

    private static final boolean debug = true;

    private boolean invalid = true;

    private ResourceReference dirResourceRef = null;

    protected ResourceReference getDirResourceRef() {
        if (invalid || (dirResourceRef == null)) {
            dirResourceRef = getResource().getParent();
        }
        return dirResourceRef;
    }

    public void registerResource(FramedResource resource) {
        super.registerOtherResource(resource);
        dirResourceRef = resource.getParent();
        try {
            FramedResource fres = (FramedResource) dirResourceRef.lock();
            fres.addStructureChangedListener(this);
        } catch (InvalidResourceException ex) {
            ex.printStackTrace();
        } finally {
            dirResourceRef.unlock();
        }
        invalid = false;
    }

    /**
     * Unused here.
     */
    public void resourceModified(StructureChangedEvent evt) {
    }

    /**
     * Unused here.
     */
    public void resourceCreated(StructureChangedEvent evt) {
    }

    public void resourceUnloaded(StructureChangedEvent evt) {
    }

    /**
     * A resource is about to be removed
     * This handles the <code>RESOURCE_REMOVED</code> kind of events.
     * @param evt The event describing the change.
     */
    public void resourceRemoved(StructureChangedEvent evt) {
        invalid = true;
    }

    protected Class httpClass = null;

    private String getResourceLine(ResourceReference rr, String name, boolean even) {
        if (httpClass == null) {
            try {
                httpClass = Class.forName("org.w3c.jigsaw.frames.HTTPFrame");
            } catch (ClassNotFoundException ex) {
                httpClass = null;
            }
        }
        SimpleDateFormat df;
        df = new SimpleDateFormat("yyyy MMM dd - HH:mm:ss zzz");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        StringBuffer buffer = new StringBuffer(100);
        try {
            FramedResource resource = (FramedResource) rr.lock();
            if (name == null) name = resource.getIdentifier();
            if (even) {
                buffer.append("<tr class=\"evenlist\" align=\"left\" " + "valign=\"bottom\">");
            } else {
                buffer.append("<tr class=\"oddlist\" align=\"left\" " + "valign=\"bottom\">");
            }
            HTTPFrame itsframe = null;
            if (httpClass != null) itsframe = (HTTPFrame) resource.getFrame(httpClass);
            if (itsframe instanceof CvsFrame) {
                buffer.append("<td></td>");
            } else {
                buffer.append("<td>");
                buffer.append("<INPUT TYPE=\"CHECKBOX\" NAME=\"" + name + "\"> ");
                buffer.append("</td>");
            }
            buffer.append("<td>");
            if (itsframe != null) {
                String icon = itsframe.getIcon();
                if (icon != null) buffer.append("<IMG SRC=\"" + getIconDirectory() + "/" + icon + "\">");
                buffer.append("<A HREF=\"" + resource.getURLPath() + "\">" + name + "</A>");
                String title = itsframe.getTitle();
                if (title != null) {
                    buffer.append("</td><td>" + title);
                } else {
                    buffer.append("</td><td>");
                }
                int clength = itsframe.getContentLength();
                if (clength != -1) {
                    int kcl = clength / 1024;
                    buffer.append("</td><td>[ " + kcl + " kB ]");
                } else {
                    buffer.append("</td><td>-");
                }
                long clm = itsframe.getLastModified();
                if (clm != -1) {
                    buffer.append("</td><td>" + df.format(new Date(clm)));
                } else {
                    buffer.append("</td><td>-");
                }
            } else {
                buffer.append("<A HREF=\"" + resource.getURLPath() + "\">" + name + "</A>" + " Not available via HTTP");
                buffer.append("</td><td></td><td></td><td>\n");
            }
            buffer.append("</td></tr>\n");
        } catch (InvalidResourceException ex) {
            buffer.append("<td> " + name);
            buffer.append("cannot be loaded (server misconfigured)");
            buffer.append("<BR>");
            buffer.append("</td></tr>\n");
        } finally {
            rr.unlock();
        }
        return buffer.toString();
    }

    /**
     * Get the directory listing.
     * @param request the incomming request.
     * @exception ProtocolException if a protocol error occurs
     * @exception ResourceException if a server error occurs
     */
    public synchronized Reply getDirectoryListing(Request request) throws ProtocolException, ResourceException {
        DirectoryResource dirResource = null;
        try {
            dirResource = (DirectoryResource) getDirResourceRef().lock();
            if (dirResource == null) throw new ResourceException("parent is NOT a " + "DirectoryResource. (" + resource.getIdentifier() + ")");
            if (!dirResource.verify()) {
                Reply error = request.makeReply(HTTP.NOT_FOUND);
                error.setContent("<h1>Document not found</h1>" + "<p>The document " + request.getURL() + " is indexed but not available." + "<p>The server is misconfigured.");
                throw new HTTPException(error);
            }
            if ((listing == null) || (dirResource.getDirectory().lastModified() > listing_stamp) || (dirResource.getLastModified() > listing_stamp) || (getLastModified() > listing_stamp)) {
                Enumeration e = dirResource.enumerateResourceIdentifiers();
                Vector resources = Sorter.sortStringEnumeration(e);
                HtmlGenerator g = new HtmlGenerator("Directory listing of " + dirResource.getIdentifier());
                addStyleSheet(g);
                g.append("<h1>Directory listing of ", dirResource.getIdentifier(), "</h1>");
                if (dirResource.getParent() != null) g.append("<p><a href=\"..\">Parent</a><br>");
                g.append("\n<form method=\"POST\" action=\"" + request.getURL() + "\">\n");
                String listername = getResource().getIdentifier();
                g.append("<table border=\"0\">\n");
                ResourceReference rr = null;
                FramedResource resource = null;
                String name = null;
                rr = dirResource.lookup("CVS");
                if (rr != null) {
                    g.append(getResourceLine(rr, "CVS", false));
                }
                boolean even = true;
                edu.hkust.clap.monitor.Monitor.loopBegin(849);
for (int i = 0; i < resources.size(); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(849);
{
                    name = (String) resources.elementAt(i);
                    if (name.equals(listername) || name.equals("CVS")) continue;
                    rr = dirResource.lookup(name);
                    g.append(getResourceLine(rr, name, even));
                    even = !even;
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(849);

                g.append("</table>\n");
                g.append("<P><INPUT TYPE=\"SUBMIT\" NAME=\"SUBMIT\" VALUE=\"" + "Delete file from  publishing space\"></FORM>\n");
                g.close();
                listing_stamp = getLastModified();
                listing = g;
            } else if (checkIfModifiedSince(request) == COND_FAILED) {
                return createDefaultReply(request, HTTP.NOT_MODIFIED);
            }
        } catch (InvalidResourceException ex) {
            return createDefaultReply(request, HTTP.INTERNAL_SERVER_ERROR);
        } finally {
            getDirResourceRef().unlock();
        }
        Reply reply = createDefaultReply(request, HTTP.OK);
        reply.setLastModified(listing_stamp);
        reply.setStream(listing);
        return reply;
    }

    /**
     * @exception org.w3c.tools.resources.ProtocolException 
     * if a protocol error occurs
     * @exception org.w3c.tools.resources.ResourceException 
     * if a server error occurs
     */
    protected Reply getOtherResource(Request request) throws ProtocolException, ResourceException {
        return getDirectoryListing(request);
    }

    /**
     * Handle the form submission, after posted data parsing.
     * <p>This method ought to be abstract, but for reasonable reason, it
     * will just dump (parsed) the form content back to the client, so that it
     * can be used for debugging.
     * @param request The request proper.
     * @param data The parsed data content.
     * @exception ProtocolException If form data processing failed.
     * @see org.w3c.jigsaw.forms.URLDecoder
     */
    public Reply handle(Request request, URLDecoder data) throws ProtocolException {
        Reply r;
        Enumeration e = data.keys();
        edu.hkust.clap.monitor.Monitor.loopBegin(850);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(850);
{
            String name = (String) e.nextElement();
            if (name.equals("SUBMIT")) continue;
            synchronized (this) {
                DirectoryResource dr;
                Resource toDeleteRes;
                ResourceReference rr;
                File dir, toDeleteFile;
                try {
                    dr = (DirectoryResource) getDirResourceRef().lock();
                    dir = dr.getDirectory();
                    if (debug) System.out.println("Deleting " + name);
                    rr = dr.lookup(name);
                    if (rr != null) {
                        try {
                            toDeleteFile = new File(dir, name);
                            toDeleteFile.delete();
                        } catch (Exception ex) {
                        }
                        try {
                            toDeleteRes = (Resource) rr.lock();
                            toDeleteRes.delete();
                        } catch (Exception ex) {
                        } finally {
                            rr.unlock();
                        }
                    }
                } catch (Exception ex) {
                } finally {
                    getDirResourceRef().unlock();
                }
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(850);

        try {
            r = getDirectoryListing(request);
        } catch (ResourceException ex) {
            r = createDefaultReply(request, HTTP.INTERNAL_SERVER_ERROR);
        }
        return r;
    }
}
