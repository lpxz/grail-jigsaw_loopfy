package org.w3c.jigadmin.editors;

import java.awt.Component;
import java.awt.Color;
import java.awt.ScrollPane;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;
import javax.swing.JScrollPane;
import java.util.Properties;
import org.w3c.jigadmin.RemoteResourceWrapper;
import org.w3c.jigadmin.PropertyManager;
import org.w3c.jigadm.editors.AttributeEditor;
import org.w3c.jigadm.events.ResourceChangeEvent;
import org.w3c.jigsaw.admin.RemoteResource;
import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.serialization.AttributeDescription;
import org.w3c.tools.widgets.Utilities;

/**
 * The Attributes helper
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class AttributesHelper extends ResourceHelper {

    /**
     * Our internal ActionListener
     */
    ActionListener al = new ActionListener() {

        public void actionPerformed(ActionEvent ae) {
            if (ae.getActionCommand().equals("Reset")) resetChanges(); else if (ae.getActionCommand().equals(COMMIT_L)) {
                Thread commiter = new Thread() {

                    public void run() {
                        try {
                            commitChanges();
                        } catch (RemoteAccessException ex) {
                            errorPopup("RemoteAccessException", ex);
                        }
                    }
                };
                setMessage("Committing...");
                commiter.start();
                setMessage("Commit done.");
            }
        }
    };

    /**
     * Our internal MouseListener
     */
    MouseAdapter ma = new MouseAdapter() {

        public void mouseEntered(MouseEvent e) {
            Component comp = e.getComponent();
            if (comp instanceof JButton) {
                String action = ((JButton) comp).getActionCommand();
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
    };

    private RemoteResourceWrapper rrw = null;

    private AttributeDescription[] a = null;

    private AttributeEditor[] ae = null;

    private boolean initialized = false;

    protected static final String COMMIT_L = "Commit";

    protected static final String RESET_L = "Reset";

    JPanel widget;

    JLabel message;

    /**
     * Set the message of the information Label.
     * @param msg the message to display
     */
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
        edu.hkust.clap.monitor.Monitor.loopBegin(1053);
while (!authorized) { 
edu.hkust.clap.monitor.Monitor.loopInc(1053);
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
                    rrw.getServerBrowser().popupPasswdDialog("admin");
                }
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1053);

        clearChanged();
    }

    /**
     * tells if the edited resource in the helper has changed
     * @return <strong>true</strong> if the values changed.
     * to get more informations about what has changed, you can use the 
     * three methods below.
     */
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

    /**
     * undo the not-yet-commited changes
     */
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

    /**
     * set the current resource to be the original resource (ie: the
     * hasChanged() method must return <strong>false</false> now.
     * to do a "fine tuned" reset, use one of the three following method.
     */
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

    /**
     * Get the AttributeHelper component
     */
    public Component getComponent() {
        return widget;
    }

    /**
     * Get the AttributeHelper title
     */
    public final String getTitle() {
        return "Attribute";
    }

    /**
     * Constructor.
     */
    public AttributesHelper() {
        widget = new JPanel(new BorderLayout());
    }

    /**
     * initialize the helper
     * @param r the ResourceWrapper containing the Resource edited with 
     * this helper
     * @param p some Properties, used to fine-tune the helper
     * @exception RemoteAccessException if a remote access error occurs.
     */
    public void initialize(org.w3c.jigadm.RemoteResourceWrapper r, Properties pr) throws RemoteAccessException {
        if (initialized) return;
        RemoteResource rr;
        AttributeDescription b[] = null;
        String s[] = null;
        int nbn = 0;
        boolean authorized;
        this.rrw = (RemoteResourceWrapper) r;
        rr = rrw.getResource();
        authorized = false;
        edu.hkust.clap.monitor.Monitor.loopBegin(1057);
while (!authorized) { 
edu.hkust.clap.monitor.Monitor.loopInc(1057);
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
                    rrw.getServerBrowser().popupPasswdDialog("admin");
                }
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1057);

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

        JLabel l;
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        JPanel p = new JPanel(gbl);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.insets = Utilities.insets4;
        edu.hkust.clap.monitor.Monitor.loopBegin(1060);
for (int i = 0; i < a.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1060);
{
            if (a[i] != null) {
                PropertyManager pm = PropertyManager.getPropertyManager();
                Properties attrProps = pm.getAttributeProperties(rrw, a[i].getAttribute());
                String labelText = (String) attrProps.get("label");
                if (labelText == null) labelText = a[i].getName();
                l = new JLabel(labelText, JLabel.RIGHT);
                ae[i] = AttributeEditorFactory.getEditor(rrw, a[i].getAttribute());
                authorized = false;
                edu.hkust.clap.monitor.Monitor.loopBegin(1061);
while (!authorized) { 
edu.hkust.clap.monitor.Monitor.loopInc(1061);
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
                            rrw.getServerBrowser().popupPasswdDialog("admin");
                        }
                    }
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1061);

                gbc.gridwidth = 1;
                gbl.setConstraints(l, gbc);
                p.add(l);
                gbc.gridwidth = GridBagConstraints.REMAINDER;
                gbl.setConstraints(ae[i].getComponent(), gbc);
                p.add(ae[i].getComponent());
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1060);

        JScrollPane pwidget = new JScrollPane(p);
        widget.add("Center", pwidget);
        JPanel toolpane = new JPanel(new BorderLayout());
        JButton commitb = new JButton(COMMIT_L);
        JButton resetb = new JButton(RESET_L);
        commitb.addMouseListener(ma);
        resetb.addMouseListener(ma);
        commitb.addActionListener(al);
        resetb.addActionListener(al);
        message = new JLabel("", JLabel.CENTER);
        message.setForeground(Color.white);
        message.setBackground(Color.gray);
        JPanel pmsg = new JPanel(new BorderLayout());
        pmsg.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
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
        l = new JLabel("Class: " + classes[0], JLabel.CENTER);
        l.setForeground(new Color(0, 0, 128));
        widget.add("North", l);
        initialized = true;
    }
}
