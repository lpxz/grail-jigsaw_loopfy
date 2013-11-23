package org.w3c.tools.offline.browse;

import org.w3c.tools.resources.serialization.Serializer;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.serialization.ResourceDescription;
import org.w3c.tools.resources.serialization.AttributeDescription;
import org.w3c.tools.resources.AttributeHolder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.lang.String;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.perl.Perl5Util;

/** 
 * The resource lister class.
 * It acts on resources independently, and perform action on 
 * resources and frames as isolated entities.
 */
public class ResourceLister {

    protected static Perl5Matcher pmatcher = new Perl5Matcher();

    protected static Perl5Compiler pcompiler = new Perl5Compiler();

    protected static Perl5Util putil = new Perl5Util();

    /**
	* the search and replace pattern.
	*/
    protected static Pattern srPattern;

    /**
	* Print all attributes of an AttributeHolder.
	* @param ah an AttributeHolder.
	*/
    public void printAttributeHolder(AttributeHolder ah) {
        Attribute[] att = ah.getAttributes();
        edu.hkust.clap.monitor.Monitor.loopBegin(236);
for (int j = 0; j < att.length; j++) { 
edu.hkust.clap.monitor.Monitor.loopInc(236);
{
            Object value = ah.getValue(j, null);
            this.printAttribute(att[j], value);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(236);

    }

    /**
	* Print an attribute and its value.
	* @param ad an Attribute
	* @param value its value.
	*/
    private void printAttribute(Attribute ad, Object value) {
        System.out.println(attributeToString(ad, value));
    }

    /**
	* Format into an attribute=value string.
	* @param ad an Attribute
	* @param value its value.
	* @return the attribute=value string.
	*/
    public String attributeToString(Attribute ad, Object value) {
        String concat = ad.getName() + "=" + value;
        return concat;
    }

    /**
	* Print the frames associated to a resource.
	* @param ah an AttributeHolder
	*/
    public void printFrames(AttributeHolder ah) {
        AttributeHolder[] frames = this.getFramesFromHolder(ah);
        edu.hkust.clap.monitor.Monitor.loopBegin(237);
for (int j = 0; j < frames.length; j++) { 
edu.hkust.clap.monitor.Monitor.loopInc(237);
{
            System.out.println("resource frame: ");
            this.printAttributeHolder(frames[j]);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(237);

    }

    /** 
	* Search for a given attribute value in an attribute holder.
	* @param ah the AttributeHolder
	* @param attribname the attribute name
	* @return the attribute value or null if not found.
	*/
    private Object searchValue(AttributeHolder ah, String attribname) {
        Attribute[] att = ah.getAttributes();
        Object value = null;
        edu.hkust.clap.monitor.Monitor.loopBegin(238);
for (int j = 0; j < att.length; j++) { 
edu.hkust.clap.monitor.Monitor.loopInc(238);
{
            if (att[j].getName().compareTo(attribname) == 0) {
                value = ah.getValue(j, null);
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(238);

        return value;
    }

    /** 
	* Search for key value in an attribute holder.
	* @param ah the AttributeHolder
	* @return the key value or null if not found.
	*/
    public Integer getKeyFromHolder(AttributeHolder ah) {
        return (Integer) searchValue(ah, "key");
    }

    /** 
	* Search for identifier in an attribute holder.
	* @param ah the AttributeHolder
	* @return the identifier.
	*/
    public String getIdentFromHolder(AttributeHolder ah) {
        return (String) searchValue(ah, "identifier");
    }

    /** 
	* Search for repository in an attribute holder.
	* @param ah the AttributeHolder
	* @return the repository name.
	*/
    public String getRepositoryFromHolder(AttributeHolder ah) {
        return (String) searchValue(ah, "repository");
    }

    /** 
	* Get frames of an attribute holder.
	* @param ah the AttributeHolder
	* @return an array with frames.
	*/
    public AttributeHolder[] getFramesFromHolder(AttributeHolder ah) {
        return (AttributeHolder[]) searchValue(ah, "frames");
    }

    /** 
	* Perform an action on frames of an attribute holder.
	* @param action the action to perform
	* @param ah the AttributeHolder
	* @param int the resource depth wrt the entire action target.
	*/
    public void performActionOnFrames(String action, AttributeHolder ah, int deep) {
        AttributeHolder[] frames = this.getFramesFromHolder(ah);
        edu.hkust.clap.monitor.Monitor.loopBegin(239);
for (int j = 0; j < frames.length; j++) { 
edu.hkust.clap.monitor.Monitor.loopInc(239);
{
            this.performAction(action, frames[j], deep + 1);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(239);

    }

    /** 
	* Perform an action on an attribute holder.
	* @param action the action to perform
	* @param ah the AttributeHolder
	* @param int the resource depth wrt the entire action target.
	*/
    public void performAction(String action, AttributeHolder ah, int deep) {
        if (action.compareTo("list") == 0) {
            String depthtab = "";
            edu.hkust.clap.monitor.Monitor.loopBegin(240);
for (int j = 0; j < deep; j++) { 
edu.hkust.clap.monitor.Monitor.loopInc(240);
{
                depthtab = depthtab + "\t";
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(240);

            System.out.println(depthtab + "|-" + this.getIdentFromHolder(ah));
        } else if (action.compareTo("listatt") == 0) {
            this.printAttributeHolder(ah);
        } else if (pmatcher.matches(action, srPattern)) {
            System.out.println("hop: " + pmatcher.getMatch().group(1));
            Attribute[] att = ah.getAttributes();
            try {
                Pattern pt = pcompiler.compile(pmatcher.getMatch().group(1), Perl5Compiler.DEFAULT_MASK);
                String newvalue = pmatcher.getMatch().group(2);
                edu.hkust.clap.monitor.Monitor.loopBegin(241);
for (int j = 0; j < att.length; j++) { 
edu.hkust.clap.monitor.Monitor.loopInc(241);
{
                    Object value = ah.getValue(j, null);
                    String atts = attributeToString(att[j], value);
                    if (pmatcher.matches(atts, pt)) {
                        String repl = putil.substitute(action, atts);
                        System.out.println("replaced: " + atts + " by: " + repl);
                        ah.setValue(j, newvalue);
                    }
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(241);

            } catch (org.apache.oro.text.regex.MalformedPatternException ex) {
                ex.printStackTrace();
            }
        }
    }

    /** 
 	* The resource lister initializer.
 	*/
    public ResourceLister() {
        try {
            srPattern = pcompiler.compile("^s/(.*[^\\\\])/(.*[^\\\\])/$", Perl5Compiler.DEFAULT_MASK);
        } catch (org.apache.oro.text.regex.MalformedPatternException ex) {
            ex.printStackTrace();
        }
    }
}
