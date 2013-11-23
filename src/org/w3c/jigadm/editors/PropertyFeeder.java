package org.w3c.jigadm.editors;

import org.w3c.jigadm.RemoteResourceWrapper;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

public class PropertyFeeder implements EditorFeeder {

    public static final String FEEDER_DATA_P = "feeder.data";

    String[] s = null;

    public String[] getDefaultItems() {
        return s;
    }

    protected String[] getStringArray(Properties p, String name) {
        String v = (String) p.get(name);
        if (v == null) return new String[0];
        StringTokenizer st = new StringTokenizer(v, "|");
        int len = st.countTokens();
        String ret[] = new String[len];
        edu.hkust.clap.monitor.Monitor.loopBegin(16);
for (int i = 0; i < ret.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(16);
{
            ret[i] = st.nextToken();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(16);

        return ret;
    }

    public void initialize(RemoteResourceWrapper rrw, Properties p) {
        s = getStringArray(p, FEEDER_DATA_P);
    }
}
