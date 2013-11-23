package org.w3c.tools.resources.upgrade;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The IPTemplates attribute description.
 * Maintains a list of IP templates (short arrays, to allow for the splash).
 */
public class IPTemplatesAttribute extends Attribute {

    /**
     * Is the given value a valid IPTemplates value ?
     * @param obj The object to test.
     * @exception IllegalAttributeAccess If the provided value doesn't pass the
     *    test.
     */
    public boolean checkValue(Object obj) {
        return ((obj == null) || (obj instanceof short[][]));
    }

    /**
     * Get the number of bytes required to save that attribute value.
     * @param The value about to be pickled.
     * @return The number of bytes needed to pickle that value.
     */
    public final int getPickleLength(Object value) {
        return ((short[][]) value).length * 2 + 4;
    }

    /**
     * Pickle an array of IP templates  to the given output stream.
     * @param out The output stream to pickle to.
     * @param obj The object to pickle.
     * @exception IOException If some IO error occured.
     */
    public void pickle(DataOutputStream out, Object obj) throws IOException {
        short ips[][] = (short[][]) obj;
        out.writeInt(ips.length);
        edu.hkust.clap.monitor.Monitor.loopBegin(77);
for (int i = 0; i < ips.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(77);
{
            out.writeShort(ips[i][0]);
            out.writeShort(ips[i][1]);
            out.writeShort(ips[i][2]);
            out.writeShort(ips[i][3]);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(77);

    }

    /**
     * Unpickle an array of IP templates from the given input stream.
     * @param in The input stream to unpickle from.
     * @return An instance of short[][].
     * @exception IOException If some IO error occured.
     */
    public Object unpickle(DataInputStream in) throws IOException {
        int cnt = in.readInt();
        short ips[][] = new short[cnt][];
        edu.hkust.clap.monitor.Monitor.loopBegin(78);
for (int i = 0; i < cnt; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(78);
{
            ips[i] = new short[4];
            ips[i][0] = in.readShort();
            ips[i][1] = in.readShort();
            ips[i][2] = in.readShort();
            ips[i][3] = in.readShort();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(78);

        return ips;
    }

    public IPTemplatesAttribute(String name, short defs[][], Integer flags) {
        super(name, defs, flags);
        this.type = "[[S";
    }
}
