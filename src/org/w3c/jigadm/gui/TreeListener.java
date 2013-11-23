package org.w3c.jigadm.gui;

import java.util.EventListener;
import java.awt.Container;
import java.awt.Panel;
import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigadm.RemoteResourceWrapper;
import org.w3c.jigadm.editors.ResourceEditor;

public class TreeListener implements EventListener {

    Panel target = null;

    RemoteResourceWrapper lastr = null;

    class Initializer extends Thread {

        ResourceEditor re;

        public void run() {
            try {
                re.initialize(lastr, null);
            } catch (RemoteAccessException ex) {
            }
        }

        Initializer(ResourceEditor re) {
            this.re = re;
        }
    }

    public void editedChanged(ServerBrowser tb, RemoteResourceWrapper resourcew) {
        lastr = resourcew;
        if (target != null) {
            ResourceEditor re = new ResourceEditor(target);
            re.addResourceListener(new ResourceEditorListener(tb));
            (new Initializer(re)).start();
        }
    }

    public void focusChanged(RemoteResourceWrapper rw) {
        if (rw == null) {
            if (lastr != null) {
                target.removeAll();
            }
        } else {
            if (!rw.equals(lastr)) {
                target.removeAll();
            }
        }
        lastr = rw;
    }

    public void nodeRemoved(RemoteResourceWrapper rw) {
        if (rw.equals(lastr)) {
            lastr = null;
            target.removeAll();
        }
    }

    public TreeListener(Panel target) {
        this.target = target;
    }
}
