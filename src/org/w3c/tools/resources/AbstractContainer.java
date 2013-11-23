package org.w3c.tools.resources;

import java.util.Enumeration;
import java.util.Hashtable;
import org.w3c.tools.resources.event.AttributeChangedEvent;
import org.w3c.tools.resources.event.AttributeChangedListener;
import org.w3c.tools.resources.event.StructureChangedEvent;
import org.w3c.tools.resources.event.StructureChangedListener;

/**
 * The top level class for Resource Container.
 */
public abstract class AbstractContainer extends FramedResource implements ContainerInterface, StructureChangedListener, AttributeChangedListener {

    public static String ur = "url".intern();

    /**
     * This handles the <code>RESOURCE_MODIFIED</code> kind of events.
     * @param evt The StructureChangeEvent.
     */
    public void resourceModified(StructureChangedEvent evt) {
        if (debugEvent) displayEvent(this, evt);
    }

    /**
     * A new resource has been created in some space.
     * This handles the <code>RESOURCE_CREATED</code> kind of events.
     * @param evt The event describing the change.
     */
    public void resourceCreated(StructureChangedEvent evt) {
        if (debugEvent) displayEvent(this, evt);
    }

    /**
     * A resource is about to be removed
     * This handles the <code>RESOURCE_REMOVED</code> kind of events.
     * @param evt The event describing the change.
     */
    public void resourceRemoved(StructureChangedEvent evt) {
        if (debugEvent) displayEvent(this, evt);
    }

    /**
     * A resource is about to be unloaded
     * This handles the <code>RESOURCE_UNLOADED</code> kind of events.
     * @param evt The event describing the change.
     */
    public void resourceUnloaded(StructureChangedEvent evt) {
    }

    /**
     * Gets called when a property changes.
     * @param evt The AttributeChangeEvent describing the change.
     */
    public void attributeChanged(AttributeChangedEvent evt) {
        if (debugEvent) displayEvent(this, evt);
    }

    /**
     * Enumerate children resource identifiers.
     * @param all Should all resources be enumerated ? Resources are often
     * created on demand only, this flag allows the caller to tell the 
     * container about wether it is interested only in already created
     * resources, or in all resources (even the one that have not yet been
     * created).
     * @return An String enumeration, one element per child.
     */
    public abstract Enumeration enumerateResourceIdentifiers(boolean all);

    public Enumeration enumerateResourceIdentifiers() {
        return enumerateResourceIdentifiers(true);
    }

    /**
     * Ask our frames to update default child attributes.
     * @param attrs A hashtable.
     */
    protected ResourceContext updateDefaultChildAttributes(Hashtable attrs) {
        ResourceFrame frames[] = getFrames();
        if (frames != null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(435);
for (int i = 0; i < frames.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(435);
{
                if (frames[i] == null) continue;
                frames[i].updateDefaultChildAttributes(attrs);
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(435);

        }
        return (ResourceContext) attrs.get(co);
    }

    /**
     * Lookup a children in the container.
     * @param name The name of the children to lookup.
     * the resource from its store.
     */
    public abstract ResourceReference lookup(String name);

    /**
     * Remove a child resource from that container.
     * @param name The name of the child to remove.
     * @exception MultipleLockException If someone else
     * has locked the resource.
     */
    public abstract void delete(String name) throws MultipleLockException;

    /**
     * Create a default child resource in that container.
     * This method is called by the editor to add a default resource
     * in the container under the given name. The meaning of <em>default</em>
     * is left up to the container here.
     * @param name The identifier for the new resource.
     */
    public abstract ResourceReference createDefaultResource(String name);

    /**
     * Initialize and register the given resource within that container.
     * @param name The identifier for the resource.
     * @param resource An unitialized resource instance.
     * @param defs A default set of init attribute values (may be
     * <strong>null</strong>).
     * @exception InvalidResourceException If an error occurs during the
     * registration.
     */
    public abstract void registerResource(String name, Resource resource, Hashtable defs) throws InvalidResourceException;
}
