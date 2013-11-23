package org.w3c.tools.resources.upgrade;

import java.util.Hashtable;
import java.util.Vector;

class ClassAttributes {

    Attribute fixed[] = null;

    Vector attrs = null;

    int nextid = 0;

    /**
     * Fix the description of the attributes for this record.
     */
    void fix() {
        if (fixed == null) {
            fixed = new Attribute[attrs.size()];
            attrs.copyInto(fixed);
            attrs = null;
        }
    }

    /**
     * Add a new attribute description into this record.
     * @param attr The new attribute description.
     * @return The attribute index.
     */
    int add(Attribute attr) {
        if (attrs == null) throw new RuntimeException("add in a fixed record.");
        attrs.addElement(attr);
        return nextid++;
    }

    ClassAttributes(int idx) {
        this.fixed = null;
        this.attrs = new Vector();
        this.nextid = idx;
    }

    ClassAttributes(ClassAttributes sup) {
        this.fixed = null;
        this.attrs = new Vector(sup.fixed.length);
        this.nextid = sup.nextid;
        edu.hkust.clap.monitor.Monitor.loopBegin(589);
for (int i = 0; i < sup.fixed.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(589);
attrs.addElement(sup.fixed[i]);} 
edu.hkust.clap.monitor.Monitor.loopEnd(589);

    }
}

public class AttributeRegistry {

    private static Hashtable registery = new Hashtable();

    private static Class top = null;

    static {
        try {
            top = Class.forName("org.w3c.tools.resources.AttributeHolder");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Register a new attribute for a given class.
     * This method create the approrpriate attribute description record if
     * required, and return the index of this attribute in the corresponding
     * holder instances.
     * @param cls The class that defines this attribute.
     * @param attr The attribute to declare.
     * @return The attribute index.
     */
    public static synchronized int registerAttribute(Class cls, Attribute attr) {
        ClassAttributes record = (ClassAttributes) registery.get(cls);
        if (record == null) {
            if (cls == top) {
                record = new ClassAttributes(0);
            } else {
                edu.hkust.clap.monitor.Monitor.loopBegin(590);
for (Class clsptr = cls.getSuperclass(); (record == null); clsptr = clsptr.getSuperclass()) { 
edu.hkust.clap.monitor.Monitor.loopInc(590);
{
                    record = (ClassAttributes) registery.get(clsptr);
                    if (clsptr == top) {
                        if (record == null) record = new ClassAttributes(0);
                        break;
                    }
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(590);

                if (record == null) throw new RuntimeException("inconsistent state.");
                record.fix();
                record = new ClassAttributes(record);
            }
            registery.put(cls, record);
        }
        return record.add(attr);
    }

    /**
     * Get this class declared attributes.
     * @param cls The class we are querying.
     * @return An array of Attribute instances, describing each of the 
     *    attributes of all instances of the class, or <strong>null</strong>
     *    if the class hasn't defined any attributes.
     */
    public static synchronized Attribute[] getClassAttributes(Class cls) {
        Object result = registery.get(cls);
        edu.hkust.clap.monitor.Monitor.loopBegin(591);
while ((cls != top) && (result == null)) { 
edu.hkust.clap.monitor.Monitor.loopInc(591);
{
            cls = cls.getSuperclass();
            result = registery.get(cls);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(591);

        if (result == null) return null;
        ClassAttributes record = (ClassAttributes) result;
        record.fix();
        return record.fixed;
    }

    /**
     * Get the name of the class that has declared this attribute.
     * @param cls The class that makes the starting point of lookup.
     * @param attr The attribute we are looking for.
     * @return The name of the class that defined that attribute, or <strong>
     * null</strong>.
     */
    public static Class getAttributeClass(Class cls, String attrname) {
        Class lookup = cls;
while (true) { 
{
            boolean found = false;
            Attribute a[] = getClassAttributes(cls);
            if (a != null) {
                edu.hkust.clap.monitor.Monitor.loopBegin(593);
for (int i = 0; i < a.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(593);
{
                    if ((found = a[i].getName().equals(attrname))) break;
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(593);

            }
            if (found) {
                lookup = cls;
                cls = cls.getSuperclass();
            } else {
                return (lookup == cls) ? null : lookup;
            }
        }} 

    }
}
