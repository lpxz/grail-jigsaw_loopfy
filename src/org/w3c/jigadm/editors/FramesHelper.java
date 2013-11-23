package org.w3c.jigadm.editors;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.Scrollbar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.StringTokenizer;
import org.w3c.jigadm.PropertyManager;
import org.w3c.jigadm.RemoteResourceWrapper;
import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;
import org.w3c.jigadm.events.ResourceChangeEvent;
import org.w3c.tools.widgets.BorderPanel;
import org.w3c.tools.widgets.FakeComboBox;
import org.w3c.tools.widgets.TreeBrowser;
import org.w3c.tools.resources.Attribute;

public class FramesHelper extends ResourceHelper {

    private static String FAL_ADD = "add";

    private static String FAL_DELETE = "delete";

    private static String FAL_EDIT = "edit";

    private static String FAL_BACK = "back";

    class FramesActionListener implements ActionListener {

        class Performer extends Thread {

            String action = null;

            public void run() {
                if (action.equals(FAL_ADD)) {
                    addFrame();
                } else if (action.equals(FAL_DELETE)) {
                    deleteCurrentFrame();
                } else if (action.equals(FAL_BACK)) {
                    initAddPanel(prop);
                }
            }

            Performer(String action) {
                this.action = action;
            }
        }

        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            (new Performer(command)).start();
        }
    }

    private FramesActionListener fal;

    RemoteResourceWrapper rrw = null;

    RemoteResourceWrapper root_rrw = null;

    AttributesHelper frameattr = null;

    private boolean initialized = false;

    Properties prop;

    Panel widget;

    Panel addFrame;

    FakeComboBox combo;

    Component centerComp;

    FrameBrowser fb = null;

    protected void addFrame() {
        String selected = combo.getText();
        if (selected != null) if (selected.length() > 0) {
            boolean authorized = false;
            boolean ok = true;
            RemoteResource newFrame = null;
            edu.hkust.clap.monitor.Monitor.loopBegin(179);
while (!authorized) { 
edu.hkust.clap.monitor.Monitor.loopInc(179);
{
                try {
                    authorized = true;
                    newFrame = rrw.getResource().registerFrame(null, selected);
                } catch (RemoteAccessException ex) {
                    if (ex.getMessage().equals("Unauthorized")) {
                        authorized = false;
                    } else {
                        errorPopup("RemoteAccessException", ex);
                        ok = false;
                    }
                } finally {
                    if (!authorized) {
                        rrw.getBrowser().popupDialog("admin");
                    }
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(179);

            if (ok) {
                RemoteResourceWrapper nrrw = new RemoteResourceWrapper(rrw, newFrame, rrw.getBrowser());
                processEvent(new ResourceChangeEvent(rrw, "added", null, nrrw));
                widget.validate();
                widget.setVisible(true);
            }
        }
    }

    protected void deleteCurrentFrame() {
        if (rrw == root_rrw) return;
        boolean authorized = false;
        boolean ok = true;
        edu.hkust.clap.monitor.Monitor.loopBegin(180);
while (!authorized) { 
edu.hkust.clap.monitor.Monitor.loopInc(180);
{
            try {
                authorized = true;
                rrw.getFatherResource().unregisterFrame(rrw.getResource());
            } catch (RemoteAccessException ex) {
                if (ex.getMessage().equals("Unauthorized")) {
                    authorized = false;
                } else {
                    errorPopup("RemoteAccessException", ex);
                    ok = false;
                }
            } finally {
                if (!authorized) {
                    rrw.getBrowser().popupDialog("admin");
                }
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(180);

        if (ok) {
            initAddPanel(prop);
            processEvent(new ResourceChangeEvent(rrw.getFatherWrapper(), "deleted", rrw, null));
            rrw = root_rrw;
        }
    }

    protected void editFrame(RemoteResourceWrapper framew) {
        rrw = framew;
        boolean authorized;
        frameattr = new AttributesHelper();
        frameattr.addResourceListener(new FramesHelperListener(fb));
        widget.remove(centerComp);
        authorized = false;
        edu.hkust.clap.monitor.Monitor.loopBegin(181);
while (!authorized) { 
edu.hkust.clap.monitor.Monitor.loopInc(181);
{
            try {
                authorized = true;
                frameattr.initialize(framew, null);
            } catch (RemoteAccessException ex) {
                if (ex.getMessage().equals("Unauthorized")) {
                    authorized = false;
                } else {
                    errorPopup("RemoteAccessException", ex);
                }
            } finally {
                if (!authorized) {
                    rrw.getBrowser().popupDialog("admin");
                }
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(181);

        Panel interp = new Panel(new BorderLayout());
        interp.add("Center", frameattr.getComponent());
        centerComp = interp;
        Panel pbutton = new Panel(new GridLayout(1, 2, 2, 2));
        Button back = new Button("Back to Add Frame menu");
        back.setActionCommand(FAL_BACK);
        back.addActionListener(fal);
        pbutton.add(back);
        if (rrw != root_rrw) {
            Button del = new Button("Delete selected Frame");
            del.setActionCommand(FAL_DELETE);
            del.addActionListener(fal);
            pbutton.add(del);
        }
        interp.add("South", pbutton);
        widget.add("Center", centerComp);
        widget.validate();
        widget.setVisible(true);
    }

    public void removeAll() {
        widget.removeAll();
    }

    public void removeCenterComp() {
        widget.remove(centerComp);
    }

    public Component getComponent() {
        return widget;
    }

    /**
     * Commit changes (if any).
     * @exception RemoteAccessException if a remote access error occurs.
     */
    public void commitChanges() throws RemoteAccessException {
        if (!initialized) return;
        return;
    }

    public boolean hasChanged() {
        return false;
    }

    public void resetChanges() {
    }

    public void clearChanged() {
    }

    public final String getTitle() {
        return "Frames";
    }

    public FramesHelper() {
        widget = new Panel();
        fal = new FramesActionListener();
    }

    protected void initFrames() throws RemoteAccessException {
        Panel fbrowser = new Panel(new BorderLayout());
        Scrollbar sv = new Scrollbar(Scrollbar.VERTICAL);
        Scrollbar sh = new Scrollbar(Scrollbar.HORIZONTAL);
        FrameTreeListener ftl = new FrameTreeListener(this);
        fb = new FrameBrowser(ftl, root_rrw);
        fb.setVerticalScrollbar(sv);
        fb.setHorizontalScrollbar(sh);
        fbrowser.add("Center", fb);
        fbrowser.add("East", sv);
        fbrowser.add("South", sh);
        BorderPanel border = new BorderPanel(BorderPanel.LOWERED);
        border.setLayout(new BorderLayout());
        border.add(fbrowser);
        widget.add("South", border);
    }

    protected void initAddPanel(Properties config) {
        if (addFrame == null) {
            addFrame = new Panel(new BorderLayout());
            String af = config.getProperty("org.w3c.jigadm.editors.frames");
            StringTokenizer st = new StringTokenizer(af, "|");
            ScrollPane fsp = new ScrollPane();
            GridBagLayout fgbl = new GridBagLayout();
            GridBagConstraints fgbc = new GridBagConstraints();
            Panel fspp = new Panel(fgbl);
            fsp.add(fspp);
            PropertyManager pm = PropertyManager.getPropertyManager();
            String downPath = pm.getIconLocation("down");
            String leftPath = pm.getIconLocation("left");
            combo = new FakeComboBox(35, 7, true, downPath, leftPath);
            edu.hkust.clap.monitor.Monitor.loopBegin(182);
while (st.hasMoreTokens()) { 
edu.hkust.clap.monitor.Monitor.loopInc(182);
combo.add(st.nextToken().trim());} 
edu.hkust.clap.monitor.Monitor.loopEnd(182);

            fspp.add(combo);
            addFrame.add("Center", fsp);
            Button newb = new Button("Add Frame");
            newb.setActionCommand(FAL_ADD);
            newb.addActionListener(fal);
            addFrame.add("South", newb);
        } else widget.remove(centerComp);
        widget.add("Center", addFrame);
        centerComp = addFrame;
    }

    /**
     * initialize this helper
     * @param rrw The RemoteResourceWrapper
     * @param pr The properties
     * @exception RemoteAccessException if a remote access error occurs.
     */
    public void initialize(RemoteResourceWrapper rrw, Properties pr) throws RemoteAccessException {
        if (!initialized) initialized = true; else {
            widget.removeAll();
        }
        RemoteResource rr;
        this.prop = pr;
        this.rrw = rrw;
        this.root_rrw = rrw;
        rr = rrw.getResource();
        if (rr.isFramed()) {
            widget.setLayout(new BorderLayout());
            initAddPanel(prop);
            initFrames();
        }
    }
}
