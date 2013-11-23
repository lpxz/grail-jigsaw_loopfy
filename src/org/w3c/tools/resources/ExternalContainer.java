package org.w3c.tools.resources;

import java.util.Enumeration;
import java.util.Hashtable;
import java.io.File;

/**
 * A Container which manage an external store, outside the space.
 */
public abstract class ExternalContainer extends ContainerResource {

    /**
     * Our transientFlag, is true that container must not be saved.
     */
    protected boolean transientFlag = false;

    /**
     * Our external repository.
     */
    protected File repository = null;

    public ResourceReference createDefaultResource(String name) {
        throw new RuntimeException("not extensible");
    }

    /**
     * Mark this resource as having been modified.
     */
    public void markModified() {
        if (transientFlag) {
            setValue(ATTR_LAST_MODIFIED, new Long(System.currentTimeMillis()));
        } else {
            super.markModified();
        }
    }

    /**
     * acquire children and notify space if we will be saved.
     */
    protected synchronized void acquireChildren() {
        if (!acquired) {
            ResourceSpace space = getSpace();
            if (repository != null) {
                space.acquireChildren(getChildrenSpaceEntry(), repository, transientFlag);
            } else {
                space.acquireChildren(getChildrenSpaceEntry());
            }
            acquired = true;
        }
    }

    /**
     * Delete this Resource instance , and remove it from its store.
     * This method will erase definitely this resource, for ever, by removing
     * it from its resource store (when doable).
     * @exception MultipleLockException if someone has locked this resource.
     */
    public synchronized void delete() throws MultipleLockException {
        if (transientFlag) {
            ResourceSpace space = getSpace();
            if (space != null) {
                acquireChildren();
                Enumeration e = enumerateResourceIdentifiers();
                ResourceReference rr = null;
                Resource resource = null;
                edu.hkust.clap.monitor.Monitor.loopBegin(1110);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(1110);
{
                    rr = lookup((String) e.nextElement());
                    if (rr != null) {
                        try {
                            synchronized (rr) {
                                resource = rr.lock();
                                resource.delete();
                            }
                        } catch (InvalidResourceException ex) {
                        } finally {
                            rr.unlock();
                        }
                    }
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1110);

                space.deleteChildren(getChildrenSpaceEntry());
            }
        } else {
            super.delete();
        }
    }

    /**
     * Get The repository for this external container.
     * Warning: called in the constructor!
     * @param context The container context.
     * @return A File instance
     */
    public abstract File getRepository(ResourceContext context);

    public void initialize(Object values[]) {
        super.initialize(values);
        if (repository == null) repository = getRepository(getContext());
    }

    /**
     * @param id The identifier.
     * @param context The default context.
     * @param transientFlag The transient flag.
     */
    public ExternalContainer(String identifier, ResourceContext context, boolean transientFlag) {
        Hashtable h = new Hashtable(3);
        h.put(id, identifier);
        h.put(co, context);
        initialize(h);
        this.acquired = false;
        this.transientFlag = transientFlag;
        if (transientFlag) context.setResourceReference(new DummyResourceReference(this));
    }

    public ExternalContainer() {
        super();
        this.acquired = false;
        this.transientFlag = false;
        this.repository = null;
    }
}
