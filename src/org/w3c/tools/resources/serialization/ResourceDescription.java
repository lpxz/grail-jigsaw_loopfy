package org.w3c.tools.resources.serialization;

import java.util.Vector;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFrame;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class ResourceDescription {

    String classname = null;

    String classes[] = null;

    String interfaces[] = null;

    String identifier = null;

    AttributeDescription attributes[] = null;

    String children[] = null;

    /**
     * Get a clone of this resource description but with only the
     * given list of attribute descriptions.
     * @param attrs the new attribute descriptions
     * @return a ResourceDescription;
     */
    public ResourceDescription getClone(AttributeDescription attrs[]) {
        ResourceDescription descr = new ResourceDescription(classname);
        descr.identifier = identifier;
        descr.children = children;
        descr.attributes = attrs;
        descr.classes = classes;
        descr.interfaces = interfaces;
        return descr;
    }

    /**
     * Get this resource class hierarchy.
     * @return a String array
     */
    public String[] getClassHierarchy() {
        return classes;
    }

    /**
     * Get this resource interfaces
     * @return a String array
     */
    public String[] getInterfaces() {
        return interfaces;
    }

    public String[] getClassesAndInterfaces() {
        String all[] = new String[interfaces.length + classes.length];
        System.arraycopy(classes, 0, all, 0, classes.length);
        System.arraycopy(interfaces, 0, all, classes.length, interfaces.length);
        return all;
    }

    /**
     * Get the resource Class name.
     * @return a String
     */
    public String getClassName() {
        return classname;
    }

    /**
     * Get the resource identifier.
     * @return a String instance
     */
    public String getIdentifier() {
        if ((identifier == null) && (attributes != null)) {
            edu.hkust.clap.monitor.Monitor.loopBegin(947);
for (int i = 0; i < attributes.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(947);
{
                if (attributes[i].getName().equals("identifier")) identifier = (String) attributes[i].getValue();
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(947);

        }
        return identifier;
    }

    /**
     * get the children identifiers
     * @return a String array
     */
    public String[] getChildren() {
        return children;
    }

    /**
     * Set the children names.
     * @param a String array
     */
    public void setChildren(String children[]) {
        this.children = children;
    }

    /**
     * Get the attributes description.
     * @return an AttributeDescription array
     * @see AttributeDescription
     */
    public AttributeDescription[] getAttributeDescriptions() {
        return attributes;
    }

    /**
     * Get the description of the frames associated to this resource.
     * @return a ResourceDescription array.
     */
    public ResourceDescription[] getFrameDescriptions() {
        edu.hkust.clap.monitor.Monitor.loopBegin(948);
for (int i = 0; i < attributes.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(948);
{
            Object value = attributes[i].getValue();
            if (value instanceof ResourceDescription[]) return (ResourceDescription[]) value;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(948);

        return new ResourceDescription[0];
    }

    /**
     * Constructor.
     * @param resource the resource to describe.
     */
    public ResourceDescription(Resource resource) {
        this.classname = resource.getClass().getName();
        Vector vclasses = new Vector(8);
        Vector vinterfaces = new Vector(8);
        Class ints[] = resource.getClass().getInterfaces();
        if (ints != null) edu.hkust.clap.monitor.Monitor.loopBegin(949);
for (int i = 0; i < ints.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(949);
vinterfaces.addElement(ints[i]);} 
edu.hkust.clap.monitor.Monitor.loopEnd(949);

        edu.hkust.clap.monitor.Monitor.loopBegin(950);
for (Class c = resource.getClass().getSuperclass(); c != null; c = c.getSuperclass()) { 
edu.hkust.clap.monitor.Monitor.loopInc(950);
{
            vclasses.addElement(c.getName());
            ints = c.getInterfaces();
            if (ints != null) edu.hkust.clap.monitor.Monitor.loopBegin(949);
for (int i = 0; i < ints.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(949);
vinterfaces.addElement(ints[i]);} 
edu.hkust.clap.monitor.Monitor.loopEnd(949);

        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(950);

        this.classes = new String[vclasses.size()];
        vclasses.copyInto(this.classes);
        this.interfaces = new String[vinterfaces.size()];
        vinterfaces.copyInto(this.interfaces);
        Attribute attrs[] = resource.getAttributes();
        Vector vattrs = new Vector(10);
        edu.hkust.clap.monitor.Monitor.loopBegin(951);
for (int j = 0; j < attrs.length; j++) { 
edu.hkust.clap.monitor.Monitor.loopInc(951);
{
            Object value = resource.getValue(j, null);
            if (value instanceof ResourceFrame[]) {
                ResourceFrame frames[] = (ResourceFrame[]) value;
                int len = frames.length;
                ResourceDescription descr[] = new ResourceDescription[len];
                edu.hkust.clap.monitor.Monitor.loopBegin(952);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(952);
descr[i] = new ResourceDescription(frames[i]);} 
edu.hkust.clap.monitor.Monitor.loopEnd(952);

                vattrs.addElement(new AttributeDescription(attrs[j], descr));
            } else {
                vattrs.addElement(new AttributeDescription(attrs[j], value));
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(951);

        this.attributes = new AttributeDescription[vattrs.size()];
        vattrs.copyInto(attributes);
    }

    /**
     * Set the attributes description of the resource
     * @param a Vector of AttributeDescription instances
     */
    public void setAttributeDescriptions(Vector attrs) {
        attributes = new AttributeDescription[attrs.size()];
        attrs.copyInto(attributes);
    }

    /**
     * Set the resource class hierarchy.
     * @param a String array
     */
    public void setClassHierarchy(String classes[]) {
        this.classes = classes;
    }

    /**
     * Set the resource class hierarchy.
     * @param a String array
     */
    public void setInterfaces(String interfaces[]) {
        this.interfaces = interfaces;
    }

    /**
     * Set the resource class hierarchy.
     * @param a Vector of String instances
     */
    public void setClassHierarchy(Vector vclasses) {
        this.classes = new String[vclasses.size()];
        vclasses.copyInto(this.classes);
    }

    /**
     * Set the resource class hierarchy.
     * @param a Vector of String instances
     */
    public void setInterfaces(Vector vinterfaces) {
        this.interfaces = new String[vinterfaces.size()];
        vinterfaces.copyInto(this.interfaces);
    }

    /**
     * Constructor.
     * @param classname the resource class name
     */
    public ResourceDescription(String classname) {
        this.classname = classname;
        this.interfaces = new String[0];
        this.classes = new String[0];
    }
}
