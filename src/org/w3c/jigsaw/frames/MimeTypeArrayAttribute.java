package org.w3c.jigsaw.frames;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.w3c.tools.resources.StringArrayAttribute;
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

    public String[] pickle(Object array) {
        return toStringArray(array);
    }

    public Object unpickle(String array[]) {
        int cnt = array.length;
        if (cnt < 1) return null;
        MimeType mimes[] = new MimeType[cnt];
        edu.hkust.clap.monitor.Monitor.loopBegin(245);
for (int i = 0; i < cnt; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(245);
{
            try {
                mimes[i] = new MimeType(array[i]);
            } catch (MimeTypeFormatException ex) {
                mimes[i] = null;
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(245);

        return mimes;
    }

    public MimeTypeArrayAttribute(String name, MimeType def[], int flags) {
        super(name, toStringArray(def), flags);
        this.type = "[Lorg.w3c.www.mime.MimeType;".intern();
    }

    public MimeTypeArrayAttribute() {
        super();
        this.type = "[Lorg.w3c.www.mime.MimeType;".intern();
    }
}
