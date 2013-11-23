package org.w3c.tools.resources;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public abstract class ArrayAttribute extends Attribute {

    /**
     * Unpickle an attribute array from a string array.
     * @param array the String array
     * @return a Object array
     */
    public abstract Object unpickle(String array[]);

    /**
     * Pickle an attribute array into a String array.
     * @param array the attribute array
     * @return a String array
     */
    public abstract String[] pickle(Object array);

    public String stringify(Object value) {
        String array[] = pickle(value);
        String string = "";
        edu.hkust.clap.monitor.Monitor.loopBegin(567);
for (int i = 0; i < array.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(567);
{
            if (i != 0) string += " | " + array[i]; else string = array[i];
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(567);

        return string;
    }

    public ArrayAttribute(String name, Object def, int flags) {
        super(name, def, flags);
    }

    public ArrayAttribute() {
        super();
    }
}
