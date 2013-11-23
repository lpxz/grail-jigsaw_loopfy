package org.w3c.jigadmin.editors;

import java.awt.Insets;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.util.Properties;
import java.util.Hashtable;
import org.w3c.jigadmin.RemoteResourceWrapper;
import org.w3c.jigadmin.PropertyManager;
import org.w3c.jigadmin.gui.Message;
import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.tools.widgets.Utilities;
import org.w3c.tools.sorter.Sorter;

/**
 * The server helper dedicated to the properties.
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class PropertiesServerHelper extends JPanel implements ServerHelperInterface, ChangeListener {

    protected String name = null;

    protected String tooltip = null;

    protected RemoteResourceWrapper root = null;

    protected String properties[] = null;

    protected JPanel cards = null;

    protected Hashtable helpers = null;

    protected String selected = null;

    /**
     * Initialize this editor.
     * @param name the editor name
     * @param rrw the RemoteResourceWrapper wrapping the editor node.
     * @param p the editor properties
     */
    public void initialize(String name, RemoteResourceWrapper rrw, Properties p) {
        this.name = name;
        this.root = rrw;
        this.tooltip = (String) p.get(TOOLTIP_P);
        build();
    }

    /**
     * ChangeListener implementation.
     * @param e a ChangeEvent
     */
    public void stateChanged(ChangeEvent e) {
        Object source = e.getSource();
        if (source instanceof JTabbedPane) {
            JTabbedPane pane = (JTabbedPane) source;
            if (pane.getSelectedComponent() == this) {
                removeAll();
                invalidate();
                build();
                if (selected != null) select(selected);
                validate();
            }
        }
    }

    /**
     * Build the interface.
     */
    protected void build() {
        int len = 0;
        JButton b = null;
        helpers = new Hashtable();
        try {
            properties = root.getResource().enumerateResourceIdentifiers();
            Sorter.sortStringArray(properties, true);
        } catch (RemoteAccessException ex) {
            Message.showErrorMessage(this, ex);
        }
        if (properties == null) return;
        setLayout(new BorderLayout());
        len = properties.length;
        JPanel buttons = new JPanel(new GridLayout(len, 1));
        cards = new JPanel(new CardLayout());
        ActionListener listener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                root.getServerBrowser().setCursor(Cursor.WAIT_CURSOR);
                String property = e.getActionCommand();
                select(property);
                root.getServerBrowser().setCursor(Cursor.DEFAULT_CURSOR);
            }
        };
        edu.hkust.clap.monitor.Monitor.loopBegin(362);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(362);
{
            String name = properties[i];
            b = new JButton(name);
            b.setVerticalTextPosition(AbstractButton.CENTER);
            b.setHorizontalTextPosition(AbstractButton.CENTER);
            b.setActionCommand(name);
            b.addActionListener(listener);
            b.setMargin(Utilities.insets2);
            buttons.add(b);
            JPanel p1 = new JPanel(new GridLayout(1, 1));
            p1.setBorder(BorderFactory.createTitledBorder(name));
            helpers.put(name, p1);
            cards.add(name, p1);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(362);

        JPanel p1 = new JPanel(new BorderLayout());
        p1.add(buttons, BorderLayout.NORTH);
        p1.setBorder(BorderFactory.createTitledBorder("Properties"));
        add(p1, BorderLayout.WEST);
        add(cards, BorderLayout.CENTER);
    }

    protected void select(String name) {
        selected = name;
        JPanel p1 = (JPanel) helpers.get(name);
        p1.removeAll();
        try {
            RemoteResourceWrapper rrw = root.getChildResource(name);
            AttributesHelper helper = new AttributesHelper();
            PropertyManager pm = PropertyManager.getPropertyManager();
            Properties props = pm.getEditorProperties(rrw);
            helper.initialize(rrw, props);
            p1.add(helper.getComponent());
            p1.invalidate();
            p1.validate();
        } catch (RemoteAccessException ex) {
            Message.showErrorMessage(this, ex);
        }
        ((CardLayout) cards.getLayout()).show(cards, name);
    }

    /**
     * Get the helper name.
     * @return a String instance
     */
    public String getName() {
        return name;
    }

    /**
     * Get the helper tooltip
     * @return a String
     */
    public String getToolTip() {
        return tooltip;
    }

    /**
     * Get the Component.
     * @return a Component instance
     */
    public Component getComponent() {
        return this;
    }

    /**
     * Constructor.
     */
    public PropertiesServerHelper() {
    }
}
