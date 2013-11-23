package org.w3c.jigadmin.editors;

import java.awt.GridLayout;
import java.awt.Font;
import java.awt.Component;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import java.util.Properties;
import org.w3c.jigadmin.RemoteResourceWrapper;
import org.w3c.jigadmin.widgets.DnDTabbedPane;
import org.w3c.jigadmin.events.ResourceActionSource;
import org.w3c.jigadmin.events.ResourceActionListener;
import org.w3c.jigsaw.admin.RemoteResource;
import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.tools.widgets.Utilities;
import org.w3c.tools.sorter.Sorter;

/**
 * The server editor.
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class ServerEditor extends JPanel implements ServerEditorInterface {

    private class Retryer extends Thread {

        RemoteAccessException ex = null;

        public void run() {
            edu.hkust.clap.monitor.Monitor.loopBegin(696);
while (server.getServerBrowser().shouldRetry(ex)) { 
edu.hkust.clap.monitor.Monitor.loopInc(696);
{
                try {
                    initializeServerHelpers();
                    break;
                } catch (RemoteAccessException ex2) {
                    ex = ex2;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(696);

            build();
        }

        private Retryer(RemoteAccessException ex) {
            this.ex = ex;
        }
    }

    protected RemoteResourceWrapper server = null;

    protected String name = null;

    protected ServerHelperInterface shelpers[] = null;

    /**
     * Reload the server configuration.
     * @param server the new server wrapper
     */
    public void setServer(RemoteResourceWrapper server) {
        this.server = server;
        try {
            initializeServerHelpers();
            build();
        } catch (RemoteAccessException ex) {
            (new Retryer(ex)).start();
        }
    }

    /**
     * Get the component.
     * @return a Component
     */
    public Component getComponent() {
        return this;
    }

    /**
     * initialize the server helpers.
     * @exception RemoteAccessException if a remote error occurs
     */
    protected void initializeServerHelpers() throws RemoteAccessException {
        shelpers = null;
        RemoteResource rr = server.getResource();
        String names[] = rr.enumerateResourceIdentifiers();
        Sorter.sortStringArray(names, true);
        shelpers = new ServerHelperInterface[names.length];
        edu.hkust.clap.monitor.Monitor.loopBegin(697);
for (int i = 0; i < names.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(697);
{
            RemoteResourceWrapper rrw = server.getChildResource(names[i]);
            shelpers[i] = ServerHelperFactory.getServerHelper(names[i], rrw);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(697);

        edu.hkust.clap.monitor.Monitor.loopBegin(698);
for (int i = 0; i < shelpers.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(698);
{
            if (shelpers[i] instanceof ResourceActionSource) {
                ResourceActionSource source = (ResourceActionSource) shelpers[i];
                edu.hkust.clap.monitor.Monitor.loopBegin(699);
for (int j = 0; j < shelpers.length; j++) { 
edu.hkust.clap.monitor.Monitor.loopInc(699);
{
                    if (j != i) {
                        if (shelpers[j] instanceof ResourceActionListener) {
                            ResourceActionListener listener = (ResourceActionListener) shelpers[j];
                            source.addResourceActionListener(listener);
                        }
                    }
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(699);

            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(698);

    }

    /**
     * Build the interface.
     */
    protected void build() {
        removeAll();
        invalidate();
        setLayout(new BorderLayout());
        TitledBorder border = BorderFactory.createTitledBorder(name);
        border.setTitleFont(Utilities.reallyBigBoldFont);
        setBorder(border);
        JTabbedPane tabbedPane = new DnDTabbedPane();
        if (shelpers == null) return;
        edu.hkust.clap.monitor.Monitor.loopBegin(700);
for (int i = 0; i < shelpers.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(700);
{
            if (shelpers[i] instanceof ChangeListener) tabbedPane.addChangeListener((ChangeListener) shelpers[i]);
            if (shelpers[i] instanceof ControlServerHelper) {
                add(shelpers[i].getComponent(), BorderLayout.NORTH);
            } else {
                String name = shelpers[i].getName().replace('_', ' ');
                char chars[] = name.toCharArray();
                chars[0] = Character.toUpperCase(chars[0]);
                String helperName = new String(chars);
                tabbedPane.addTab(helperName, null, shelpers[i].getComponent(), shelpers[i].getToolTip());
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(700);

        tabbedPane.setSelectedIndex(0);
        add(tabbedPane, BorderLayout.CENTER);
        validate();
    }

    /**
     * Initialize this editor.
     * @param name the editor name
     * @param rrw the RemoteResourceWrapper wrapping the editor node.
     * @param p the editor properties
     */
    public void initialize(String name, RemoteResourceWrapper rrw, Properties p) {
        this.server = rrw;
        this.name = name;
    }

    public ServerEditor() {
    }
}
