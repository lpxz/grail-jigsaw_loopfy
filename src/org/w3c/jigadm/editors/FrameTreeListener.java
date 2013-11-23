package org.w3c.jigadm.editors;

import java.util.EventListener;
import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigadm.RemoteResourceWrapper;

public class FrameTreeListener implements EventListener {

    FramesHelper helper = null;

    RemoteResourceWrapper lastr = null;

    FramesHelperListener fl = null;

    class Initializer extends Thread {

        FramesHelper fh;

        public void run() {
            try {
                fh.initialize(lastr, null);
            } catch (RemoteAccessException ex) {
            }
        }

        Initializer(FramesHelper fh) {
            this.fh = fh;
        }
    }

    public void editedChanged(FrameBrowser fb, RemoteResourceWrapper framew) {
        lastr = framew;
        if (fl != null) helper.removeResourceListener(fl);
        fl = new FramesHelperListener(fb);
        helper.addResourceListener(fl);
        helper.editFrame(framew);
    }

    public void focusChanged(RemoteResourceWrapper rw) {
        if (rw == null) {
            if (lastr != null) {
                helper.removeCenterComp();
            }
        } else {
            if (!rw.equals(lastr)) {
                helper.removeCenterComp();
            }
        }
        lastr = rw;
    }

    public void nodeRemoved(RemoteResourceWrapper rw) {
        if (rw.equals(lastr)) {
            lastr = null;
            helper.removeCenterComp();
        }
    }

    public FrameTreeListener(FramesHelper helper) {
        this.helper = helper;
    }
}
