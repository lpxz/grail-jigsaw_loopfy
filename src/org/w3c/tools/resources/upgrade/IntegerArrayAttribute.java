package org.w3c.tools.resources.upgrade;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The generic description of an IntegerArrayAttribute.
 */
public class IntegerArrayAttribute extends Attribute {

    /**
     * Turn a IntegerArray attribute into a String.
     * We use the <em>normal</em> property convention, which is to separate
     * each item with a <strong>|</strong>.
     * @return A String based encoding for that value.
     */
    public String stringify(Object value) {
        if ((value == null) || (!(value instanceof int[]))) return null;
        int ai[] = (int[]) value;
        StringBuffer sb = new StringBuffer();
        edu.hkust.clap.monitor.Monitor.loopBegin(768);
for (int i = 0; i < ai.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(768);
{
            if (i > 0) sb.append('|');
            sb.append(ai[i]);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(768);

        return sb.toString();
    }

    /**
     * Is the given object a valid IntegerArrayAttribute value ?
     * @param obj The object to test.
     * @return A boolean <strong>true</strong> if okay.
     */
    public boolean checkValue(Object obj) {
        return (obj instanceof int[]);
    }

    /**
     * Get the number of bytes required to save that attribute value.
     * @param The value about to be pickled.
     * @return The number of bytes needed to pickle that value.
     */
    public final int getPickleLength(Object value) {
        return ((int[]) value).length * 4 + 4;
    }

    /**
     * Pickle a integer array to the given output stream.
     * @param out The output stream to pickle to.
     * @param obj The object to pickle.
     * @exception IOException If some IO error occured.
     */
    public void pickle(DataOutputStream out, Object ia) throws IOException {
        int is[] = (int[]) ia;
        out.writeInt(is.length);
        edu.hkust.clap.monitor.Monitor.loopBegin(769);
for (int i = 0; i < is.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(769);
out.writeInt(is[i]);} 
edu.hkust.clap.monitor.Monitor.loopEnd(769);

    }

    /**
     * Unpickle an integer array from the given input stream.
     * @param in The input stream to unpickle from.
     * @return An instance of int[].
     * @exception IOException If some IO error occured.
     */
    public Object unpickle(DataInputStream in) throws IOException {
        int cnt = in.readInt();
        int is[] = new int[cnt];
        edu.hkust.clap.monitor.Monitor.loopBegin(770);
for (int i = 0; i < cnt; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(770);
is[i] = in.readInt();} 
edu.hkust.clap.monitor.Monitor.loopEnd(770);

        return is;
    }

    public IntegerArrayAttribute(String name, String def[], Integer flags) {
        super(name, def, flags);
        this.type = "int[]";
    }
}
