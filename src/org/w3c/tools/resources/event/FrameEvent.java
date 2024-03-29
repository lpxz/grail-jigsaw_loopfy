package org.w3c.tools.resources.event;

import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFrame;

public class FrameEvent extends ResourceEvent {

    public String toString() {
        String ssource = ((Resource) getSource()).getIdentifier();
        String stype = null;
        switch(id) {
            case Events.FRAME_ADDED:
                stype = "FRAME_ADDED";
                break;
            case Events.FRAME_MODIFIED:
                stype = "FRAME_MODIFIED";
                break;
            case Events.FRAME_REMOVED:
                stype = "FRAME_REMOVED";
                break;
            default:
                stype = "UNKNOWN";
        }
        return "FrameEvent : [" + ssource + " : " + stype + "]";
    }

    /**
   * Create the Frame event.
   * @param source The frame emitting the event.
   * @param type The kind of event being emitted.
   */
    public FrameEvent(ResourceFrame frame, int type) {
        super(frame, type);
    }
}
