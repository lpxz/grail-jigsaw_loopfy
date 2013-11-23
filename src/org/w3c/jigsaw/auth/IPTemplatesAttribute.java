package org.w3c.jigsaw.auth;

import org.w3c.tools.resources.ArrayAttribute;
import org.w3c.tools.resources.Attribute;

/**
 * The IPTemplates attribute description.
 * Maintains a list of IP templates (short arrays, to allow for the splash).
 */
public class IPTemplatesAttribute extends ArrayAttribute {

    /**
     * Is the given value a valid IPTemplates value ?
     * @param obj The object to test.
     * @exception IllegalAttributeAccess If the provided value doesn't pass the
     *    test.
     */
    public boolean checkValue(Object obj) {
        return ((obj == null) || (obj instanceof short[][]));
    }

    public String[] pickle(Object array) {
        short ips[][] = (short[][]) array;
        String str[] = new String[ips.length];
        edu.hkust.clap.monitor.Monitor.loopBegin(745);
for (int i = 0; i < ips.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(745);
{
            str[i] = ips[i][0] + "." + ips[i][1] + "." + ips[i][2] + "." + ips[i][3];
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(745);

        return str;
    }

    public Object unpickle(String array[]) {
        if (array.length < 1) return null;
        short ips[][] = new short[array.length][4];
        edu.hkust.clap.monitor.Monitor.loopBegin(746);
for (int i = 0; i < array.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(746);
{
            String ip = array[i];
            int idx1;
            int idx2;
            idx1 = ip.indexOf('.');
            ips[i][0] = Short.parseShort(ip.substring(0, idx1));
            idx1++;
            idx2 = ip.indexOf('.', idx1);
            ips[i][1] = Short.parseShort(ip.substring(idx1, idx2));
            idx1 = ++idx2;
            idx2 = ip.indexOf('.', idx1);
            ips[i][2] = Short.parseShort(ip.substring(idx1, idx2));
            idx1 = ++idx2;
            ips[i][3] = Short.parseShort(ip.substring(idx1));
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(746);

        return ips;
    }

    public IPTemplatesAttribute(String name, short defs[][], int flags) {
        super(name, defs, flags);
        this.type = "[[S".intern();
    }

    public IPTemplatesAttribute() {
        super();
        this.type = "[[S".intern();
    }
}
