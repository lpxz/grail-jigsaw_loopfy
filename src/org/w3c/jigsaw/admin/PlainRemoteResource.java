package org.w3c.jigsaw.admin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.SimpleAttribute;
import org.w3c.tools.resources.ArrayAttribute;
import org.w3c.tools.resources.serialization.AttributeDescription;
import org.w3c.tools.resources.serialization.EmptyDescription;
import org.w3c.tools.resources.serialization.ResourceDescription;
import org.w3c.www.protocol.http.HttpManager;
import org.w3c.www.protocol.http.Reply;
import org.w3c.www.protocol.http.Request;

public class PlainRemoteResource implements RemoteResource {

    private static final boolean debug = false;

    /**
     * The client side admin context
     */
    protected AdminContext admin = null;

    /**
     * The remote resource set of attributes.
     */
    protected AttributeDescription attributes[];

    /**
     * The remote resource attribute values.
     */
    protected Object values[] = null;

    /**
     * Is that resource a container resource ?
     */
    protected boolean iscontainer = false;

    /**
     * Is that resource a indexers catalog ?
     */
    protected boolean isindexerscatalog = false;

    /**
     * Is that resource a directory resource ?
     */
    protected boolean isDirectoryResource = false;

    /**
     * Is that resource a framed resource ?
     */
    protected boolean isframed = false;

    /**
     * The name of that resource (ie it's identifier attribute).
     */
    protected String identifier = null;

    /**
     * The name of the parent of that resource, as an URL.
     */
    protected URL parent = null;

    /**
     * The admin URL for the wrapped resource.
     */
    protected URL url = null;

    /**
     * Set of attached frames.
     */
    protected RemoteResource frames[] = null;

    /**
     * Our description
     */
    protected ResourceDescription description = null;

    protected Request createRequest() {
        Request request = admin.http.createRequest();
        request.setURL(url);
        if (!debug) {
            request.setValue("TE", "gzip");
        }
        return request;
    }

    protected InputStream getInputStream(Reply reply) throws IOException {
        if (reply.hasTransferEncoding("gzip")) return new GZIPInputStream(reply.getInputStream()); else return reply.getInputStream();
    }

    protected void setFrames(RemoteResource frames[]) {
        this.isframed = true;
        this.frames = frames;
    }

    /**
     * Get the target resource class hierarchy.
     * This method will return the class hierarchy as an array of String. The
     * first string in the array is the name of the resource class itself, the
     * last string will always be <em>java.lang.Object</em>.
     * @return A String array givimg the target resource's class description.
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public String[] getClassHierarchy() throws RemoteAccessException {
        return description.getClassHierarchy();
    }

    /**
     * Reindex the resource's children if this resource is a DirectoryResource.
     * @exception RemoteAccessException If it's not a DirectoryResource
     */
    public void reindex(boolean rec) throws RemoteAccessException {
        if (isDirectoryResource()) {
            try {
                Request req = createRequest();
                if (rec) {
                    req.setMethod("REINDEX-RESOURCE");
                } else {
                    req.setMethod("REINDEX-LOCALLY");
                }
                Reply rep = admin.runRequest(req);
            } catch (RemoteAccessException rae) {
                throw rae;
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RemoteAccessException(ex.getMessage());
            }
        } else {
            throw new RemoteAccessException("Error, can't reindex! This is " + "not a DirectoryResource.");
        }
    }

    /**
     * Delete that resource, and detach it from its container.
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public void delete() throws RemoteAccessException {
        try {
            Request req = createRequest();
            req.setMethod("DELETE-RESOURCE");
            Reply rep = admin.runRequest(req);
        } catch (RemoteAccessException rae) {
            throw rae;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RemoteAccessException(ex.getMessage());
        }
    }

    /**
     * Get the target resource list of attributes.
     * This method returns the target resource attributes description. The
     * resulting array contains instances of the Attribute class, one item
     * per described attributes.
     * <p>Even though this returns all the attribute resources, only the
     * ones that are advertized as being editable can be set through this
     * interface.
     * @return An array of Attribute.
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public synchronized AttributeDescription[] getAttributes() throws RemoteAccessException {
        return description.getAttributeDescriptions();
    }

    /**
     * @param name The attribute whose value is to be fetched, encoded as
     * its name.
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public Object getValue(String attr) throws RemoteAccessException {
        if (attr.equals("identifier")) {
            return identifier;
        }
        String attrs[] = new String[1];
        attrs[0] = attr;
        return getValues(attrs)[0];
    }

    protected AttributeDescription lookupAttribute(String name) {
        AttributeDescription attds[] = description.getAttributeDescriptions();
        edu.hkust.clap.monitor.Monitor.loopBegin(1187);
for (int i = 0; i < attds.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1187);
{
            AttributeDescription ad = attds[i];
            if (ad.getName().equals(name)) return ad;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1187);

        return null;
    }

    /**
     * @param attrs The (ordered) set of attributes whose value is to be
     * fetched.
     * @return An (ordered) set of values, one per queried attribute.
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public Object[] getValues(String attrs[]) throws RemoteAccessException {
        Object values[] = new Object[attrs.length];
        edu.hkust.clap.monitor.Monitor.loopBegin(1188);
for (int i = 0; i < attrs.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1188);
{
            AttributeDescription ad = lookupAttribute(attrs[i]);
            if (ad != null) {
                values[i] = ad.getValue();
            } else {
                values[i] = null;
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1188);

        return values;
    }

    /**
     * @param attr The attribute to set, encoded as it's name.
     * @param value The new value for that attribute.
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public void setValue(String attr, Object value) throws RemoteAccessException {
        String attrs[] = new String[1];
        Object vals[] = new Object[1];
        attrs[0] = attr;
        vals[0] = value;
        setValues(attrs, vals);
    }

    /**
     * Set a set of attribute values in one shot.
     * This method guarantees that either all setting is done, or none of
     * them are.
     * @param attrs The (ordered) list of attribute to set, encoded as their
     * names.
     * @param values The (ordered) list of values, for each of the above
     * attributes.
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public void setValues(String names[], Object values[]) throws RemoteAccessException {
        String newId = null;
        boolean change = false;
        AttributeDescription attrs[] = new AttributeDescription[names.length];
        edu.hkust.clap.monitor.Monitor.loopBegin(1189);
for (int i = 0; i < names.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1189);
{
            AttributeDescription ad = lookupAttribute(names[i]);
            if (ad != null) {
                ad.setValue(values[i]);
                attrs[i] = ad;
            }
            if (names[i].equals("identifier")) {
                change = true;
                newId = (String) values[i];
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1189);

        ResourceDescription descr = description.getClone(attrs);
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            OutputStream out;
            if (debug) {
                out = bout;
            } else {
                out = new GZIPOutputStream(bout);
            }
            admin.writer.writeResourceDescription(descr, out);
            byte bits[] = bout.toByteArray();
            Request req = createRequest();
            req.setMethod("SET-VALUES");
            req.setContentType(admin.conftype);
            req.setContentLength(bits.length);
            if (!debug) {
                req.addTransferEncoding("gzip");
            }
            req.setOutputStream(new ByteArrayInputStream(bits));
            Reply rep = admin.runRequest(req);
        } catch (RemoteAccessException rae) {
            throw rae;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RemoteAccessException("exception " + ex.getMessage());
        }
        if (change) {
            identifier = new String(newId);
            try {
                if (!isFrame()) {
                    if (iscontainer) url = new URL(parent.toString() + identifier + "/"); else url = new URL(parent.toString() + identifier);
                    updateURL(new URL(parent.toString() + identifier));
                } else {
                    String oldFile = url.getFile();
                    int index = oldFile.lastIndexOf('?');
                    String newFile = oldFile.substring(0, index);
                    updateURL(new URL(url, newFile));
                }
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }
        return;
    }

    public void updateURL(URL parentURL) {
        if (isFrame()) {
            try {
                url = new URL(parentURL, parentURL.getFile() + "?" + identifier);
            } catch (MalformedURLException ex) {
                return;
            }
        }
        if (frames != null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(1190);
for (int i = 0; i < frames.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1190);
{
                frames[i].updateURL(url);
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1190);

        }
    }

    /**
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public boolean isContainer() throws RemoteAccessException {
        if (identifier != null) {
            if (identifier.equals("root")) return false;
            if (identifier.equals("control")) {
                String classname = getClassHierarchy()[0];
                if (classname.equals("org.w3c.jigsaw.http.ControlResource")) return false;
            }
        }
        return iscontainer;
    }

    /**
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public boolean isIndexersCatalog() throws RemoteAccessException {
        return isindexerscatalog;
    }

    /**
     * Is is a DirectoryResource
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public boolean isDirectoryResource() throws RemoteAccessException {
        if (identifier != null) {
            if (identifier.equals("root")) return false;
        }
        return isDirectoryResource;
    }

    /**
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public String[] enumerateResourceIdentifiers() throws RemoteAccessException {
        if (!iscontainer) throw new RuntimeException("not a container");
        try {
            update();
            return description.getChildren();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RemoteAccessException("http " + ex.getMessage());
        }
    }

    /**
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public RemoteResource loadResource(String identifier) throws RemoteAccessException {
        try {
            Request req = createRequest();
            req.setMethod("LOAD-RESOURCE");
            req.setURL(new URL(url.toString() + URLEncoder.encode(identifier)));
            Reply rep = admin.runRequest(req);
            InputStream in = getInputStream(rep);
            RemoteResource ret = admin.reader.readResource(url, identifier, in);
            in.close();
            return ret;
        } catch (RemoteAccessException rae) {
            throw rae;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RemoteAccessException(ex.getMessage());
        }
    }

    /**
     * Register a new resource within this container.
     * @param id The identifier of the resource to be created.
     * @param classname The name of the class of the resource to be added.
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public RemoteResource registerResource(String id, String classname) throws RemoteAccessException {
        ResourceDescription rd = new EmptyDescription(classname, id);
        try {
            Request req = createRequest();
            req.setMethod("REGISTER-RESOURCE");
            req.setContentType(admin.conftype);
            req.setURL(url);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            OutputStream out;
            if (debug) {
                out = bout;
            } else {
                out = new GZIPOutputStream(bout);
            }
            admin.writer.writeResourceDescription(rd, out);
            byte bits[] = bout.toByteArray();
            req.setContentLength(bits.length);
            if (!debug) {
                req.addTransferEncoding("gzip");
            }
            req.setOutputStream(new ByteArrayInputStream(bits));
            Reply rep = admin.runRequest(req);
            rd = admin.reader.readResourceDescription(getInputStream(rep));
            RemoteResource ret = new PlainRemoteResource(admin, url, rd.getIdentifier(), rd);
            return ret;
        } catch (RemoteAccessException rae) {
            throw rae;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RemoteAccessException(ex.getMessage());
        }
    }

    /**
     * Is this resource a framed resource ?
     * @return A boolean, <strong>true</strong> if the resource is framed
     * and it currently has some frames attached, <strong>false</strong>
     * otherwise.
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public boolean isFramed() throws RemoteAccessException {
        return isframed;
    }

    /**
     * Get the frames attached to that resource.
     * Each frame is itself a resource, so it is returned as an instance of
     * a remote resource.
     * @return A (posssibly <strong>null</strong>) array of frames attached
     * to that resource.
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public RemoteResource[] getFrames() throws RemoteAccessException {
        if (!isframed) throw new RuntimeException("not a framed resource");
        return frames;
    }

    /**
     * Unregister a given frame from that resource.
     * @param frame The frame to unregister.
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public void unregisterFrame(RemoteResource frame) throws RemoteAccessException {
        if (!isframed) throw new RuntimeException("not a framed resource");
        if (frames == null) throw new RuntimeException("this resource has no frames");
        String id = null;
        try {
            id = ((PlainRemoteResource) frame).identifier;
            Request req = createRequest();
            req.setMethod("UNREGISTER-FRAME");
            req.setContentType(admin.conftype);
            req.setURL(url);
            ResourceDescription dframe = new EmptyDescription("", id);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            OutputStream out;
            if (debug) {
                out = bout;
            } else {
                out = new GZIPOutputStream(bout);
            }
            admin.writer.writeResourceDescription(dframe, out);
            byte bits[] = bout.toByteArray();
            req.setContentLength(bits.length);
            if (!debug) {
                req.addTransferEncoding("gzip");
            }
            req.setOutputStream(new ByteArrayInputStream(bits));
            Reply rep = admin.runRequest(req);
        } catch (RemoteAccessException rae) {
            throw rae;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RemoteAccessException(ex.getMessage());
        }
        RemoteResource f[] = new RemoteResource[frames.length - 1];
        int j = 0;
        edu.hkust.clap.monitor.Monitor.loopBegin(1191);
for (int i = 0; i < frames.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1191);
{
            if (((PlainRemoteResource) frames[i]).identifier.equals(id)) {
                System.arraycopy(frames, i + 1, f, j, frames.length - i - 1);
                frames = f;
                return;
            } else {
                try {
                    f[j++] = frames[i];
                } catch (ArrayIndexOutOfBoundsException ex) {
                    return;
                }
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1191);

    }

    public boolean isFrame() {
        return isFrameURL(url);
    }

    protected boolean isFrameURL(URL furl) {
        return (furl.toString().lastIndexOf('?') != -1);
    }

    /**
     * Attach a new frame to that resource.
     * @param identifier The name for this frame (if any).
     * @param clsname The name of the frame's class.
     * @return A remote handle to the (remotely) created frame instance.
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public RemoteResource registerFrame(String id, String classname) throws RemoteAccessException {
        if (!isframed) throw new RuntimeException("not a framed resource");
        try {
            Request req = createRequest();
            req.setMethod("REGISTER-FRAME");
            req.setContentType(admin.conftype);
            ResourceDescription dframe = new EmptyDescription(classname, id);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            OutputStream out;
            if (debug) {
                out = bout;
            } else {
                out = new GZIPOutputStream(bout);
            }
            admin.writer.writeResourceDescription(dframe, out);
            byte bits[] = bout.toByteArray();
            req.setContentLength(bits.length);
            if (!debug) {
                req.addTransferEncoding("gzip");
            }
            req.setOutputStream(new ByteArrayInputStream(bits));
            Reply rep = admin.runRequest(req);
            dframe = admin.reader.readResourceDescription(getInputStream(rep));
            id = dframe.getIdentifier();
            URL url = null;
            if (isFrame()) {
                url = new URL(this.url, this.url.getFile() + "?" + id);
            } else {
                url = new URL(parent.toString() + identifier + "?" + id);
            }
            PlainRemoteResource frame = new PlainRemoteResource(admin, parent, url, id, dframe);
            if (frames != null) {
                RemoteResource nf[] = new RemoteResource[frames.length + 1];
                System.arraycopy(frames, 0, nf, 0, frames.length);
                nf[frames.length] = frame;
                frames = nf;
            } else {
                frames = new RemoteResource[1];
                frames[0] = frame;
            }
            return frame;
        } catch (RemoteAccessException rae) {
            throw rae;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RemoteAccessException(ex.getMessage());
        }
    }

    protected void createRemoteFrames() {
        ResourceDescription dframes[] = description.getFrameDescriptions();
        int len = dframes.length;
        this.frames = new RemoteResource[len];
        edu.hkust.clap.monitor.Monitor.loopBegin(1192);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1192);
{
            ResourceDescription dframe = dframes[i];
            String frameid = dframe.getIdentifier();
            URL url = null;
            try {
                if (isFrame()) {
                    url = new URL(this.url, this.url.getFile() + "?" + frameid);
                } else {
                    url = new URL(parent.toString() + identifier + "?" + frameid);
                }
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
                url = null;
            }
            PlainRemoteResource frame = new PlainRemoteResource(admin, parent, url, frameid, dframe);
            frames[i] = frame;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1192);

    }

    /**
     * Dump that resource to the given output stream.
     * @param prt A print stream to dump to.
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public void dump(PrintStream prt) throws RemoteAccessException {
        System.out.println("+ classes: ");
        String classes[] = getClassHierarchy();
        edu.hkust.clap.monitor.Monitor.loopBegin(1193);
for (int i = 0; i < classes.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1193);
System.out.println("\t" + classes[i]);} 
edu.hkust.clap.monitor.Monitor.loopEnd(1193);

        if (isframed && (frames != null)) {
            System.out.println("+ " + frames.length + " frames.");
            edu.hkust.clap.monitor.Monitor.loopBegin(1194);
for (int i = 0; i < frames.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1194);
{
                prt.println("\t" + ((PlainRemoteResource) frames[i]).identifier);
                ((PlainRemoteResource) frames[i]).dump(prt);
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1194);

        }
        System.out.println("+ attributes: ");
        AttributeDescription attrs[] = getAttributes();
        edu.hkust.clap.monitor.Monitor.loopBegin(1195);
for (int i = 0; i < attrs.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1195);
{
            Attribute att = attrs[i].getAttribute();
            if (att.checkFlag(Attribute.EDITABLE)) {
                Object value = attrs[i].getValue();
                if (value != null) {
                    if (att instanceof SimpleAttribute) {
                        SimpleAttribute sa = (SimpleAttribute) att;
                        prt.println("\t" + att.getName() + "=" + sa.pickle(attrs[i].getValue()));
                    } else if (att instanceof ArrayAttribute) {
                        ArrayAttribute aa = (ArrayAttribute) att;
                        String values[] = aa.pickle(attrs[i].getValue());
                        prt.print("\t" + att.getName() + "=");
                        edu.hkust.clap.monitor.Monitor.loopBegin(1196);
for (int j = 0; j < values.length; j++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1196);
{
                            if (j != 0) prt.print(" | ");
                            prt.print(values[j]);
                        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1196);

                    }
                } else prt.println("\t" + att.getName() + " <undef>");
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1195);

    }

    /**
     * reload the RemoteResource.
     */
    protected void update() throws RemoteAccessException {
        try {
            Request req = createRequest();
            req.setMethod("LOAD-RESOURCE");
            Reply rep = admin.runRequest(req);
            InputStream in = getInputStream(rep);
            this.description = admin.reader.readResourceDescription(in);
            createRemoteFrames();
        } catch (RemoteAccessException rae) {
            throw rae;
        } catch (Exception ex) {
            throw new RemoteAccessException(ex.getMessage());
        }
    }

    PlainRemoteResource(AdminContext admin, URL parent, String identifier, ResourceDescription description) {
        this(admin, parent, null, identifier, description);
    }

    PlainRemoteResource(AdminContext admin, URL parent, URL url, String identifier, ResourceDescription description) {
        this.admin = admin;
        this.parent = parent;
        this.identifier = identifier;
        this.description = description;
        String classes[] = description.getClassesAndInterfaces();
        edu.hkust.clap.monitor.Monitor.loopBegin(1197);
for (int i = 0; i < classes.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1197);
{
            if (classes[i].equals("org.w3c.tools.resources.ContainerInterface")) iscontainer = true;
            if (classes[i].equals("org.w3c.tools.resources.FramedResource")) isframed = true;
            if (classes[i].equals("org.w3c.tools.resources.DirectoryResource")) isDirectoryResource = true;
            if (classes[i].equals("org.w3c.tools.resources.indexer.IndexersCatalog")) isindexerscatalog = true;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1197);

        if (url == null) {
            if (parent != null) {
                String encoded = ((identifier == null) ? identifier : URLEncoder.encode(identifier));
                String urlpart = iscontainer ? encoded + "/" : encoded;
                try {
                    this.url = ((identifier != null) ? new URL(parent.toString() + urlpart) : parent);
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                    this.url = null;
                }
            } else {
                this.url = null;
            }
        } else {
            this.url = url;
        }
        createRemoteFrames();
    }
}
