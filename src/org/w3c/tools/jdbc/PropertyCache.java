package org.w3c.tools.jdbc;

import java.util.Hashtable;
import java.util.Enumeration;
import java.beans.PropertyDescriptor;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class PropertyCache {

    public static boolean debug = false;

    /**
     * The modified properties
     */
    protected static Hashtable properties = new Hashtable();

    protected static String getId(JdbcBeanInterface bean, String property) {
        StringBuffer buffer = new StringBuffer(String.valueOf(bean.hashCode()));
        buffer.append(".").append(property);
        return buffer.toString();
    }

    public static void addProperty(JdbcBeanInterface bean, String property, Object value) {
        String id = getId(bean, property);
        if (id != null) {
            if (debug) {
                System.out.println("add property in cache: " + id + " = " + value);
            }
            properties.put(id, value);
        }
    }

    public static Object getProperty(JdbcBeanInterface bean, PropertyDescriptor pd) {
        String id = getId(bean, pd.getName());
        if (id != null) {
            return properties.get(id);
        }
        return null;
    }

    public static void removeProperties(JdbcBeanInterface bean) {
        Enumeration keys = properties.keys();
        StringBuffer buffer = new StringBuffer(String.valueOf(bean.hashCode()));
        buffer.append(".");
        String beankey = buffer.toString();
        edu.hkust.clap.monitor.Monitor.loopBegin(821);
while (keys.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(821);
{
            String key = (String) keys.nextElement();
            if (key.startsWith(beankey)) {
                if (debug) {
                    System.out.println("remove property from cache: " + beankey);
                }
                properties.remove(key);
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(821);

    }
}
