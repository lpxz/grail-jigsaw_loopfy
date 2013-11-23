package org.w3c.util;

import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.io.File;

/**
 * An enhanced property class that provides support to monitor changes.
 * This class extends the basic properties class of Java, by providing
 * monitoring support. It also provides more type conversion.
 * @see PropertyMonitoring
 */
public class ObservableProperties extends Properties {

    private static final boolean debug = false;

    PropertyMonitoring observers[] = null;

    int observers_count = 0;

    /**
     * Subscribe for property monitoring.
     * @param observer The object that handles the PropertyMonitoring 
     *    interface.
     */
    public synchronized void registerObserver(PropertyMonitoring o) {
        edu.hkust.clap.monitor.Monitor.loopBegin(933);
for (int i = 0; i < observers.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(933);
{
            if (observers[i] == null) {
                observers[i] = o;
                return;
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(933);

        if (observers_count + 1 >= observers.length) {
            PropertyMonitoring m[] = new PropertyMonitoring[observers.length * 2];
            System.arraycopy(observers, 0, m, 0, observers.length);
            observers = m;
        }
        observers[observers_count++] = o;
    }

    /**
     * Unsubscribe this object from the observers list.
     * @param observer The observer to unsubscribe.
     * @return A boolean <strong>true</strong> if object was succesfully 
     *     unsubscribed, <strong>false</strong> otherwise.
     */
    public synchronized boolean unregisterObserver(PropertyMonitoring o) {
        edu.hkust.clap.monitor.Monitor.loopBegin(934);
for (int i = 0; i < observers.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(934);
{
            if (observers[i] == o) {
                observers[i] = null;
                return true;
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(934);

        return false;
    }

    /**
     * Update a property value.
     * Assign a value to a property. If the property value has really changed
     * notify our observers of the change.
     * @param name The name of the property to assign.
     * @param value The new value for this property, or <strong>null</strong>
     *    if the property setting is to be cancelled.
     * @return A boolean <strong>true</strong> if change was accepted by 
     *    our observers, <strong>false</strong> otherwise.
     */
    public synchronized boolean putValue(String name, String value) {
        if (debug) System.out.println("ObservableProperties: put " + name + "=[" + value + "]");
        if (value == null) {
            super.remove(name);
            return true;
        }
        String old = (String) get(name);
        if ((old == null) || (!old.equals(value))) {
            super.put(name, value);
            edu.hkust.clap.monitor.Monitor.loopBegin(935);
for (int i = 0; i < observers.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(935);
{
                if (observers[i] == null) continue;
                if (debug) System.out.println("ObservableProperties: notifies " + observers[i]);
                if (!observers[i].propertyChanged(name)) {
                    if (old != null) super.put(name, old);
                    return false;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(935);

        }
        return true;
    }

    /**
     * Get this property value, as a boolean.
     * @param name The name of the property to be fetched.
     * @param def The default value, if the property isn't defined.
     * @return A Boolean instance.
     */
    public boolean getBoolean(String name, boolean def) {
        String v = getProperty(name, null);
        if (v != null) return "true".equalsIgnoreCase(v) ? true : false;
        return def;
    }

    /**
     * Get this property value, as a String.
     * @param name The name of the property to be fetched.
     * @param def The default value, if the property isn't defined.
     * @return An instance of String.
     */
    public String getString(String name, String def) {
        String v = getProperty(name, null);
        if (v != null) return v;
        return def;
    }

    /**
     * Get this property as a String array.
     * By convention, properties that are get as string arrays should be
     * encoded as a <strong>|</strong> separated list of Strings.
     * @param name The property's name.
     * @param def The default value (if undefined).
     * @return A String array, or <strong>null</strong> if the property
     * is undefined.
     */
    public String[] getStringArray(String name, String def[]) {
        String v = getProperty(name, null);
        if (v == null) return def;
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

    /**
     * Get this property value, as an integer.
     * @param name The name of the property to be fetched.
     * @param def The default value, if the property isn't defined.
     * @return An integer value.
     */
    public int getInteger(String name, int def) {
        String v = getProperty(name, null);
        if (v != null) {
            try {
                if (v.startsWith("0x")) {
                    return Integer.valueOf(v.substring(2), 16).intValue();
                }
                if (v.startsWith("#")) {
                    return Integer.valueOf(v.substring(1), 16).intValue();
                }
                return Integer.valueOf(v).intValue();
            } catch (NumberFormatException e) {
            }
        }
        return def;
    }

    public long getLong(String name, long def) {
        String v = getProperty(name, null);
        if (v != null) {
            try {
                return Long.valueOf(v).longValue();
            } catch (NumberFormatException e) {
            }
        }
        return def;
    }

    /**
     * Get this property value, as a double.
     * @param name The name of the property.
     * @param def The default value if undefined.
     * @return A double value.
     */
    public double getDouble(String name, double def) {
        String v = getProperty(name, null);
        if (v != null) {
            try {
                return Double.valueOf(v).doubleValue();
            } catch (NumberFormatException ex) {
            }
        }
        return def;
    }

    /**
     * Get this property value, as a File.
     * @param name The name of the property to be fetched.
     * @param def The default value, if the property isn't defined.
     * @return An instance of File.
     */
    public File getFile(String name, File def) {
        String v = getProperty(name, null);
        if (v != null) return new File(v);
        return def;
    }

    /**
     * Build an httpdProperties instance from a Properties instance.
     * @param props The Properties instance.
     */
    public ObservableProperties(Properties props) {
        super(props);
        this.observers = new PropertyMonitoring[5];
        this.observers_count = 0;
    }
}
