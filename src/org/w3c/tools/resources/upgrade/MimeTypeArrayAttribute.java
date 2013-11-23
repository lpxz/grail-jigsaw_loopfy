package org.w3c.tools.resources.upgrade;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.w3c.www.mime.MimeType;
import org.w3c.www.mime.MimeTypeFormatException;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class MimeTypeArrayAttribute extends StringArrayAttribute {

    public static String[] toStringArray(Object array) {
        if (array == null) return null;
        if (array instanceof String[]) return (String[]) array; else if (array instanceof MimeType[]) {
            MimeType mimes[] = (MimeType[]) array;
            String strArray[] = new String[mimes.length];
            edu.hkust.clap.monitor.Monitor.loopBegin(244);
for (int i = 0; i < mimes.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(244);
strArray[i] = mimes[i].toString();} 
edu.hkust.clap.monitor.Monitor.loopEnd(244);

            return strArray;
        } else return null;
    }

    /**
     * Is the given object a valid MimeTypeArrayAttribute value ?
     * @param obj The object to test.
     * @return A boolean <strong>true</strong> if okay.
     */
    public boolean checkValue(Object obj) {
        return ((obj instanceof MimeType[]) || (obj instanceof String[]));
    }

    /**
     * Get the number of bytes required to save that attribute value.
     * @param The value about to be pickled.
     * @return The number of bytes needed to pickle that value.
     */
    public int getPickleLength(Object value) {
        return super.getPickleLength(toStringArray(value));
    }

    /**
     * Pickle a String array to the given output stream.
     * @param out The output stream to pickle to.
     * @param obj The object to pickle.
     * @exception IOException If some IO error occured.
     */
    public void pickle(DataOutputStream out, Object sa) throws IOException {
        super.pickle(out, toStringArray(sa));
    }

    /**
     * Unpickle an MimeType array from the given input stream.
     * @param in The input stream to unpickle from.
     * @return An instance of MimeType[].
     * @exception IOException If some IO error occured.
     */
    public Object unpickle(DataInputStream in) throws IOException {
        int cnt = in.readInt();
        MimeType mimes[] = new MimeType[cnt];
        edu.hkust.clap.monitor.Monitor.loopBegin(930);
for (int i = 0; i < cnt; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(930);
{
            try {
                mimes[i] = new MimeType(in.readUTF());
            } catch (MimeTypeFormatException ex) {
                mimes[i] = null;
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(930);

        return mimes;
    }

    public MimeTypeArrayAttribute(String name, MimeType def[], Integer flags) {
        super(name, toStringArray(def), flags);
        this.type = "[Lorg.w3c.www.mime.MimeType;";
    }
}
