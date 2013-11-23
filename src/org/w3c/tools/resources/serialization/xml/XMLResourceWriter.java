package org.w3c.tools.resources.serialization.xml;

import java.io.Writer;
import java.io.Reader;
import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.ContainerInterface;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.SimpleAttribute;
import org.w3c.tools.resources.ArrayAttribute;
import org.w3c.tools.resources.FrameArrayAttribute;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class XMLResourceWriter extends XMLWriter implements JigXML {

    protected void startResource(AttributeHolder res) throws IOException {
        String classname = res.getClass().getName();
        writer.write('<');
        writer.write(RESOURCE_TAG);
        writer.write(' ');
        writer.write(CLASS_ATTR);
        writer.write("='");
        writer.write(classname);
        writer.write("'>\n");
    }

    protected void startResourceDescr(AttributeHolder res) throws IOException {
        String classname = res.getClass().getName();
        writer.write('<');
        writer.write(RESOURCE_TAG);
        writer.write(' ');
        writer.write(CLASS_ATTR);
        writer.write("='");
        writer.write(classname);
        writer.write("'>\n");
        Vector interfaces = new Vector(10);
        Class c = res.getClass();
        Class intfs[] = c.getInterfaces();
        if (intfs != null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(798);
for (int i = 0; i < intfs.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(798);
interfaces.addElement(intfs[i].getName());} 
edu.hkust.clap.monitor.Monitor.loopEnd(798);

        }
        writeInherit(c.getSuperclass(), interfaces);
        level++;
        edu.hkust.clap.monitor.Monitor.loopBegin(799);
for (int i = 0; i < interfaces.size(); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(799);
{
            String s = (String) interfaces.elementAt(i);
            indent();
            writer.write('<');
            writer.write(IMPLEMENTS_TAG);
            writer.write(' ');
            writer.write(CLASS_ATTR);
            writer.write("='");
            writer.write(s);
            writer.write("'/>\n");
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(799);

        level--;
        writeChildren(res);
    }

    protected void writeInherit(Class c, Vector interfaces) throws IOException {
        if (c != null) {
            level++;
            indent();
            writer.write('<');
            writer.write(INHERIT_TAG);
            writer.write(' ');
            writer.write(CLASS_ATTR);
            writer.write("='");
            writer.write(c.getName());
            writer.write("'>\n");
            Class intfs[] = c.getInterfaces();
            if (intfs != null) {
                edu.hkust.clap.monitor.Monitor.loopBegin(798);
for (int i = 0; i < intfs.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(798);
interfaces.addElement(intfs[i].getName());} 
edu.hkust.clap.monitor.Monitor.loopEnd(798);

            }
            writeInherit(c.getSuperclass(), interfaces);
            indent();
            writer.write("</");
            writer.write(INHERIT_TAG);
            writer.write(">\n");
            level--;
        }
    }

    protected void writeChildren(AttributeHolder res) throws IOException {
        if (res instanceof ContainerInterface) {
            Vector vids = new Vector();
            Enumeration e = ((ContainerInterface) res).enumerateResourceIdentifiers(true);
            edu.hkust.clap.monitor.Monitor.loopBegin(800);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(800);
{
                vids.addElement((String) e.nextElement());
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(800);

            int len = vids.size();
            level++;
            indent();
            writer.write('<');
            writer.write(CHILDREN_TAG);
            writer.write(' ');
            writer.write(LENGTH_ATTR);
            writer.write("='");
            writer.write(String.valueOf(len));
            writer.write("'>\n");
            level++;
            edu.hkust.clap.monitor.Monitor.loopBegin(801);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(801);
{
                indent();
                writer.write('<');
                writer.write(CHILD_TAG);
                writer.write('>');
                writer.write(encode((String) vids.elementAt(i)));
                writer.write("</");
                writer.write(CHILD_TAG);
                writer.write(">\n");
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(801);

            level--;
            indent();
            writer.write("</");
            writer.write(CHILDREN_TAG);
            writer.write(">\n");
            level--;
        }
    }

    protected void writeAttribute(Attribute attr, Object value, boolean descr) throws IOException {
        level++;
        if (attr instanceof SimpleAttribute) {
            indent();
            writer.write('<');
            writer.write(ATTRIBUTE_TAG);
            writer.write(' ');
            writer.write(NAME_ATTR);
            writer.write("='");
            writer.write(attr.getName());
            writer.write("' ");
            writer.write(FLAG_ATTR);
            writer.write("='");
            writer.write(attr.getFlag());
            writer.write("' ");
            writer.write(CLASS_ATTR);
            writer.write("='");
            writer.write(attr.getClass().getName());
            writer.write("'>");
            if (value == null) {
                writer.write(NULL);
            } else {
                String pickled = ((SimpleAttribute) attr).pickle(value);
                writer.write(encode(pickled));
            }
            writer.write("</");
            writer.write(ATTRIBUTE_TAG);
            writer.write(">\n");
        } else if (attr instanceof ArrayAttribute) {
            indent();
            writer.write('<');
            writer.write(ARRAY_TAG);
            writer.write(' ');
            writer.write(NAME_ATTR);
            writer.write("='");
            writer.write(attr.getName());
            writer.write("' ");
            writer.write(FLAG_ATTR);
            writer.write("='");
            writer.write(attr.getFlag());
            writer.write("' ");
            writer.write(CLASS_ATTR);
            writer.write("='");
            writer.write(attr.getClass().getName());
            writer.write("' ");
            String values[] = null;
            if (value == null) values = new String[0]; else values = ((ArrayAttribute) attr).pickle(value);
            int len = values.length;
            writer.write(LENGTH_ATTR);
            writer.write("='");
            writer.write(String.valueOf(len));
            writer.write("'>\n");
            level++;
            String rval;
            edu.hkust.clap.monitor.Monitor.loopBegin(802);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(802);
{
                indent();
                writer.write('<');
                writer.write(VALUE_TAG);
                writer.write('>');
                rval = values[i];
                if (rval == null) {
                    writer.write(NULL);
                } else {
                    writer.write(encode(rval));
                }
                writer.write("</");
                writer.write(VALUE_TAG);
                writer.write(">\n");
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(802);

            level--;
            indent();
            writer.write("</");
            writer.write(ARRAY_TAG);
            writer.write(">\n");
        } else if (attr instanceof FrameArrayAttribute) {
            indent();
            writer.write('<');
            writer.write(RESARRAY_TAG);
            writer.write(' ');
            writer.write(NAME_ATTR);
            writer.write("='");
            writer.write(attr.getName());
            writer.write("' ");
            writer.write(CLASS_ATTR);
            writer.write("='");
            writer.write(attr.getClass().getName());
            writer.write("' ");
            ResourceFrame frames[] = null;
            if (value == null) frames = new ResourceFrame[0]; else frames = (ResourceFrame[]) value;
            int len = frames.length;
            writer.write(LENGTH_ATTR);
            writer.write("='");
            writer.write(String.valueOf(len));
            writer.write("'>\n");
            if (descr) {
                edu.hkust.clap.monitor.Monitor.loopBegin(803);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(803);
{
                    writeResourceDescription(frames[i]);
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(803);

            } else {
                edu.hkust.clap.monitor.Monitor.loopBegin(804);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(804);
{
                    writeResource(frames[i]);
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(804);

            }
            indent();
            writer.write("</");
            writer.write(RESARRAY_TAG);
            writer.write(">\n");
        }
        level--;
    }

    public void writeResourceDescription(AttributeHolder holder) throws IOException {
        level++;
        indent();
        startResourceDescr(holder);
        Attribute attrs[] = holder.getAttributes();
        edu.hkust.clap.monitor.Monitor.loopBegin(805);
for (int j = 0; j < attrs.length; j++) { 
edu.hkust.clap.monitor.Monitor.loopInc(805);
{
            Object value = holder.unsafeGetValue(j, null);
            if (!attrs[j].checkFlag(Attribute.DONTSAVE)) writeAttribute(attrs[j], value, true);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(805);

        indent();
        closeResource();
        level--;
    }

    public void writeResource(AttributeHolder holder) throws IOException {
        level++;
        indent();
        startResource(holder);
        Attribute attrs[] = holder.getAttributes();
        edu.hkust.clap.monitor.Monitor.loopBegin(806);
for (int j = 0; j < attrs.length; j++) { 
edu.hkust.clap.monitor.Monitor.loopInc(806);
{
            Object value = holder.unsafeGetValue(j, null);
            if (!attrs[j].checkFlag(Attribute.DONTSAVE)) writeAttribute(attrs[j], value, false);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(806);

        indent();
        closeResource();
        level--;
    }

    public XMLResourceWriter(Writer writer) {
        super(writer);
    }
}
