package org.w3c.tools.resources.event;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class StructureChangedAdapter implements StructureChangedListener {

    /**
     * This handles the <code>RESOURCE_MODIFIED</code> kind of events.
     * @param evt The StructureChangeEvent.
     */
    public void resourceModified(StructureChangedEvent evt) {
    }

    /**
     * A new resource has been created in some space.
     * This handles the <code>RESOURCE_CREATED</code> kind of events.
     * @param evt The event describing the change.
     */
    public void resourceCreated(StructureChangedEvent evt) {
    }

    /**
     * A resource is about to be removed
     * This handles the <code>RESOURCE_REMOVED</code> kind of events.
     * @param evt The event describing the change.
     */
    public void resourceRemoved(StructureChangedEvent evt) {
    }

    /**
     * A resource is about to be unloaded
     * This handles the <code>RESOURCE_UNLOADED</code> kind of events.
     * @param evt The event describing the change.
     */
    public void resourceUnloaded(StructureChangedEvent evt) {
    }
}
