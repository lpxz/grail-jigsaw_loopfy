package org.w3c.tools.resources;

/**
 * The generic description of an IntegerArrayAttribute.
 */
public class IntegerArrayAttribute extends ArrayAttribute {

    /**
     * Is the given object a valid IntegerArrayAttribute value ?
     * @param obj The object to test.
     * @return A boolean <strong>true</strong> if okay.
     */
    public boolean checkValue(Object obj) {
        return (obj instanceof int[]);
    }

    public String[] pickle(Object array) {
        if (array == null) return null;
        int ints[] = (int[]) array;
        int len = ints.length;
        String strings[] = new String[len];
        edu.hkust.clap.monitor.Monitor.loopBegin(908);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(908);
{
            strings[i] = String.valueOf(ints[i]);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(908);

        return strings;
    }

    public Object unpickle(String array[]) {
        if (array.length < 1) return null;
        int len = array.length;
        int ints[] = new int[len];
        edu.hkust.clap.monitor.Monitor.loopBegin(909);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(909);
{
            ints[i] = Integer.parseInt(array[i]);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(909);

        return ints;
    }

    public IntegerArrayAttribute(String name, int def[], int flags) {
        super(name, def, flags);
        this.type = "int[]".intern();
    }

    public IntegerArrayAttribute() {
        super();
    }
}
