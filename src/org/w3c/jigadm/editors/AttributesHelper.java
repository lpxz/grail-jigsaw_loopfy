package org.w3c.jigadm.editors;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Properties;
import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.serialization.AttributeDescription;
import org.w3c.tools.widgets.BorderPanel;
import org.w3c.jigadm.PropertyManager;
import org.w3c.jigadm.RemoteResourceWrapper;
import org.w3c.jigadm.gui.ServerBrowser;
import org.w3c.jigadm.events.ResourceChangeEvent;

public class AttributesHelper extends ResourceHelper {

    class ButtonBarListener implements ActionListener {

        class Commiter extends Thread {

            public void run() {
                try {
                    commitChanges();
                } catch (RemoteAccessException ex) {
                    errorPopup("RemoteAccessException", ex);
                }
            }
        }

        public void actionPerformed(ActionEvent ae) {
            if (ae.getActionCommand().equals("Reset")) resetChanges(); else if (ae.getActionCommand().equals(COMMIT_L)) {
                setMessage("Committing...");
                (new Commiter()).start();
                setMessage("Commit done.");
            }
        }
    }

    class MouseButtonListener extends MouseAdapter {

        public void mouseEntered(MouseEvent e) {
            Component comp = e.getComponent();
            if (comp instanceof Button) {
                String action = ((Button) comp).getActionCommand();
                if (action.equals(COMMIT_L)) {
                    setMessage("Commit the changes to the server.");
                } else if (action.equals(RESET_L)) {
                    setMessage("Reset changes");
                }
            }
        }

        public void mouseExited(MouseEvent e) {
            setMessage("");
        }
    }

    private RemoteResourceWrapper rrw = null;

    private AttributeDescription[] a = null;

    private AttributeEditor[] ae = null;

    private ScrollPane pwidget;

    private boolean initialized = false;

    protected static final String COMMIT_L = "Commit";

    protected static final String RESET_L = "Reset";

    Panel widget;

    Label message;

    public void setMessage(String msg) {
        message.setText(msg);
    }

    /**
     * Commit changes (if any)
     * @exception RemoteAccessException if a remote access error occurs.
     */
    public void commitChanges() throws RemoteAccessException {
        if (!initialized) return;
        int num = 0;
        edu.hkust.clap.monitor.Monitor.loopBegin(1051);
for (int i = 0; i < ae.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1051);
{
            if (ae[i].hasChanged()) num++;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1051);

        boolean authorized;
        String s[] = new String[num];
        Object o[] = new Object[num];
        num = 0;
        edu.hkust.clap.monitor.Monitor.loopBegin(1052);
for (int i = 0; i < ae.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1052);
{
            if (ae[i].hasChanged()) {
                s[num] = a[i].getName();
                o[num] = ae[i].getValue();
                if (s[num].equals("identifier")) {
                    if (rrw.getBrowser() != null) rrw.getBrowser().renameNode(rrw, (String) o[num]);
                    processEvent(new ResourceChangeEvent(rrw, "identifier", null, o[num]));
                }
                num++;
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1052);

        authorized = false;
        edu.hkust.clap.monitor.Monitor.loopBegin(1130);
while (!authorized) { 
edu.hkust.clap.monitor.Monitor.loopInc(1130);
{
            try {
                authorized = true;
                rrw.getResource().setValues(s, o);
            } catch (RemoteAccessException ex) {
                if (ex.getMessage().equals("Unauthorized")) {
                    authorized = false;
                } else {
                    throw ex;
                }
            } finally {
                if (!authorized) {
                    rrw.getBrowser().popupDialog("admin");
                }
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1130);

        clearChanged();
    }

    public boolean hasChanged() {
        if (ae == null) return false;
        boolean changed = false;
        edu.hkust.clap.monitor.Monitor.loopBegin(1054);
for (int i = 0; !changed && i < ae.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1054);
{
            changed = ae[i].hasChanged();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1054);

        return changed;
    }

    public void resetChanges() {
        if (ae == null) return;
        edu.hkust.clap.monitor.Monitor.loopBegin(1055);
for (int i = 0; i < ae.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1055);
{
            if (ae[i].hasChanged()) ae[i].resetChanges();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1055);

    }

    public void clearChanged() {
        if (ae == null) return;
        edu.hkust.clap.monitor.Monitor.loopBegin(1056);
for (int i = 0; i < ae.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1056);
{
            if (ae[i].hasChanged()) ae[i].clearChanged();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1056);

    }

    public Component getComponent() {
        return widget;
    }

    public final String getTitle() {
        return "Attribute";
    }

    public AttributesHelper() {
        widget = new Panel(new BorderLayout());
    }

    /**
     * initialize.
     * @exception RemoteAccessException if a remote access error occurs.
     */
    public void initialize(RemoteResourceWrapper rrw, Properties pr) throws RemoteAccessException {
        if (initialized) return;
        RemoteResource rr;
        AttributeDescription b[] = null;
        String s[] = null;
        int nbn = 0;
        boolean authorized;
        this.rrw = rrw;
        rr = rrw.getResource();
        authorized = false;
        edu.hkust.clap.monitor.Monitor.loopBegin(1131);
while (!authorized) { 
edu.hkust.clap.monitor.Monitor.loopInc(1131);
{
            try {
                authorized = true;
                b = rr.getAttributes();
            } catch (RemoteAccessException ex) {
                if (ex.getMessage().equals("Unauthorized")) {
                    authorized = false;
                } else {
                    throw ex;
                }
            } finally {
                if (!authorized) {
                    rrw.getBrowser().popupDialog("admin");
                }
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1131);

        edu.hkust.clap.monitor.Monitor.loopBegin(1058);
for (int i = 0; i < b.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1058);
if (b[i] == null) nbn++; else if (!b[i].getAttribute().checkFlag(Attribute.EDITABLE)) nbn++;} 
edu.hkust.clap.monitor.Monitor.loopEnd(1058);

        a = new AttributeDescription[b.length - nbn];
        ae = new AttributeEditor[a.length];
        int j = 0;
        edu.hkust.clap.monitor.Monitor.loopBegin(1059);
for (int i = 0; i < b.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1059);
{
            if (b[i] != null && b[i].getAttribute().checkFlag(Attribute.EDITABLE)) {
                a[j++] = b[i];
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1059);

        Label l;
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        Panel p = new Panel(gbl);
        p.setForeground(new Color(0, 0, 128));
        pwidget = new ScrollPane();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        edu.hkust.clap.monitor.Monitor.loopBegin(1132);
for (int i = 0; i < a.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1132);
{
            if (a[i] != null) {
                PropertyManager pm = PropertyManager.getPropertyManager();
                Properties attrProps = pm.getAttributeProperties(rrw, a[i].getAttribute());
                String labelText = (String) attrProps.get("label");
                if (labelText == null) labelText = a[i].getName();
                l = new Label(labelText, Label.RIGHT);
                ae[i] = AttributeEditorFactory.getEditor(rrw, a[i].getAttribute());
                authorized = false;
                edu.hkust.clap.monitor.Monitor.loopBegin(1133);
while (!authorized) { 
edu.hkust.clap.monitor.Monitor.loopInc(1133);
{
                    try {
                        authorized = true;
                        ae[i].initialize(rrw, a[i].getAttribute(), a[i].getValue(), attrProps);
                    } catch (RemoteAccessException ex) {
                        if (ex.getMessage().equals("Unauthorized")) {
                            authorized = false;
                        } else {
                            throw ex;
                        }
                    } finally {
                        if (!authorized) {
                            rrw.getBrowser().popupDialog("admin");
                        }
                    }
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1133);

                gbc.gridwidth = 1;
                gbl.setConstraints(l, gbc);
                p.add(l);
                gbc.gridwidth = GridBagConstraints.REMAINDER;
                gbl.setConstraints(ae[i].getComponent(), gbc);
                p.add(ae[i].getComponent());
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1132);

        pwidget.add(p);
        widget.add("Center", pwidget);
        Panel toolpane = new Panel(new BorderLayout());
        Button commitb = new Button(COMMIT_L);
        Button resetb = new Button(RESET_L);
        MouseButtonListener mbl = new MouseButtonListener();
        commitb.addMouseListener(mbl);
        resetb.addMouseListener(mbl);
        ButtonBarListener bbl = new ButtonBarListener();
        commitb.addActionListener(bbl);
        resetb.addActionListener(bbl);
        message = new Label("", Label.CENTER);
        message.setForeground(Color.white);
        message.setBackground(Color.gray);
        BorderPanel pmsg = new BorderPanel(BorderPanel.IN, 2);
        pmsg.setLayout(new BorderLayout());
        pmsg.add("Center", message);
        toolpane.add("West", commitb);
        toolpane.add("Center", pmsg);
        toolpane.add("East", resetb);
        widget.add("South", toolpane);
        String classes[] = { "" };
        try {
            classes = rr.getClassHierarchy();
        } catch (RemoteAccessException ex) {
            ex.printStackTrace();
        }
        l = new Label("Class: " + classes[0], Label.CENTER);
        l.setForeground(new Color(0, 0, 128));
        widget.add("North", l);
        initialized = true;
    }
}
