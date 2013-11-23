package org.w3c.jigadmin.editors;

import java.awt.GridLayout;
import java.awt.Font;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import java.util.Properties;
import java.util.Vector;
import org.w3c.jigadmin.RemoteResourceWrapper;
import org.w3c.jigadmin.gui.Message;
import org.w3c.jigadmin.events.ResourceActionListener;
import org.w3c.jigadmin.events.ResourceActionEvent;
import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;
import org.w3c.tools.widgets.Utilities;

/**
 * The admin server editor
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class AdminServerEditor extends ServerEditor implements ServerEditorInterface, ResourceActionListener {

    protected static final String CONTROL_NAME = "control";

    protected static final String REALMS_NAME = "realms";

    private RemoteResource[] controls = null;

    /**
     * initialize the server helpers. There is only one helper for the
     * Admin server, the ControlServerHelper
     * @exception RemoteAccessException if a remote error occurs
     */
    protected void initializeServerHelpers() throws RemoteAccessException {
        shelpers = new ServerHelperInterface[2];
        RemoteResourceWrapper rrw = server.getChildResource(CONTROL_NAME);
        shelpers[0] = ServerHelperFactory.getServerHelper(CONTROL_NAME, rrw);
        ControlServerHelper control = (ControlServerHelper) shelpers[0];
        control.setResOpEnabled(false);
        control.setSaveToolTipText("Save all servers configuration");
        control.setStopToolTipText("Stop all servers");
        control.addResourceActionListener(this);
        rrw = server.getChildResource(REALMS_NAME);
        shelpers[1] = ServerHelperFactory.getServerHelper(REALMS_NAME, rrw);
    }

    /**
     * Get the control resource of all administrated servers.
     * @return a RemoteResource Array
     * @exception RemoteAccessException if a remote error occurs
     */
    protected RemoteResource[] getControls() throws RemoteAccessException {
        if (controls == null) {
            RemoteResource admin = server.getResource();
            String names[] = admin.enumerateResourceIdentifiers();
            Vector vcontrols = new Vector(2);
            edu.hkust.clap.monitor.Monitor.loopBegin(439);
for (int i = 0; i < names.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(439);
{
                if ((!names[i].equals("control")) && (!names[i].equals("realms"))) {
                    RemoteResource srr = admin.loadResource(names[i]);
                    RemoteResource control = srr.loadResource("control");
                    vcontrols.addElement(control);
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(439);

            controls = new RemoteResource[vcontrols.size()];
            vcontrols.copyInto(controls);
        }
        return controls;
    }

    /**
     * A resource action occured.
     * @param e the ResourceActionEvent
     */
    public void resourceActionPerformed(ResourceActionEvent e) {
        switch(e.getResourceActionCommand()) {
            case ResourceActionEvent.SAVE_EVENT:
                try {
                    RemoteResource ctrls[] = getControls();
                    edu.hkust.clap.monitor.Monitor.loopBegin(662);
for (int i = 0; i < ctrls.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(662);
ctrls[i].loadResource("save");} 
edu.hkust.clap.monitor.Monitor.loopEnd(662);

                } catch (RemoteAccessException ex) {
                    Message.showErrorMessage(server, ex);
                }
                break;
            case ResourceActionEvent.STOP_EVENT:
                try {
                    RemoteResource ctrls[] = getControls();
                    edu.hkust.clap.monitor.Monitor.loopBegin(663);
for (int i = 0; i < ctrls.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(663);
ctrls[i].loadResource("stop");} 
edu.hkust.clap.monitor.Monitor.loopEnd(663);

                } catch (RemoteAccessException ex) {
                    Message.showErrorMessage(server, ex);
                }
                break;
            default:
        }
    }

    /**
     * Initialize this editor.
     * @param name the editor name
     * @param rrw the RemoteResourceWrapper wrapping the editor node.
     * @param p the editor properties
     */
    public void initialize(String name, RemoteResourceWrapper rrw, Properties p) {
        super.initialize(name, rrw, p);
        setServer(rrw);
    }

    /**
     * Constructor.
     */
    public AdminServerEditor() {
    }
}
