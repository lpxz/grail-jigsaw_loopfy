package org.w3c.jigadm.editors;

import java.awt.Dimension;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import org.w3c.tools.resources.Attribute;
import org.w3c.jigadm.RemoteResourceWrapper;
import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.tools.widgets.IPTextField;
import org.w3c.tools.widgets.TextEditable;

/**
 * IPTemplateArrayEditor : 
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */
public class IPTemplateArrayEditor extends StringArrayEditor {

    protected short oldshortarray[][] = null;

    private String shortsToIPString(short one, short two, short three, short four) {
        return (((one == 256) ? "*" : String.valueOf((int) one)) + "." + ((two == 256) ? "*" : String.valueOf((int) two)) + "." + ((three == 256) ? "*" : String.valueOf((int) three)) + "." + ((four == 256) ? "*" : String.valueOf((int) four)));
    }

    protected String[] toStringArray(short selectedItems[][]) {
        if (selectedItems == null) return null;
        String selected[] = new String[selectedItems.length];
        edu.hkust.clap.monitor.Monitor.loopBegin(59);
for (int i = 0; i < selectedItems.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(59);
selected[i] = shortsToIPString(selectedItems[i][0], selectedItems[i][1], selectedItems[i][2], selectedItems[i][3]);} 
edu.hkust.clap.monitor.Monitor.loopEnd(59);

        return selected;
    }

    protected void setSelectedItems(short selectedItems[][]) {
        comp.setSelectedItems(toStringArray(selectedItems));
    }

    /**
     * reset the changes (if any)
     */
    public void resetChanges() {
        hasChanged = false;
        setSelectedItems(oldshortarray);
    }

    /**
     * Get the current value of the edited value
     * @return an object or <strong>null</strong> if the object was not
     * initialized
     */
    public Object getValue() {
        StringTokenizer st = null;
        String stvalue[] = comp.getSelectedItems();
        short shvalue[][] = new short[stvalue.length][4];
        edu.hkust.clap.monitor.Monitor.loopBegin(60);
for (int i = 0; i < stvalue.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(60);
{
            st = new StringTokenizer(stvalue[i], ".");
            int j = 0;
            edu.hkust.clap.monitor.Monitor.loopBegin(61);
while (j < 4 && st.hasMoreTokens()) { 
edu.hkust.clap.monitor.Monitor.loopInc(61);
{
                String tok = st.nextToken();
                if (tok.equals("*")) shvalue[i][j] = (short) 256; else {
                    try {
                        shvalue[i][j] = Short.parseShort(tok);
                    } catch (NumberFormatException ex) {
                        ex.printStackTrace();
                        shvalue[i][j] = (short) 0;
                    }
                }
                j++;
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(61);

        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(60);

        return shvalue;
    }

    /**
     * Set the value of the edited value
     * @param o the new value.
     */
    public void setValue(Object o) {
        this.oldshortarray = (short[][]) o;
        setSelectedItems(oldshortarray);
    }

    protected TextEditable getTextEditor() {
        return new IPTextField();
    }

    protected Dimension getPopupSize() {
        return new Dimension(350, 250);
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
        oldshortarray = (short[][]) o;
        createComponent(feeder, toStringArray(oldshortarray));
    }

    public IPTemplateArrayEditor() {
        super();
    }
}
