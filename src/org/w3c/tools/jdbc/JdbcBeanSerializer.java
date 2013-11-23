package org.w3c.tools.jdbc;

import java.beans.BeanInfo;
import java.beans.Beans;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

/**
 * Read <a href="http://www.w3.org/Jigsaw/Doc/Programmer/jspdb.html">http://www.w3.org/Jigsaw/Doc/Programmer/jspdb.html</a> 
 * to know how to use this class.
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class JdbcBeanSerializer implements PropertyChangeListener {

    /**
     * Bean modified?
     */
    private boolean modified = false;

    /**
     * Our bean.
     */
    protected JdbcBeanInterface bean = null;

    /**
     * The associated JdbcBean
     */
    private JdbcBeanInterface beans[] = null;

    /**
     * INTERSECT, UNION, EXCEPT.
     */
    protected static final int NOTHING = -1;

    protected static final int INTERSECT = 10;

    protected static final int UNION = 20;

    protected static final int EXCEPT = 30;

    protected int priority[] = { NOTHING, NOTHING, NOTHING };

    protected JdbcBeanSerializer intersect_serializer = null;

    protected JdbcBeanSerializer union_serializer = null;

    protected JdbcBeanSerializer except_serializer = null;

    /**
     * The ResultSet
     */
    protected ResultSet result = null;

    /**
     * The tables/bean used to generate the SQL request (in the correct
     * order)
     */
    private Vector beantables = null;

    /**
     * The Foreign keys <(class,class), String[]>
     */
    private static Hashtable foreignKeys = new Hashtable();

    private void registerForeignKeys(Class beanclass1, Class beanclass2) {
        Integer key = new Integer(beanclass1.hashCode() & beanclass2.hashCode());
        if (!foreignKeys.containsKey(key)) {
            foreignKeys.put(key, computeForeignKeys(beanclass1, beanclass2));
        }
    }

    private String[] getForeignKeys(Class beanclass1, Class beanclass2) {
        Integer key = new Integer(beanclass1.hashCode() & beanclass2.hashCode());
        String keys[] = (String[]) foreignKeys.get(key);
        if (keys == null) {
            keys = computeForeignKeys(beanclass1, beanclass2);
            foreignKeys.put(key, keys);
        }
        return keys;
    }

    private String[] computeForeignKeys(Class beanclass1, Class beanclass2) {
        try {
            BeanInfo bi1 = Introspector.getBeanInfo(beanclass1);
            PropertyDescriptor pds1[] = bi1.getPropertyDescriptors();
            BeanInfo bi2 = Introspector.getBeanInfo(beanclass2);
            PropertyDescriptor pds2[] = bi2.getPropertyDescriptors();
            Vector foreign = new Vector();
            edu.hkust.clap.monitor.Monitor.loopBegin(860);
for (int cpt1 = 0; cpt1 < pds1.length; cpt1++) { 
edu.hkust.clap.monitor.Monitor.loopInc(860);
{
                PropertyDescriptor pd1 = pds1[cpt1];
                edu.hkust.clap.monitor.Monitor.loopBegin(861);
for (int cpt2 = 0; cpt2 < pds2.length; cpt2++) { 
edu.hkust.clap.monitor.Monitor.loopInc(861);
{
                    PropertyDescriptor pd2 = pds2[cpt2];
                    if ((!pd2.isHidden()) && (!pd1.isHidden()) && (equalsForeignKeys(pd1.getName(), pd2.getName()))) {
                        foreign.addElement(pd1.getName());
                    }
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(861);

            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(860);

            String keys[] = new String[foreign.size()];
            foreign.copyInto(keys);
            return keys;
        } catch (IntrospectionException ex) {
            return new String[0];
        }
    }

    /**
     * toto_username == username
     */
    private static boolean equalsForeignKeys(String key1, String key2) {
        int idx1 = key1.lastIndexOf("_");
        if (idx1 != -1) {
            key1 = key1.substring(idx1);
        }
        int idx2 = key2.lastIndexOf("_");
        if (idx2 != -1) {
            key2 = key2.substring(idx2);
        }
        return key1.equals(key2);
    }

    protected void markModified(boolean modified) {
        this.modified = modified;
    }

    protected boolean isModified() {
        return modified;
    }

    /**
     * PropertyChangeListener implementation: This method gets called when
     * a bound property is changed.
     * @param evt A PropertyChangeEvent object describing the event source 
     * and the property that has changed.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        Object source = evt.getSource();
        String name = evt.getPropertyName();
        Object value = evt.getNewValue();
        if (source == bean) {
            if (value == null) {
                PropertyDescriptor pd = getPropertyDescriptor(name);
                if (JdbcBeanUtil.isJdbcBean(pd.getPropertyType())) {
                    this.beans = null;
                    JdbcBeanInterface oldbean = (JdbcBeanInterface) evt.getOldValue();
                    if (oldbean != null) {
                        PropertyCache.removeProperties(oldbean);
                        oldbean.removePropertyChangeListener(this);
                    }
                }
            } else if (value instanceof JdbcBeanInterface) {
                registerForeignKeys(bean.getClass(), value.getClass());
                this.beans = null;
                JdbcBeanInterface oldbean = (JdbcBeanInterface) evt.getOldValue();
                JdbcBeanInterface newbean = (JdbcBeanInterface) value;
                if (oldbean != null) {
                    PropertyCache.removeProperties(oldbean);
                    oldbean.removePropertyChangeListener(this);
                }
                newbean.addPropertyChangeListener(this);
            } else {
                JdbcBeanInterface bean = (JdbcBeanInterface) source;
                PropertyCache.addProperty(bean, name, evt.getNewValue());
                markModified(true);
            }
        } else {
            markModified(true);
        }
    }

    /**
     * Get the raw value of the given property, split the operator and
     * the value and convert the raw value into a SQL value.<p>
     * ie "~A.*" will become { " ~ " , "'A.*'" }
     * @param bean the property holder
     * @param pd the property descriptor
     */
    private String[] getSQLOperatorNValue(JdbcBeanInterface bean, PropertyDescriptor pd) {
        Class type = pd.getPropertyType();
        Method m = pd.getReadMethod();
        if (m != null) {
            Object value = PropertyCache.getProperty(bean, pd);
            if (value == null) {
                return null;
            }
            return SQL.getSQLOperator(value);
        }
        return null;
    }

    /**
     * Get the SQL value of the given property.
     * @param bean the property holder
     * @param pd the property descriptor
     */
    private String getSQLValue(JdbcBeanInterface bean, PropertyDescriptor pd) {
        Class type = pd.getPropertyType();
        Method m = pd.getReadMethod();
        if (m != null) {
            Object value = PropertyCache.getProperty(bean, pd);
            if (value == null) {
                return null;
            }
            return SQL.getSQLValue(value);
        }
        return null;
    }

    /**
     * @param name
     * @param value
     * @param buf
     */
    private void append(String name, String operator, String value, String separator, StringBuffer buf) {
        if (buf.length() > 0) {
            buf.append(separator).append(" ");
        }
        buf.append(name).append(operator).append(value).append(" ");
    }

    /**
     * @param name
     * @param value
     * @param namesbuffer
     * @param valuesbuffer
     */
    private void appendInsert(String name, String value, StringBuffer namesbuffer, StringBuffer valuesbuffer) {
        if (namesbuffer.length() > 0) {
            namesbuffer.append(", ").append(name);
            valuesbuffer.append(", ").append(value);
        } else {
            namesbuffer.append("(").append(name);
            valuesbuffer.append("(").append(value);
        }
    }

    private JdbcBeanInterface[] getJdbcBeans() {
        if (beans != null) {
            return beans;
        }
        try {
            BeanInfo info = Introspector.getBeanInfo(bean.getClass());
            Vector vb = new Vector();
            PropertyDescriptor pds[] = info.getPropertyDescriptors();
            edu.hkust.clap.monitor.Monitor.loopBegin(862);
for (int i = 0; i < pds.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(862);
{
                PropertyDescriptor pd = pds[i];
                if ((!pd.isHidden()) && (JdbcBeanUtil.isJdbcBean(pd.getPropertyType()))) {
                    Method m = pd.getReadMethod();
                    if (m != null) {
                        Object value = m.invoke(bean, (Object[]) null);
                        if (value != null) {
                            vb.addElement(value);
                        }
                    }
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(862);

            beans = new JdbcBeanInterface[vb.size()];
            vb.copyInto(beans);
            return beans;
        } catch (IntrospectionException ex) {
            return null;
        } catch (IllegalAccessException ex) {
            return null;
        } catch (InvocationTargetException ex) {
            return null;
        }
    }

    private void appendForeignKeys(Vector tables, StringBuffer buffer, String properties[]) throws IntrospectionException {
        BeanInfo info = Introspector.getBeanInfo(bean.getClass());
        PropertyDescriptor pds[] = info.getPropertyDescriptors();
        appendForeignKeys(tables, buffer, pds, properties);
    }

    private void appendForeignKeys(Vector tables, StringBuffer buffer, PropertyDescriptor pds[], String properties[]) throws IntrospectionException {
        JdbcBeanInterface jbeans[] = getJdbcBeans();
        if (jbeans != null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(863);
for (int i = 0; i < jbeans.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(863);
{
                JdbcBeanInterface jbean = jbeans[i];
                String keys[] = getForeignKeys(jbean.getClass(), bean.getClass());
                edu.hkust.clap.monitor.Monitor.loopBegin(864);
for (int f = 0; f < keys.length; f++) { 
edu.hkust.clap.monitor.Monitor.loopInc(864);
{
                    String key = keys[f];
                    if ((properties == null) || (JdbcBeanUtil.isIn(key, properties))) {
                        append(jbean.getJdbcTable() + "." + key, " = ", bean.getJdbcTable() + "." + key, "AND", buffer);
                    }
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(864);

                BeanInfo bi = null;
                PropertyDescriptor jpds[] = null;
                bi = Introspector.getBeanInfo(jbean.getClass());
                jpds = bi.getPropertyDescriptors();
                edu.hkust.clap.monitor.Monitor.loopBegin(865);
for (int jpd_cpt = 0; jpd_cpt < jpds.length; jpd_cpt++) { 
edu.hkust.clap.monitor.Monitor.loopInc(865);
{
                    PropertyDescriptor jpd = jpds[jpd_cpt];
                    if (jpd.isHidden()) {
                        continue;
                    }
                    String jname = jpd.getName();
                    if ((properties == null) || (JdbcBeanUtil.isIn(jname, properties))) {
                        String split[] = getSQLOperatorNValue(jbean, jpd);
                        if (split != null) {
                            append(jbean.getJdbcTable() + "." + jname, split[0], split[1], "AND", buffer);
                        }
                    }
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(865);

                tables.addElement(jbean);
                jbean.getSerializer().appendForeignKeys(tables, buffer, properties);
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(863);

        }
    }

    protected String computeSQLCount(boolean all, boolean distinct, String properties[]) {
        String count = (distinct) ? "DISTINCT count(*)" : "count(*)";
        return computeSQLSelect(all, count, properties);
    }

    private String computeSQLSelect(String orderby[], boolean asc[], boolean all) {
        return computeSQLSelect(orderby, asc, all, "*", null);
    }

    private String computeSQLSelect(String orderby[], boolean asc[], boolean all, String select) {
        return computeSQLSelect(orderby, asc, all, select, null);
    }

    protected String computeSQLSelect(String orderby[], boolean asc[], boolean all, String select, String properties[]) {
        String sql = computeSQLSelect(all, select, properties);
        StringBuffer buffer = new StringBuffer(sql);
        if (orderby != null) {
            buffer.append(" ORDER BY ");
            edu.hkust.clap.monitor.Monitor.loopBegin(866);
for (int j = 0; j < orderby.length; j++) { 
edu.hkust.clap.monitor.Monitor.loopInc(866);
{
                if (j != 0) {
                    buffer.append(", ");
                }
                buffer.append(orderby[j]);
                if (!asc[j]) {
                    buffer.append(" DESC");
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(866);

        }
        return buffer.toString();
    }

    private String computeSQLSelect(boolean all, String select, String properties[]) {
        try {
            BeanInfo info = Introspector.getBeanInfo(bean.getClass());
            StringBuffer buffer = new StringBuffer();
            String table = bean.getJdbcTable();
            this.beantables = new Vector();
            beantables.addElement(bean);
            PropertyDescriptor pds[] = info.getPropertyDescriptors();
            if (all) {
                appendForeignKeys(beantables, buffer, pds, properties);
            }
            edu.hkust.clap.monitor.Monitor.loopBegin(867);
for (int i = 0; i < pds.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(867);
{
                PropertyDescriptor pd = pds[i];
                if (!pd.isHidden()) {
                    String jname = pd.getName();
                    if ((properties == null) || (JdbcBeanUtil.isIn(jname, properties))) {
                        String split[] = getSQLOperatorNValue(bean, pd);
                        if (split != null) {
                            append(table + "." + jname, split[0], split[1], "AND", buffer);
                        }
                    }
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(867);

            if (buffer.length() > 0) {
                StringBuffer tables = new StringBuffer();
                edu.hkust.clap.monitor.Monitor.loopBegin(868);
for (int i = 0; i < beantables.size(); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(868);
{
                    JdbcBeanInterface jbean = (JdbcBeanInterface) beantables.elementAt(i);
                    if (i != 0) {
                        tables.append(", ");
                    }
                    tables.append(jbean.getJdbcTable());
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(868);

                tables.append(" WHERE ");
                tables.insert(0, "SELECT " + select + " FROM ");
                tables.append(buffer.toString());
                buffer = tables;
            } else {
                buffer = new StringBuffer("SELECT " + select + " FROM ");
                buffer.append(table);
            }
            edu.hkust.clap.monitor.Monitor.loopBegin(869);
for (int i = 0; i < priority.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(869);
{
                int p = priority[i];
                if (p == NOTHING) {
                    break;
                }
                switch(p) {
                    case INTERSECT:
                        if (intersect_serializer != null) {
                            String intersect = intersect_serializer.computeSQLSelect(all, select, properties);
                            buffer.append(" INTERSECT (").append(intersect);
                            buffer.append(")");
                        }
                        break;
                    case UNION:
                        if (union_serializer != null) {
                            String union = union_serializer.computeSQLSelect(all, select, properties);
                            buffer.append(" UNION (").append(union);
                            buffer.append(")");
                        }
                        break;
                    case EXCEPT:
                        if (except_serializer != null) {
                            String except = except_serializer.computeSQLSelect(all, select, properties);
                            buffer.append(" EXCEPT (").append(except);
                            buffer.append(")");
                        }
                        break;
                    default:
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(869);

            return buffer.toString();
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    /**
     * Compute the SQL request necessary to update the Database.
     * @return a String
     */
    protected String computeSQLInsert() {
        try {
            BeanInfo info = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor pds[] = info.getPropertyDescriptors();
            StringBuffer namesbuffer = new StringBuffer();
            StringBuffer valuesbuffer = new StringBuffer();
            edu.hkust.clap.monitor.Monitor.loopBegin(870);
for (int i = 0; i < pds.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(870);
{
                PropertyDescriptor pd = pds[i];
                if (!pd.isHidden()) {
                    String value = getSQLValue(bean, pd);
                    if (value != null) {
                        appendInsert(pd.getName(), value, namesbuffer, valuesbuffer);
                    }
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(870);

            if (namesbuffer.length() > 0) {
                StringBuffer request = new StringBuffer("INSERT INTO ");
                request.append(bean.getJdbcTable()).append(" ");
                request.append(namesbuffer).append(") ");
                request.append("VALUES ").append(valuesbuffer).append(")");
                return request.toString();
            } else {
                return null;
            }
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    protected String computeSQLDelete() {
        try {
            BeanInfo info = Introspector.getBeanInfo(bean.getClass());
            StringBuffer buffer = new StringBuffer();
            StringBuffer table = new StringBuffer(bean.getJdbcTable());
            PropertyDescriptor pds[] = info.getPropertyDescriptors();
            edu.hkust.clap.monitor.Monitor.loopBegin(871);
for (int i = 0; i < pds.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(871);
{
                PropertyDescriptor pd = pds[i];
                if (!pd.isHidden()) {
                    String split[] = getSQLOperatorNValue(bean, pd);
                    if (split != null) {
                        append(pd.getName(), split[0], split[1], "AND", buffer);
                    }
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(871);

            if (buffer.length() > 0) {
                table.append(" WHERE ");
                table.insert(0, "DELETE FROM ");
                table.append(buffer.toString());
                buffer = table;
            } else {
                return null;
            }
            return buffer.toString();
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    protected String computeSQLUpdate(String primarykeys[]) {
        try {
            BeanInfo info = Introspector.getBeanInfo(bean.getClass());
            StringBuffer buffer = new StringBuffer();
            StringBuffer pkbuffer = new StringBuffer();
            StringBuffer table = new StringBuffer(bean.getJdbcTable());
            PropertyDescriptor pds[] = info.getPropertyDescriptors();
            edu.hkust.clap.monitor.Monitor.loopBegin(872);
for (int i = 0; i < pds.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(872);
{
                PropertyDescriptor pd = pds[i];
                if (!pd.isHidden()) {
                    String name = pd.getName();
                    String split[] = getSQLOperatorNValue(bean, pd);
                    if (split != null) {
                        if (JdbcBeanUtil.isIn(name, primarykeys)) {
                            append(name, split[0], split[1], "AND", pkbuffer);
                        } else {
                            append(name, split[0], split[1], ",", buffer);
                        }
                    }
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(872);

            if (buffer.length() > 0) {
                table.append(" SET ");
                table.insert(0, "UPDATE ");
                table.append(buffer.toString());
                table.append(" WHERE ");
                table.append(pkbuffer.toString());
                buffer = table;
            } else {
                return null;
            }
            return buffer.toString();
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    protected JdbcServer getJdbcServer() {
        Properties props = new Properties();
        Jdbc.setMaxConn(props, bean.getMaxConn());
        return JdbcServer.getServer(bean.getJdbcURI(), bean.getJdbcUser(), bean.getJdbcPassword(), bean.getJdbcDriver(), props);
    }

    private void executeSQLQuery(String sqlrequest) throws SQLException {
        result = getJdbcServer().runQuery(sqlrequest, false);
    }

    private int executeSQLUpdate(String sqlrequest) throws SQLException {
        return getJdbcServer().runUpdate(sqlrequest, false);
    }

    /**
     * Count the number or row with columns matching the value of the
     * bean properties.
     * @return an int
     */
    public int count() {
        return count(true, false, null);
    }

    /**
     * Count the number or row with columns matching the value of the
     * given properties.
     * @param properties The property names
     * @return an int
     */
    public int count(String properties[]) {
        return count(true, false, properties);
    }

    /**
     * Count the number or row with columns matching the value of the
     * bean properties.
     * @param all (join with associated beans?)
     * @return an int
     */
    public int count(boolean all) {
        return count(all, false, null);
    }

    /**
     * Count the number or row with columns matching the value of the
     * bean properties
     * @param all (join with associated beans?)
     * @param distinct (SELECT DISTINCT?)
     * @return an int
     */
    public int count(boolean all, boolean distinct) {
        return count(all, distinct, null);
    }

    /**
     * Count the number or row with columns matching the value of the
     * given properties.
     * @param all (join with associated beans?)
     * @param distinct (SELECT DISTINCT?)
     * @param properties The property names
     * @return an int
     */
    public int count(boolean all, boolean distinct, String properties[]) {
        String sql = computeSQLCount(all, distinct, properties);
        try {
            executeSQLQuery(sql);
            if (result.first()) {
                return result.getInt(1);
            } else {
                return 0;
            }
        } catch (SQLException ex) {
            System.out.println("SQL STATE: " + ex.getSQLState());
            ex.printStackTrace();
            return 0;
        } finally {
            result = null;
            beantables = null;
        }
    }

    /**
     * Perform a sql select to update the beans properties.
     */
    public void select() {
        boolean array[] = { true };
        select((String[]) null, array, true, false);
    }

    /**
     * Perform a sql select to update the beans properties.
     * @param all join with attached beans? (default is true)
     */
    public void select(boolean all) {
        boolean array[] = { true };
        select((String[]) null, array, all, false);
    }

    /**
     * Perform a sql select to update the beans properties.
     * @param orderby orderby rule
     */
    public void select(String orderby) {
        String array[] = { orderby };
        boolean arrayb[] = { true };
        select(array, arrayb, true, false);
    }

    /**
     * Perform a sql select to update the beans properties.
     * @param orderby orderby rule
     * @param asc boolean if true orderby is ASC if false it it
     * DESC (relative to the orderby[] parameter)
     * @param all join with attached beans? (default is true)
     */
    public void select(String orderby, boolean asc, boolean all) {
        String array[] = { orderby };
        boolean arrayb[] = { asc };
        select(array, arrayb, all, false);
    }

    /**
     * Perform a sql select to update the beans properties.
     * @param orderby orderby rule
     * @param asc boolean if true orderby is ASC if false it it
     * DESC (relative to the orderby[] parameter)
     * @param all join with attached beans? (default is true)
     * @param distinct if true, result won't have duplicate row (default is 
     * false)
     */
    public void select(String orderby, boolean asc, boolean all, boolean distinct) {
        String array[] = { orderby };
        boolean arrayb[] = { asc };
        select(array, arrayb, all, distinct);
    }

    /**
     * Perform a sql select to update the beans properties.
     * @param orderby array of orderby rules (ASC by default)
     */
    public void select(String orderby[]) {
        boolean array[] = { true };
        select(orderby, array, true, false);
    }

    /**
     * Perform a sql select to update the beans properties.
     * @param orderby array of orderby rules
     * @param asc array of boolean if true orderby is ASC if false it it
     * DESC (relative to the orderby[] parameter)
     * @param all join with attached beans? (default is true)
     * @param distinct if true, result won't have duplicate row (default is 
     * false)
     */
    public void select(String orderby[], boolean asc[], boolean all, boolean distinct) {
        String select = (distinct) ? "DISTINCT *" : "*";
        String sql = computeSQLSelect(orderby, asc, all, select);
        try {
            executeSQLQuery(sql);
        } catch (SQLException ex) {
            System.out.println("SQL STATE: " + ex.getSQLState());
            ex.printStackTrace();
            result = null;
        }
    }

    /**
     * Perform a sql select to update the beans properties.
     * @param orderby array of orderby rules
     * @param asc array of boolean if true orderby is ASC if false it it
     * DESC (relative to the orderby[] parameter)
     * @param all join with attached beans? (default is true)
     * @param distinct if true, result won't have duplicate row (default is 
     * @param toselect array of columns name to select
     * false)
     */
    public void select(String orderby[], boolean asc[], boolean all, boolean distinct, String toselect[]) {
        String query = null;
        if (toselect != null) {
            StringBuffer buffer = new StringBuffer();
            edu.hkust.clap.monitor.Monitor.loopBegin(873);
for (int i = 0; i < toselect.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(873);
{
                if (i != 0) {
                    buffer.append(", ");
                }
                buffer.append(toselect[i]).append(" ");
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(873);

            query = buffer.toString();
        } else {
            query = "*";
        }
        String select = (distinct) ? "DISTINCT " + query : query;
        String sql = computeSQLSelect(orderby, asc, all, select);
        try {
            executeSQLQuery(sql);
        } catch (SQLException ex) {
            System.out.println("SQL STATE: " + ex.getSQLState());
            ex.printStackTrace();
            result = null;
        }
    }

    /**
     * Perform a sql select to update only the given columns. (distinct flag is
     * set as true.
     * @param column the bean property to update
     */
    public void selectDistinct(String column) {
        boolean array[] = { true };
        String order[] = { column };
        String sql = computeSQLSelect(order, array, false, "DISTINCT " + column);
        try {
            executeSQLQuery(sql);
        } catch (SQLException ex) {
            System.out.println("SQL STATE: " + ex.getSQLState());
            ex.printStackTrace();
            result = null;
        }
    }

    private void setPriority(int p) {
        int idx = 0;
        edu.hkust.clap.monitor.Monitor.loopBegin(874);
while ((idx < priority.length) && (priority[idx] != NOTHING)) { 
edu.hkust.clap.monitor.Monitor.loopInc(874);
{
            if (priority[idx] == p) {
                return;
            }
            idx++;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(874);

        priority[idx] = p;
    }

    /**
     * USE THIS METHOD ONLY BEFORE SELECT QUERIES.
     * This will produce a select query with an INTERSECT statement in it
     * using the values of the given bean.
     * @param ibean the intersect bean
     */
    public JdbcBeanSerializer intersect(JdbcBeanInterface ibean) {
        setPriority(INTERSECT);
        intersect_serializer = ibean.getSerializer();
        return intersect_serializer;
    }

    /**
     * USE THIS METHOD ONLY BEFORE QUERIES.
     * This will produce a select query with an UNION statement in it
     * using the values of the given bean.
     * @param ibean the intersect bean
     */
    public JdbcBeanSerializer union(JdbcBeanInterface ubean) {
        setPriority(UNION);
        union_serializer = ubean.getSerializer();
        return union_serializer;
    }

    /**
     * USE THIS METHOD ONLY BEFORE SELECT QUERIES.
     * This will produce a select query with an EXCEPT statement in it
     * using the values of the given bean.
     * @param ibean the intersect bean
     */
    public JdbcBeanSerializer except(JdbcBeanInterface ebean) {
        setPriority(EXCEPT);
        except_serializer = ebean.getSerializer();
        return except_serializer;
    }

    /**
     * Remove the intersect bean
     */
    public void removeIntersectBean() {
        intersect_serializer = null;
    }

    /**
     * Remove the union bean
     */
    public void removeUnionBean() {
        union_serializer = null;
    }

    /**
     * Remove the except bean
     */
    public void removeExceptBean() {
        except_serializer = null;
    }

    /**
     * Insert the current bean values in the associated table.
     * @return false if the INSERT request failed.
     */
    public boolean insert() {
        if (!isModified()) {
            return false;
        }
        JdbcBeanInterface beans[] = getJdbcBeans();
        edu.hkust.clap.monitor.Monitor.loopBegin(875);
for (int i = 0; i < beans.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(875);
{
            JdbcBeanInterface jbean = beans[i];
            JdbcBeanSerializer ser = jbean.getSerializer();
            if (ser.isModified()) {
                ser.insert();
                updateForeignKeys(jbean);
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(875);

        if (!bean.getReadOnly()) {
            String request = computeSQLInsert();
            try {
                executeSQLUpdate(request);
            } catch (SQLException ex) {
                System.err.println(ex.getMessage());
                return false;
            }
        }
        select(false);
        try {
            if (result == null) {
                return false;
            }
            if (result.first()) {
                return updateProperties(false);
            } else {
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Update the row relative to our bean.
     * @param primarykey The primary key of the SQL table
     * @return false if the UPDATE request failed.
     */
    public boolean update(String primarykey) {
        String array[] = { primarykey };
        return update(array);
    }

    /**
     * Update the row relative to our bean.
     * @param primarykey The primary key of the SQL table
     * @return false if the UPDATE request failed.
     */
    public boolean update(String primarykeys[]) {
        if (!isModified()) {
            return false;
        }
        String sql = computeSQLUpdate(primarykeys);
        try {
            int nb = executeSQLUpdate(sql);
            return (nb > 0);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Delete the row relative to the current bean.
     * @return false if the DELETE request failed.
     */
    public boolean delete() {
        if (bean.getReadOnly()) {
            return false;
        }
        String sql = computeSQLDelete();
        try {
            int nb = executeSQLUpdate(sql);
            return (nb > 0);
        } catch (SQLException ex) {
            System.out.println("SQL STATE: " + ex.getSQLState());
            ex.printStackTrace();
            result = null;
            return false;
        }
    }

    /**
     * Go to the first row
     * @return false if there is no first row
     */
    public boolean first() {
        try {
            if (result == null) {
                return false;
            }
            if (result.first()) {
                return updateProperties();
            }
        } catch (SQLException ex) {
        }
        return false;
    }

    /**
     * Update our bean with the value of the next row
     * @return false if there is no more row
     */
    public boolean next() {
        try {
            if (result == null) {
                return false;
            }
            if (result.next()) {
                return updateProperties();
            }
        } catch (SQLException ex) {
        }
        return false;
    }

    /**
     * Did we reached the last row?
     * @return true if the last row is reached
     */
    public boolean isLast() {
        try {
            if (result == null) {
                return true;
            }
            return result.isLast();
        } catch (SQLException ex) {
        }
        return true;
    }

    /**
     * Clean cached properties (relative to our bean)
     */
    public void clean() {
        result = null;
        PropertyCache.removeProperties(bean);
        markModified(false);
    }

    /**
     * Restore default value except for JdbcBean properties.
     */
    public void initBean() {
        try {
            BeanInfo info = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor pds[] = info.getPropertyDescriptors();
            edu.hkust.clap.monitor.Monitor.loopBegin(876);
for (int i = 0; i < pds.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(876);
{
                PropertyDescriptor pd = pds[i];
                if ((!pd.isHidden()) && (!JdbcBeanUtil.isJdbcBean(pd.getPropertyType()))) {
                    Method getter = pd.getReadMethod();
                    Method setter = pd.getWriteMethod();
                    Object value = null;
                    if ((getter != null) && (setter != null)) {
                        try {
                            value = getter.invoke(bean.getDefault(), (Object[]) null);
                            Object array[] = { value };
                            setter.invoke(bean, array);
                        } catch (IllegalAccessException ex) {
                            ex.printStackTrace();
                        } catch (InvocationTargetException ex) {
                            ex.printStackTrace();
                        } catch (IllegalArgumentException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(876);

            clean();
        } catch (IntrospectionException ex) {
        }
    }

    private int findColumn(Vector tables, ResultSet result, String colname) throws SQLException {
        String tablename = bean.getJdbcTable();
        ResultSetMetaData metadata = result.getMetaData();
        int cpt = 0;
        if (metadata.getTableName(1).length() > 0) {
            edu.hkust.clap.monitor.Monitor.loopBegin(877);
for (int i = 0; i < metadata.getColumnCount(); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(877);
{
                String coltable = metadata.getTableName(i);
                if ((metadata.getTableName(i).equalsIgnoreCase(tablename)) && (metadata.getColumnName(i).equalsIgnoreCase(colname))) {
                    return i;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(877);

        } else {
            Vector indexes = new Vector();
            try {
                edu.hkust.clap.monitor.Monitor.loopBegin(878);
for (int i = 1; i <= metadata.getColumnCount(); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(878);
{
                    if (metadata.getColumnName(i).equals(colname)) {
                        indexes.addElement(new Integer(i));
                    }
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(878);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (indexes.size() == 0) {
                return -1;
            } else if (indexes.size() == 1) {
                return ((Integer) indexes.elementAt(0)).intValue();
            } else {
                int idxidx = 0;
                edu.hkust.clap.monitor.Monitor.loopBegin(879);
for (int i = 0; i < tables.size(); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(879);
{
                    JdbcBeanInterface jbean = (JdbcBeanInterface) tables.elementAt(i);
                    if (jbean == bean) {
                        return ((Integer) indexes.elementAt(idxidx)).intValue();
                    }
                    if (jbean.getSerializer().getPropertyDescriptor(colname) != null) {
                        idxidx++;
                    }
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(879);

            }
        }
        return -1;
    }

    private boolean updateProperties() {
        return updateProperties(this.beantables, this.result, true);
    }

    private boolean updateProperties(boolean all) {
        return updateProperties(this.beantables, this.result, all);
    }

    private boolean updateProperties(Vector tables, ResultSet result) {
        return updateProperties(tables, result, true);
    }

    private boolean updateProperties(Vector tables, ResultSet result, boolean all) {
        try {
            BeanInfo info = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor pds[] = info.getPropertyDescriptors();
            edu.hkust.clap.monitor.Monitor.loopBegin(880);
for (int i = 0; i < pds.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(880);
{
                PropertyDescriptor pd = pds[i];
                if (!pd.isHidden()) {
                    try {
                        int idx = findColumn(tables, result, pd.getName());
                        if (idx != -1) {
                            Object value = result.getObject(idx);
                            Class propertyclass = pd.getPropertyType();
                            value = SQL.getMatchingValue(propertyclass, value);
                            if (value != null) {
                                Object values[] = { value };
                                Method setter = pd.getWriteMethod();
                                if (setter != null) {
                                    try {
                                        setter.invoke(bean, values);
                                    } catch (IllegalAccessException ex) {
                                        ex.printStackTrace();
                                    } catch (InvocationTargetException ex) {
                                        ex.printStackTrace();
                                    } catch (IllegalArgumentException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            } else {
                                Method getter = pd.getReadMethod();
                                Method setter = pd.getWriteMethod();
                                if ((getter != null) && (setter != null)) {
                                    try {
                                        value = getter.invoke(bean.getDefault(), (Object[]) null);
                                        Object array[] = { value };
                                        setter.invoke(bean, array);
                                    } catch (IllegalAccessException ex) {
                                        ex.printStackTrace();
                                    } catch (InvocationTargetException ex) {
                                        ex.printStackTrace();
                                    } catch (IllegalArgumentException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        }
                    } catch (SQLException ex) {
                    }
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(880);

            if (all) {
                JdbcBeanInterface beans[] = getJdbcBeans();
                edu.hkust.clap.monitor.Monitor.loopBegin(881);
for (int i = 0; i < beans.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(881);
{
                    beans[i].getSerializer().updateProperties(tables, result);
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(881);

            }
            markModified(false);
            return true;
        } catch (IntrospectionException ex) {
            return false;
        }
    }

    /**
     * Update our bean property with the given bean property 
     * (must be an instance of the same class).
     * @param ubean the bean to get new properties
     */
    public void updateProperties(JdbcBeanInterface ubean) {
        if (ubean.getClass() != bean.getClass()) {
            return;
        }
        try {
            BeanInfo bi = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor pds[] = bi.getPropertyDescriptors();
            edu.hkust.clap.monitor.Monitor.loopBegin(882);
for (int i = 0; i < pds.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(882);
{
                PropertyDescriptor pd = pds[i];
                if (!pd.isHidden()) {
                    try {
                        Method reader = pd.getReadMethod();
                        Method writer = pd.getWriteMethod();
                        Object value = reader.invoke(ubean, (Object[]) null);
                        if (value != null) {
                            Object array[] = { value };
                            writer.invoke(bean, array);
                        }
                    } catch (IllegalAccessException ex) {
                        ex.printStackTrace();
                    } catch (InvocationTargetException ex) {
                        ex.printStackTrace();
                    }
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(882);

        } catch (IntrospectionException ex) {
        }
    }

    private PropertyDescriptor getPropertyDescriptor(String property) {
        try {
            BeanInfo bi = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor pds[] = bi.getPropertyDescriptors();
            edu.hkust.clap.monitor.Monitor.loopBegin(883);
for (int i = 0; i < pds.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(883);
{
                PropertyDescriptor pd = pds[i];
                if (pd.getName().equals(property)) {
                    return pd;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(883);

            return null;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private void updateForeignKeys(JdbcBeanInterface jbean) {
        String keys[] = getForeignKeys(jbean.getClass(), bean.getClass());
        JdbcBeanSerializer ser = jbean.getSerializer();
        edu.hkust.clap.monitor.Monitor.loopBegin(884);
for (int i = 0; i < keys.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(884);
{
            try {
                String key = keys[i];
                PropertyDescriptor wkeypd = getPropertyDescriptor(key);
                PropertyDescriptor rkeypd = ser.getPropertyDescriptor(key);
                Method reader = rkeypd.getReadMethod();
                Method writer = wkeypd.getWriteMethod();
                Object value = reader.invoke(jbean, (Object[]) null);
                if (value != null) {
                    Object array[] = { value };
                    writer.invoke(bean, array);
                }
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            } catch (InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(884);

    }

    /**
     * Called by the Garbage Collector.
     */
    protected void finalize() throws Throwable {
        PropertyCache.removeProperties(this.bean);
    }

    /**
     * Constructor
     * @param bean the JdbcBean to serialize
     */
    public JdbcBeanSerializer(JdbcBeanInterface bean) {
        this.bean = bean;
        bean.addPropertyChangeListener(this);
    }
}
