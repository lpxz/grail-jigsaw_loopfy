package org.w3c.tools.resources.event;

public interface FrameEventListener extends java.util.EventListener {

    /**
   * This handles the <code>FRAME_ADDED</code> kind of events.
   * @param evt The FrameEvent.
   */
    public void frameAdded(FrameEvent evt);

    /**
   * This handles the <code>FRAME_MODIFIED</code> kind of events.
   * @param evt The event describing the change.
   */
    public void frameModified(FrameEvent evt);

    /**
   * A frame is about to be removed
   * This handles the <code>FRAME_REMOVED</code> kind of events.
   * @param evt The event describing the change.
   */
    public void frameRemoved(FrameEvent evt);
}
