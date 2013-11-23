package org.w3c.jigadmin.attributes;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.util.Properties;
import java.util.Vector;
import org.w3c.jigadmin.widgets.ListEditor;
import org.w3c.jigadmin.widgets.ClosableDialog;
import org.w3c.jigadmin.widgets.Icons;
import org.w3c.jigadm.RemoteResourceWrapper;
import org.w3c.jigadm.editors.AttributeEditor;
import org.w3c.jigadm.editors.EditorFeeder;
import org.w3c.jigsaw.admin.RemoteResource;
import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.widgets.TextEditable;

/**
 * An editor for StringArray attributes.  
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */
public class StringArrayEditor extends AttributeEditor {

    protected JFrame frame = null;

    class EditStringArrayPopup extends ClosableDialog implements ActionListener {

        protected StringArrayComponent parent = null;

        protected EditorFeeder feeder = null;

        protected Vector selected = null;

        protected Vector vitems = null;

        protected JList witems = null;

        protected JPanel items = null;

        protected JPanel pitems = null;

        protected JButton waddItem = null;

        protected JButton wdelItem = null;

        protected JList wselected = null;

        protected JPanel pselected = null;

        protected TextEditable newItem = null;

        protected boolean modified = false;

        /**
	 * ActionListsner implementation - One of our button was fired.
	 * @param evt The ActionEvent.
	 */
        public void actionPerformed(ActionEvent evt) {
            String command = evt.getActionCommand();
            if (command.equals("add")) {
                if (newItem.updated()) {
                    modified = true;
                    selected.addElement(newItem.getText());
                }
                newItem.setDefault();
                Object sels[] = witems.getSelectedValues();
                if ((sels != null) && (sels.length > 0)) {
                    modified = true;
                    edu.hkust.clap.monitor.Monitor.loopBegin(353);
for (int i = 0; i < sels.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(353);
{
                        selected.addElement(sels[i]);
                        vitems.removeElement(sels[i]);
                    }} 
edu.hkust.clap.monitor.Monitor.loopEnd(353);

                }
                wselected.setListData(selected);
                witems.setListData(vitems);
            } else if (command.equals("del")) {
                Object sels[] = wselected.getSelectedValues();
                if ((sels != null) && (sels.length > 0)) {
                    modified = true;
                    edu.hkust.clap.monitor.Monitor.loopBegin(354);
for (int i = 0; i < sels.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(354);
{
                        vitems.addElement(sels[i]);
                        selected.removeElement(sels[i]);
                    }} 
edu.hkust.clap.monitor.Monitor.loopEnd(354);

                    wselected.setListData(selected);
                    witems.setListData(vitems);
                }
            } else if (command.equals("update")) {
                if (modified) {
                    String items[] = new String[selected.size()];
                    selected.copyInto(items);
                    parent.setSelectedItems(items);
                    parent.setModified();
                }
                close();
            } else if (command.equals("cancel")) {
                close();
            } else if (evt.getSource().equals(newItem)) {
                if (newItem.updated()) {
                    modified = true;
                    selected.addElement(newItem.getText());
                }
                newItem.setDefault();
                wselected.setListData(selected);
                ((Component) newItem).requestFocus();
            }
        }

        protected void close() {
            modified = false;
            setVisible(false);
            dispose();
        }

        /**
	 * Create the list of possible items, querying the feeder:
	 * @param feeder The one that knows about default items.
	 */
        protected void createDefaultItems(EditorFeeder feeder) {
            this.witems = new JList();
            witems.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            String items[] = feeder.getDefaultItems();
            if (items != null) {
                vitems = new Vector(items.length);
                edu.hkust.clap.monitor.Monitor.loopBegin(355);
for (int i = 0; i < items.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(355);
vitems.addElement(items[i]);} 
edu.hkust.clap.monitor.Monitor.loopEnd(355);

                witems.setListData(vitems);
            } else {
                vitems = new Vector();
            }
        }

        protected void createSelectedItems(String sel[]) {
            this.wselected = new JList();
            wselected.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            if (sel != null) {
                selected = new Vector(sel.length);
                edu.hkust.clap.monitor.Monitor.loopBegin(356);
for (int i = 0; i < sel.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(356);
selected.addElement(sel[i]);} 
edu.hkust.clap.monitor.Monitor.loopEnd(356);

                wselected.setListData(selected);
            } else {
                selected = new Vector();
            }
        }

        protected void updateSize() {
            setSize(parent.editor.getPopupSize());
        }

        public EditStringArrayPopup(StringArrayComponent parent, EditorFeeder feeder, String selected[], String title) {
            super(StringArrayEditor.this.frame, title, false);
            this.parent = parent;
            this.feeder = feeder;
            this.newItem = parent.editor.getTextEditor();
            createDefaultItems(feeder);
            createSelectedItems(selected);
            waddItem = new JButton(Icons.copyLIcon);
            waddItem.setActionCommand("add");
            waddItem.addActionListener(this);
            wdelItem = new JButton(Icons.copyRIcon);
            wdelItem.setActionCommand("del");
            wdelItem.addActionListener(this);
            JPanel arrows = new JPanel(new GridLayout(1, 2, 5, 5));
            arrows.add(wdelItem);
            arrows.add(waddItem);
            JButton Ok = new JButton("Ok");
            Ok.setActionCommand("update");
            Ok.addActionListener(this);
            JButton Cancel = new JButton("Cancel");
            Cancel.setActionCommand("cancel");
            Cancel.addActionListener(this);
            JPanel buttons = new JPanel(new GridLayout(1, 2, 5, 5));
            buttons.add(Ok);
            buttons.add(Cancel);
            JPanel pselected = new JPanel(new BorderLayout(3, 3));
            pselected.setBorder(BorderFactory.createTitledBorder("Selection"));
            pselected.add(new JScrollPane(wselected), "Center");
            JPanel items = new JPanel(new BorderLayout(3, 3));
            items.setBorder(BorderFactory.createTitledBorder("Choice"));
            newItem.addActionListener(this);
            items.add((Component) newItem, "North");
            items.add(new JScrollPane(witems), "Center");
            JPanel lists = new JPanel(new GridLayout(1, 2, 5, 5));
            lists.add(pselected);
            lists.add(items);
            JPanel mainp = new JPanel(new BorderLayout(0, 5));
            mainp.add(arrows, BorderLayout.NORTH);
            mainp.add(lists, BorderLayout.CENTER);
            mainp.add(buttons, BorderLayout.SOUTH);
            mainp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(mainp);
            updateSize();
        }
    }

    class StringArrayComponent extends ListEditor {

        protected StringArrayEditor editor = null;

        protected String selected[] = null;

        protected EditorFeeder feeder = null;

        protected void edit() {
            EditStringArrayPopup popup = new EditStringArrayPopup(this, feeder, getSelectedItems(), "Edit");
            popup.setLocationRelativeTo(this);
            popup.show();
            popup.toFront();
        }

        public void setModified() {
            editor.setModified();
        }

        protected void setSelectedItems(String selected[]) {
            this.selected = selected;
            list.removeAll();
            if (selected != null) list.setListData(selected);
        }

        protected String[] getSelectedItems() {
            return selected;
        }

        StringArrayComponent(StringArrayEditor editor, String selected[], EditorFeeder feeder) {
            super(5, true);
            this.editor = editor;
            this.feeder = feeder;
            setSelectedItems(selected);
        }
    }

    class TextEditor extends JTextField implements TextEditable {

        public boolean updated() {
            return ((getText().length() > 0) && (!getText().equals("")));
        }

        public void setDefault() {
            setText("");
        }

        TextEditor(int nb) {
            super(nb);
            setBorder(BorderFactory.createLoweredBevelBorder());
        }
    }

    /**
     * Properties - The feeder's class name.
     */
    public static final String FEEDER_CLASS_P = "feeder.class";

    protected boolean hasChanged = false;

    protected String oldvalue[] = null;

    protected StringArrayComponent comp = null;

    protected TextEditable getTextEditor() {
        return new TextEditor(15);
    }

    protected Dimension getPopupSize() {
        return new Dimension(350, 250);
    }

    protected void createComponent(EditorFeeder feeder, String selected[]) {
        if (comp == null) comp = new StringArrayComponent(this, selected, feeder);
    }

    protected void setModified() {
        hasChanged = true;
    }

    /**
     * Tells if the edited value has changed
     * @return true if the value changed.
     */
    public boolean hasChanged() {
        return hasChanged;
    }

    /**
     * set the current value to be the original value, ie: changed
     * must return <strong>false</strong> after a reset.
     */
    public void clearChanged() {
        hasChanged = false;
    }

    /**
     * reset the changes (if any)
     */
    public void resetChanges() {
        hasChanged = false;
        comp.setSelectedItems(oldvalue);
    }

    /**
     * Get the current value of the edited value
     * @return an object or <strong>null</strong> if the object was not
     * initialized
     */
    public Object getValue() {
        String[] val = comp.getSelectedItems();
        if ((val != null) && (val.length > 0)) {
            return val;
        }
        return null;
    }

    /**
     * Set the value of the edited value
     * @param o the new value.
     */
    public void setValue(Object o) {
        this.oldvalue = (String[]) o;
        comp.setSelectedItems(oldvalue);
    }

    /**
     * get the Component created by the editor.
     * @return a Component
     */
    public Component getComponent() {
        return comp;
    }

    public static String[] toStringArray(Object array) throws ClassCastException {
        if (array == null) return null;
        if (array instanceof String[]) return (String[]) array;
        if (array instanceof Object[]) {
            Object objects[] = (Object[]) array;
            String strArray[] = new String[objects.length];
            edu.hkust.clap.monitor.Monitor.loopBegin(357);
for (int i = 0; i < objects.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(357);
strArray[i] = objects[i].toString();} 
edu.hkust.clap.monitor.Monitor.loopEnd(357);

            return strArray;
        }
        throw new ClassCastException("Object array required");
    }

    /**
     * Initialize the editor
     * @param w the ResourceWrapper father of the attribute
     * @param a the Attribute we are editing
     * @param o the value of the above attribute
     * @param p some Properties, used to fine-tune the editor
     * @exception RemoteAccessException if a remote access error occurs.
     */
    public void initialize(RemoteResourceWrapper w, Attribute a, Object o, Properties p) throws RemoteAccessException {
        EditorFeeder feeder = null;
        String feederClass = null;
        feederClass = (String) p.get(FEEDER_CLASS_P);
        if (feederClass == null) throw new RuntimeException("StringArrayEditor mis-configuration: " + FEEDER_CLASS_P + " property undefined.");
        try {
            Class c = Class.forName(feederClass);
            feeder = (EditorFeeder) c.newInstance();
            feeder.initialize(w, p);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("StringArrayEditor mis-configured: " + " unable to instantiate " + feederClass + ".");
        }
        this.frame = ((org.w3c.jigadmin.RemoteResourceWrapper) w).getServerBrowser().getFrame();
        String s[] = toStringArray(o);
        createComponent(feeder, s);
        oldvalue = s;
    }

    public StringArrayEditor() {
        super();
    }
}
