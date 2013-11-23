package org.w3c.jigadm.editors;

import org.w3c.jigadm.RemoteResourceWrapper;
import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class IndexFeeder implements EditorFeeder {

    public static final String FILTER_P = "feeder.filter";

    public static final String FORBID_RULE = "*forbid*";

    String[] sdefault = { FORBID_RULE };

    String[] extentions = null;

    String[] s = null;

    public String[] getDefaultItems() {
        return s;
    }

    protected boolean match(String s, String[] extensions) {
        edu.hkust.clap.monitor.Monitor.loopBegin(983);
for (int i = 0; i < extensions.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(983);
{
            if (s.endsWith("." + extensions[i])) return true;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(983);

        return false;
    }

    protected String[] filter(String[] children, String[] extensions) {
        if (extensions == null) return children;
        Vector V = new Vector(12);
        V.addElement(FORBID_RULE);
        edu.hkust.clap.monitor.Monitor.loopBegin(984);
for (int i = 0; i < children.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(984);
{
            if (match(children[i], extensions)) V.addElement(children[i]);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(984);

        String filtered[] = new String[V.size()];
        V.copyInto(filtered);
        return filtered;
    }

    protected RemoteResource getResource(RemoteResourceWrapper rrw, Properties p) {
        return rrw.getResource();
    }

    protected String[] getStringArray(RemoteResourceWrapper rrw, Properties p) {
        String extensions[] = null;
        String exts = (String) p.get(FILTER_P);
        if (exts != null) {
            StringTokenizer st = new StringTokenizer(exts, "|");
            int len = st.countTokens();
            extensions = new String[len];
            edu.hkust.clap.monitor.Monitor.loopBegin(985);
for (int i = 0; i < extensions.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(985);
{
                extensions[i] = st.nextToken();
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(985);

        }
        try {
            RemoteResource rm = getResource(rrw, p);
            if (rm.isContainer()) {
                return filter(rm.enumerateResourceIdentifiers(), extensions);
            } else if (rm.isFrame()) {
                rm = rrw.getFatherResource();
                if ((rm != null) && (rm.isContainer())) return filter(rm.enumerateResourceIdentifiers(), extensions);
            }
        } catch (RemoteAccessException ex) {
        }
        return new String[0];
    }

    public void initialize(RemoteResourceWrapper rrw, Properties p) {
        s = getStringArray(rrw, p);
        if (s.length == 0) s = sdefault;
    }
}
