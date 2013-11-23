package org.w3c.tools.jdbc;

import java.beans.BeanInfo;
import java.beans.SimpleBeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class JdbcBeanInterfaceBeanInfo extends SimpleBeanInfo {

    private PropertyDescriptor getPropertyDescriptor(String name, boolean hidden) throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor(name, JdbcBeanInterface.class);
        pd.setHidden(hidden);
        return pd;
    }

    private PropertyDescriptor getPropertyDescriptor(String name, String getter, String setter, boolean hidden) throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor(name, JdbcBeanInterface.class, getter, setter);
        pd.setHidden(hidden);
        return pd;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor[] pds = { getPropertyDescriptor("jdbcDriver", true), getPropertyDescriptor("jdbcUser", true), getPropertyDescriptor("jdbcPassword", true), getPropertyDescriptor("jdbcURI", true), getPropertyDescriptor("jdbcTable", true), getPropertyDescriptor("maxConn", true), getPropertyDescriptor("readOnly", true), getPropertyDescriptor("serializer", "getSerializer", null, true) };
            return pds;
        } catch (IntrospectionException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
