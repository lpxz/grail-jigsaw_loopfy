package org.w3c.tools.resources.upgrade;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The generic description of an StringArrayAttribute.
 */
public class StringArrayAttribute extends Attribute {

    /**
     * Turn a StringArray attribute into a String.
     * We use the <em>normal</em> property convention, which is to separate
     * each item with a <strong>|</strong>.
     * @return A String based encoding for that value.
     */
    public String stringify(Object value) {
        if ((value == null) || (!(value instanceof String[]))) return null;
        String as[] = (String[]) value;
        StringBuffer sb = new StringBuffer();
        edu.hkust.clap.monitor.Monitor.loopBegin(926);
for (int i = 0; i < as.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(926);
{
            if (i > 0) sb.append('|');
            sb.append(as[i]);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(926);

        return sb.toString();
    }

    /**
     * Is the given object a valid StringArrayAttribute value ?
     * @param obj The object to test.
     * @return A boolean <strong>true</strong> if okay.
     */
    public boolean checkValue(Object obj) {
        return (obj instanceof String[]);
    }

    /**
     * Get the number of bytes required to save that attribute value.
     * @param The value about to be pickled.
     * @return The number of bytes needed to pickle that value.
     */
    public int getPickleLength(Object value) {
        String strs[] = (String[]) value;
        int sz = 4;
        edu.hkust.clap.monitor.Monitor.loopBegin(927);
for (int n = 0; n < strs.length; n++) { 
edu.hkust.clap.monitor.Monitor.loopInc(927);
{
            String str = strs[n];
            int strlen = str.length();
            int utflen = 0;
            edu.hkust.clap.monitor.Monitor.loopBegin(274);
for (int i = 0; i < strlen; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(274);
{
                int c = str.charAt(i);
                if ((c >= 0x0001) && (c <= 0x007F)) {
                    utflen++;
                } else if (c > 0x07FF) {
                    utflen += 3;
                } else {
                    utflen += 2;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(274);

            sz += (utflen + 2);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(927);

        return sz;
    }

    /**
     * Pickle a String array to the given output stream.
     * @param out The output stream to pickle to.
     * @param obj The object to pickle.
     * @exception IOException If some IO error occured.
     */
    public void pickle(DataOutputStream out, Object sa) throws IOException {
        String strs[] = (String[]) sa;
        out.writeInt(strs.length);
        edu.hkust.clap.monitor.Monitor.loopBegin(928);
for (int i = 0; i < strs.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(928);
out.writeUTF(strs[i]);} 
edu.hkust.clap.monitor.Monitor.loopEnd(928);

    }

    /**
     * Unpickle an String array from the given input stream.
     * @param in The input stream to unpickle from.
     * @return An instance of String[].
     * @exception IOException If some IO error occured.
     */
    public Object unpickle(DataInputStream in) throws IOException {
        int cnt = in.readInt();
        String strs[] = new String[cnt];
        edu.hkust.clap.monitor.Monitor.loopBegin(929);
for (int i = 0; i < cnt; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(929);
strs[i] = in.readUTF();} 
edu.hkust.clap.monitor.Monitor.loopEnd(929);

        return strs;
    }

    public StringArrayAttribute(String name, String def[], Integer flags) {
        super(name, def, flags);
        this.type = "java.lang.String[]";
    }
}
