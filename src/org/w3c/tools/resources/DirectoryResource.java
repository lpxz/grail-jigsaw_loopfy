package org.w3c.tools.resources;

import java.util.Enumeration;
import java.util.Hashtable;
import java.io.File;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import org.w3c.tools.resources.indexer.IndexerModule;
import org.w3c.tools.resources.indexer.ResourceIndexer;
import org.w3c.tools.resources.event.StructureChangedEvent;

/**
 * A simple, and reasonably efficient directory resource.
 */
public class DirectoryResource extends ContainerResource {

    /**
     * Attribute index - The index for our directory attribute.
     */
    protected static int ATTR_DIRECTORY = -1;

    /**
     * Attribute index - The last time we physically visited the directory.
     */
    protected static int ATTR_DIRSTAMP = -1;

    /**
     * Attribute index - The indexer to use for that directory, if any.
     */
    protected static int ATTR_INDEXER = -1;

    /**
     * Attribute index - The index of wether we are extensible.
     */
    protected static int ATTR_EXTENSIBLE = -1;

    /**
     * Attribute index - The index of wether we can be shrinked.
     */
    protected static int ATTR_SHRINKABLE = -1;

    static String di = "directory".intern();

    static {
        Attribute a = null;
        Class cls = null;
        try {
            cls = Class.forName("org.w3c.tools.resources.DirectoryResource");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        a = new FileAttribute("directory", null, Attribute.COMPUTED | Attribute.DONTSAVE);
        ATTR_DIRECTORY = AttributeRegistry.registerAttribute(cls, a);
        a = new DateAttribute("dirstamp", null, Attribute.COMPUTED);
        ATTR_DIRSTAMP = AttributeRegistry.registerAttribute(cls, a);
        a = new StringAttribute("indexer", null, Attribute.EDITABLE);
        ATTR_INDEXER = AttributeRegistry.registerAttribute(cls, a);
        a = new BooleanAttribute("extensible", Boolean.TRUE, Attribute.EDITABLE);
        ATTR_EXTENSIBLE = AttributeRegistry.registerAttribute(cls, a);
        a = new BooleanAttribute("shrinkable", Boolean.TRUE, Attribute.EDITABLE);
        ATTR_SHRINKABLE = AttributeRegistry.registerAttribute(cls, a);
    }

    /**
     * Get the indexer out of the given context.
     * @return A ResourceIndexer instance, guaranteeed not to be <strong>
     * null</strong>.
     */
    protected ResourceReference getIndexer(ResourceContext c) {
        IndexerModule m = (IndexerModule) c.getModule(IndexerModule.NAME);
        ResourceReference rr = m.getIndexer(c);
        return rr;
    }

    public void setValue(int idx, Object value) {
        super.setValue(idx, value);
        if (idx == ATTR_INDEXER) {
            String indexer = getString(ATTR_INDEXER, null);
            if (indexer != null) {
                ResourceContext c = null;
                IndexerModule m = null;
                c = getContext();
                m = (IndexerModule) c.getModule(IndexerModule.NAME);
                m.registerIndexer(c, indexer);
            }
        }
    }

    /**
     * Get the physical directory exported by this resource.
     * @return A non-null File object giving the directory of this resource.
     */
    public File getDirectory() {
        return (File) getValue(ATTR_DIRECTORY, null);
    }

    /**
     * Get the physical directory exported by this resource.
     * @return A non-null File object giving the directory of this resource.
     */
    public File unsafeGetDirectory() {
        return (File) unsafeGetValue(ATTR_DIRECTORY, null);
    }

    /**
     * Get the absolute time at which we examined the physicall directory.
     * @return The date (as a long number of ms since Java epoch), or
     * <strong>-1</strong> if we never examined it before.
     */
    public long getDirStamp() {
        return getLong(ATTR_DIRSTAMP, -1);
    }

    /**
     * Get the extensible flag value.
     * A DirectoryResource is extensible, if it is allowed to create new
     * resources out of the file system knowledge on the fly.
     * <p>Setting this flag might slow down the server. It unfortunatelly
     * defaults to <strong>true</strong> until I have a decent admin
     * program.
     * @return A boolean <strong>true</strong> if the directory is
     *    extensible.
     */
    public boolean getExtensibleFlag() {
        return getBoolean(ATTR_EXTENSIBLE, true);
    }

    /**
     * Get the extensible flag value.
     * A DirectoryResource is extensible, if it is allowed to create new
     * resources out of the file system knowledge on the fly.
     * <p>Setting this flag might slow down the server. It unfortunatelly
     * defaults to <strong>true</strong> until I have a decent admin
     * program.
     * @return A boolean <strong>true</strong> if the directory is
     *    extensible.
     */
    public boolean getShrinkableFlag() {
        return getBoolean(ATTR_SHRINKABLE, true);
    }

    /**
     * Get the extensible flag value.
     * A DirectoryResource is extensible, if it is allowed to create new
     * resources out of the file system knowledge on the fly.
     * <p>Setting this flag might slow down the server. It unfortunatelly
     * defaults to <strong>true</strong> until I have a decent admin
     * program.
     * @return A boolean <strong>true</strong> if the directory is
     *    extensible.
     */
    public boolean unsafeGetShrinkableFlag() {
        Object value = unsafeGetValue(ATTR_SHRINKABLE, null);
        if (value == null) {
            return true;
        } else if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        } else {
            throw new IllegalAttributeAccess(this, attributes[ATTR_SHRINKABLE], "getBoolean");
        }
    }

    /**
     * A resource is about to be removed
     * This handles the <code>RESOURCE_REMOVED</code> kind of events.
     * @param evt The event describing the change.
     */
    public void resourceRemoved(StructureChangedEvent evt) {
        super.resourceRemoved(evt);
        if (!isUnloaded()) markModified();
    }

    /**
     * Create a DirectoryResource and the physical directory too.
     * @param name the name of the resource.
     * @return A ResourceReference instance.
     */
    public ResourceReference createDirectoryResource(String name) {
        File file = new File(getDirectory(), name);
        boolean created = false;
        boolean exists_before = false;
        try {
            if (file.exists()) {
                if (!file.isDirectory()) created = false; else exists_before = true;
            } else {
                file.mkdir();
                created = true;
            }
        } catch (Exception ex) {
            created = false;
        }
        if (!created) return null;
        ResourceReference rr = createDefaultResource(name);
        if (rr == null) {
            if (!exists_before) file.delete();
            return null;
        }
        try {
            Resource r = rr.lock();
            if (!(r instanceof DirectoryResource)) {
                try {
                    r.delete();
                } catch (MultipleLockException ex) {
                }
                if (!exists_before) file.delete();
                return null;
            }
        } catch (InvalidResourceException ex) {
            if (!exists_before) file.delete();
            return null;
        } finally {
            rr.unlock();
        }
        return rr;
    }

    /**
     * Create a Resource and the physical file too.
     * @param name the name of the resource.
     * @return A ResourceReference instance.
     */
    public ResourceReference createResource(String name) {
        return createResource(name, null);
    }

    /**
     * Create a Resource and the physical file too.
     * @param name the name of the resource.
     * @param req the protocol request.
     * @return A ResourceReference instance.
     */
    public ResourceReference createResource(String name, RequestInterface req) {
        File file = new File(getDirectory(), name);
        boolean created = false;
        if (!file.exists()) {
            try {
                (new RandomAccessFile(file, "rw")).close();
                created = true;
            } catch (Exception ex) {
                created = false;
            }
        }
        if (!created) return null;
        ResourceReference rr = createDefaultResource(name, req);
        file.delete();
        return rr;
    }

    /**
     * Index a Resource. Call the indexer.
     * @param name The name of the resource to index.
     * @param defs The defaults attributes.
     * @return A resource instance.
     * @see org.w3c.tools.resources.indexer.SampleResourceIndexer
     */
    private Resource index(String name, Hashtable defs) {
        return index(name, defs, null);
    }

    /**
     * Index a Resource. Call the indexer.
     * @param name The name of the resource to index.
     * @param defs The defaults attributes.
     * @param req The protocol request.
     * @return A resource instance.
     * @see org.w3c.tools.resources.indexer.SampleResourceIndexer
     */
    protected Resource index(String name, Hashtable defs, RequestInterface req) {
        defs.put(id, name);
        updateDefaultChildAttributes(defs);
        ResourceContext context = getContext();
        Resource resource = null;
        ResourceReference rr_indexer = null;
        ResourceReference rr_lastidx = null;
        edu.hkust.clap.monitor.Monitor.loopBegin(1161);
while (context != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(1161);
{
            do {
                rr_indexer = getIndexer(context);
                context = context.getParent();
            } while ((rr_indexer == rr_lastidx) && (context != null));
            if ((rr_lastidx = rr_indexer) != null) {
                try {
                    ResourceIndexer indexer = (ResourceIndexer) rr_indexer.lock();
                    resource = indexer.createResource(this, req, getDirectory(), name, defs);
                    if (resource != null) break;
                } catch (InvalidResourceException ex) {
                    resource = null;
                } finally {
                    rr_indexer.unlock();
                }
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1161);

        return resource;
    }

    /**
     * Get the name of the resource relative to the given filename.
     * @param name The name of the file.
     * @return a String, the resource name.
     * @see org.w3c.tools.resources.indexer.SampleResourceIndexer
     */
    protected String getIndexedName(String name) {
        ResourceContext context = getContext();
        String indexed = null;
        ResourceReference rr_indexer = null;
        ResourceReference rr_lastidx = null;
        edu.hkust.clap.monitor.Monitor.loopBegin(1162);
while (context != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(1162);
{
            do {
                rr_indexer = getIndexer(context);
                context = context.getParent();
            } while ((rr_indexer == rr_lastidx) && (context != null));
            if ((rr_lastidx = rr_indexer) != null) {
                try {
                    ResourceIndexer indexer = (ResourceIndexer) rr_indexer.lock();
                    indexed = indexer.getIndexedName(getDirectory(), name);
                    if (indexed != null) break;
                } catch (InvalidResourceException ex) {
                    indexed = null;
                } finally {
                    rr_indexer.unlock();
                }
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1162);

        return ((indexed == null) ? name : indexed);
    }

    public synchronized ResourceReference createDefaultResource(String name) {
        return createDefaultResource(name, null);
    }

    /**
     * Try creating a default resource having the given name.
     * This method will make its best effort to create a default resource
     * having this name in the directory. If a file with this name exists,
     * it will check the pre-defined admin extensions and look for a match.
     * If a directory with this name exists, and admin allows to do so, it
     * will create a sub-directory resource.
     * @param name The name of the resource to try to create.
     * @param req The incomming request
     * @return A Resource instance, if possible, <strong>null</strong>
     *    otherwise.
     */
    protected synchronized ResourceReference createDefaultResource(String name, RequestInterface req) {
        if (name.equals("..") || name.equals(".") || (name.indexOf('\\') >= 0)) {
            return null;
        }
        Hashtable defs = new Hashtable(10);
        Resource resource = index(name, defs, req);
        ResourceReference rr = null;
        if (resource != null) {
            rr = addResource(resource, defs);
            markModified();
        }
        return rr;
    }

    /**
     * Initialize and register a new resource into this directory.
     * @param resource The uninitialized resource to be added.
     */
    protected ResourceContext updateDefaultChildAttributes(Hashtable attrs) {
        ResourceContext context = null;
        context = super.updateDefaultChildAttributes(attrs);
        String name = (String) attrs.get(id);
        if ((name != null) && (getDirectory() != null)) {
            attrs.put(di, new File(getDirectory(), name));
        }
        return context;
    }

    /**
     * Reindex recursivly all the resources from this DirectoryResource.
     * @param rec recursivly?
     */
    public synchronized void reindex(boolean rec) {
        if (getExtensibleFlag()) {
            Enumeration e = enumerateAllResourceIdentifiers();
            String name = null;
            ResourceReference rr = null;
            Resource r = null;
            edu.hkust.clap.monitor.Monitor.loopBegin(1163);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(1163);
{
                name = (String) e.nextElement();
                rr = lookup(name);
                if (rr != null) {
                    try {
                        r = rr.lock();
                        if (r == this) continue;
                        if (r instanceof DirectoryResource) {
                            DirectoryResource dir = (DirectoryResource) r;
                            Hashtable defs = new Hashtable(5);
                            defs.put(co, dir.getContext());
                            Resource newdir = index(name, defs);
                            if ((newdir == null) && rec) {
                                dir.reindex(true);
                            } else {
                                if (!(newdir instanceof DirectoryResource)) {
                                    throw new RuntimeException("Reindex Error : " + name + " can't be reindexed. " + "The reindexed resource is " + "no more a DirectoryResource.");
                                }
                                DirectoryResource reindexed = (DirectoryResource) newdir;
                                String indexer = reindexed.getString(ATTR_INDEXER, "");
                                if (indexer.equals("")) {
                                    if (rec) {
                                        dir.reindex(true);
                                    }
                                    indexer = dir.getString(ATTR_INDEXER, null);
                                    reindexed.setValue(ATTR_INDEXER, indexer);
                                } else {
                                    dir.setValue(ATTR_INDEXER, indexer);
                                    if (rec) {
                                        dir.reindex(true);
                                    }
                                }
                                reindexed.setValue(ATTR_KEY, dir.getKey());
                                dir.setValue(ATTR_IDENTIFIER, name + "-bakindex");
                                addResource(reindexed, defs);
                                try {
                                    dir.replace(reindexed);
                                } catch (MultipleLockException ex) {
                                    throw new RuntimeException("Reindex Error : " + ex.getMessage());
                                }
                            }
                        } else if (!(r instanceof AbstractContainer)) {
                            Hashtable resdefs = new Hashtable(10);
                            Resource resource = index(name, resdefs);
                            if (resource != null) {
                                try {
                                    r.delete();
                                } catch (MultipleLockException ex) {
                                    throw new RuntimeException("Reindex Error : " + ex.getMessage());
                                }
                                addResource(resource, resdefs);
                            }
                        }
                    } catch (InvalidResourceException ex) {
                        System.out.println(ex.getMessage());
                    } finally {
                        rr.unlock();
                    }
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1163);

            markModified();
        }
    }

    /**
     * Enumerate all available children resource identifiers. 
     * This method <em>requires</em> that we create all our pending resources.
     * @return An enumeration of all our resources.
     */
    protected synchronized Enumeration enumerateAllResourceIdentifiers() {
        File directory = getDirectory();
        if (directory != null) {
            synchronized (this) {
                String lst[] = directory.list();
                if (lst != null) {
                    edu.hkust.clap.monitor.Monitor.loopBegin(1164);
for (int i = 0; i < lst.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1164);
{
                        if (lst[i].equals(".") || lst[i].equals("..")) continue;
                        if (super.lookup(lst[i]) == null) {
                            String indexed = getIndexedName(lst[i]);
                            if (indexed.equals(lst[i])) createDefaultResource(lst[i]); else if (super.lookup(indexed) == null) createDefaultResource(lst[i]);
                        }
                    }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1164);

                }
            }
        }
        return super.enumerateResourceIdentifiers(true);
    }

    /**
     * Enumerate all available children resource identifiers. 
     * This method <em>requires</em> that we create all our pending resources
     * if we are in the extensible mode...too bad !
     * @return An enumeration of all our resources.
     */
    public synchronized Enumeration enumerateResourceIdentifiers(boolean all) {
        if (all && getExtensibleFlag()) {
            File directory = getDirectory();
            if (directory != null) {
                synchronized (this) {
                    long dirstamp = directory.lastModified();
                    if (dirstamp > getDirStamp()) {
                        String lst[] = directory.list();
                        if (lst != null) {
                            edu.hkust.clap.monitor.Monitor.loopBegin(1164);
for (int i = 0; i < lst.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1164);
{
                                if (lst[i].equals(".") || lst[i].equals("..")) continue;
                                if (super.lookup(lst[i]) == null) {
                                    String indexed = getIndexedName(lst[i]);
                                    if (indexed.equals(lst[i])) createDefaultResource(lst[i]); else if (super.lookup(indexed) == null) createDefaultResource(lst[i]);
                                }
                            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1164);

                        }
                        setLong(ATTR_DIRSTAMP, dirstamp);
                    }
                }
            }
        }
        return super.enumerateResourceIdentifiers(all);
    }

    /**
     * Lookup the resource having the given name in this directory.
     * @param name The name of the resource.
     * @return A resource instance, or <strong>null</strong>.
     */
    public ResourceReference lookup(String name) {
        ResourceReference rr = null;
        rr = super.lookup(name);
        if (rr != null) return rr;
        return getExtensibleFlag() ? createDefaultResource(name) : null;
    }

    /**
     * Delete this directory resource, for ever.
     * This method will delete the directory resource, and its associated 
     * store, <strong>along</strong> with any of the sub-resources it contains.
     * Deleting the root directory of your server might take sometime...
     * <p>Once the resource is deleted, it isx1 removed from its inital store
     * and will not be unpickleable any more.
     * @exception MultipleLockException if someone has locked this resource.
     */
    public synchronized void delete() throws MultipleLockException {
        disableEvent();
        setBoolean(ATTR_EXTENSIBLE, false);
        super.delete();
    }

    /**
     * Was return false (don't khow why)
     */
    public synchronized boolean verify() {
        return getDirectory().exists();
    }

    /**
     * Initialize this directory resource with the given set of attributes.
     * @param values The attribute values.
     */
    public void initialize(Object values[]) {
        super.initialize(values);
        disableEvent();
        File dir = null;
        if (!definesAttribute(ATTR_DIRECTORY)) {
            ResourceReference rr = getParent();
            if (rr != null) {
                try {
                    Resource parent = rr.lock();
                    if (parent.definesAttribute(di)) {
                        File pdir = (File) parent.getValue(di, null);
                        if (pdir != null) {
                            dir = new File(pdir, getIdentifier());
                            setValue(ATTR_DIRECTORY, dir);
                        }
                    }
                } catch (InvalidResourceException ex) {
                } finally {
                    rr.unlock();
                }
            }
        } else {
            dir = getDirectory();
        }
        ResourceContext c = getContext();
        String indexer = getString(ATTR_INDEXER, null);
        if ((indexer != null) && (!indexer.equals(""))) {
            IndexerModule m = (IndexerModule) c.getModule(IndexerModule.NAME);
            m.registerIndexer(c, indexer);
        }
        enableEvent();
    }
}
