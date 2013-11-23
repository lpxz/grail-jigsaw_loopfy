package org.w3c.tools.offline.browse;

import org.w3c.tools.resources.serialization.Serializer;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.serialization.ResourceDescription;
import org.w3c.tools.resources.serialization.AttributeDescription;
import org.w3c.tools.resources.serialization.xml.XMLSerializer;
import org.w3c.tools.resources.AttributeHolder;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.MalformedPatternException;
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
import java.util.Hashtable;
import java.util.Stack;

/**
 * <p>The finder that handles a given jigsaw store
 */
public class StoreFinder {

    public static final String ROOT_REP = "root.xml";

    public static final String INDEX_F = "index.xml";

    /**
	 *  The store directory (relative to current path)
	 */
    public String store_dir = null;

    /**
	 * The server name (e.g. http-server) 
	 */
    public String server_name = null;

    /**
	 * A resource lister instance that handles the repository resources 
	 */
    protected ResourceLister rl = null;

    protected Serializer serializer = null;

    protected Hashtable index_table = new Hashtable();

    protected Hashtable finders = new Hashtable();

    private Stack cur_stack = new Stack();

    private Stack res_stack = new Stack();

    private Stack rep_stack = new Stack();

    /**
	 * The working repository
	 */
    private String working_rep = null;

    protected static Perl5Matcher pmatcher = new Perl5Matcher();

    protected static Perl5Compiler pcompiler = new Perl5Compiler();

    /**
	* Initializes the finder
	*/
    public StoreFinder(String store_dir, String server_name) {
        this.store_dir = store_dir;
        this.server_name = server_name;
        this.rl = new ResourceLister();
    }

    /**
	* Reads the index file of the store.
	* opens the file and initialize index_table, launch one finder for
	* each repository.
	*/
    public void readStoreIndex() throws IOException {
        serializer = new org.w3c.tools.resources.serialization.xml.XMLSerializer();
        File index = new File(store_dir, server_name + "-" + INDEX_F);
        System.out.println("Reading index file...");
        try {
            Reader reader = new BufferedReader(new FileReader(index));
            AttributeHolder[] atth = serializer.readAttributeHolders(reader);
            edu.hkust.clap.monitor.Monitor.loopBegin(314);
for (int i = 0; i < atth.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(314);
{
                Integer key = rl.getKeyFromHolder(atth[i]);
                if (key != null) {
                    String rep = rl.getRepositoryFromHolder(atth[i]);
                    index_table.put(key, rep);
                    if (!finders.containsKey(rep)) {
                        RepositoryFinder rf = new RepositoryFinder(store_dir, rep);
                        finders.put(rep, rf);
                    }
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(314);

        } catch (InvalidStoreException e) {
            System.out.println("Invalid store");
            throw new IOException();
        } catch (IOException e) {
            System.out.println("can't read index file");
            throw new IOException();
        }
    }

    /**
	* Reads the root file of the store.
	* opens the file and initialize working repository and stacks, 
	* lists all the store recurively.
	*/
    public void readStoreRoot() throws IOException {
        serializer = new org.w3c.tools.resources.serialization.xml.XMLSerializer();
        File root = new File(store_dir, ROOT_REP);
        working_rep = ROOT_REP;
        res_stack.push(ROOT_REP);
        RepositoryFinder rf = (RepositoryFinder) finders.get(ROOT_REP);
        rep_stack.push(rf);
        System.out.println("reading root file...\n");
        Reader reader = new BufferedReader(new FileReader(root));
        AttributeHolder[] atth = serializer.readAttributeHolders(reader);
        edu.hkust.clap.monitor.Monitor.loopBegin(315);
for (int i = 0; i < atth.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(315);
{
            System.out.println(rl.getIdentFromHolder(atth[i]));
            Integer key = rl.getKeyFromHolder(atth[i]);
            if (key != null) {
                String repository = (String) index_table.get(key);
                if (repository.compareTo(ROOT_REP) != 0) {
                    recursiveReadContainer(repository, 1);
                }
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(315);

    }

    /**
	* Recursive read of containers 
	* @param repname the repository file name
	* @param deep recursivity depth level.
	*/
    private void recursiveReadContainer(String repname, int deep) {
        RepositoryFinder rf = (RepositoryFinder) finders.get(repname);
        AttributeHolder[] atth = rf.getAttributeHolders();
        String depthtab = "";
        edu.hkust.clap.monitor.Monitor.loopBegin(240);
for (int j = 0; j < deep; j++) { 
edu.hkust.clap.monitor.Monitor.loopInc(240);
{
            depthtab = depthtab + "\t";
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(240);

        edu.hkust.clap.monitor.Monitor.loopBegin(316);
for (int i = 0; i < atth.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(316);
{
            System.out.println(depthtab + "|-" + rl.getIdentFromHolder(atth[i]));
            Integer key = rl.getKeyFromHolder(atth[i]);
            if (key != null) {
                if (index_table.containsKey(key)) {
                    String chrep = (String) index_table.get(key);
                    recursiveReadContainer(chrep, deep + 1);
                } else {
                    System.out.println("WARNING! " + rl.getIdentFromHolder(atth[i]) + " is a container but its key " + key + " does not exist in store index");
                }
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(316);

    }

    /**
	* Execute an action on the store.
	* launch a try for the given action on the current repository.
	* @param action the action string
	* @param identifier the target of action (a Perl5 regexp)
	* @param recursive a boolean flag (true = recursive action).
	*/
    public void finderAction(String action, String identifier, boolean recursive) {
        if (working_rep.compareTo(ROOT_REP) == 0) {
            System.out.println(working_rep);
            tryAction(action, working_rep, identifier, recursive, 1);
        } else {
            RepositoryFinder rf = (RepositoryFinder) rep_stack.peek();
            System.out.println(working_rep);
            tryAction(action, rf.getRep(), identifier, recursive, 1);
        }
    }

    /**
	* the real try method 
	*/
    private void tryAction(String action, String repname, String identifier, boolean recursive, int deep) {
        RepositoryFinder rf = (RepositoryFinder) finders.get(repname);
        AttributeHolder[] atth = rf.getAttributeHolders();
        Pattern iPattern = null;
        boolean toWrite = false;
        try {
            iPattern = pcompiler.compile("^" + identifier + "$", Perl5Compiler.DEFAULT_MASK);
            edu.hkust.clap.monitor.Monitor.loopBegin(317);
for (int i = 0; i < atth.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(317);
{
                cur_stack.push(rl.getIdentFromHolder(atth[i]));
                if (pmatcher.matches(rl.getIdentFromHolder(atth[i]), iPattern)) {
                    rl.performAction(action, atth[i], deep);
                    rl.performActionOnFrames(action, atth[i], deep);
                    toWrite = true;
                    Integer key = rl.getKeyFromHolder(atth[i]);
                    if (key != null && recursive == true) {
                        String chrep = (String) index_table.get(key);
                        if (chrep.compareTo(ROOT_REP) != 0) {
                            recursivePerformAction(action, chrep, deep + 1);
                        }
                    }
                } else {
                    Integer key = rl.getKeyFromHolder(atth[i]);
                    if (key != null && recursive == true) {
                        String chrep = (String) index_table.get(key);
                        if (chrep.compareTo(ROOT_REP) != 0) {
                            tryAction(action, chrep, identifier, recursive, deep + 1);
                        }
                    }
                }
                cur_stack.pop();
                if (toWrite) {
                    rf.writeHolders();
                    toWrite = false;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(317);

        } catch (MalformedPatternException ex) {
            System.out.println("malformed expression " + identifier);
        }
    }

    private void recursivePerformAction(String action, String repname, int deep) {
        RepositoryFinder rf = (RepositoryFinder) finders.get(repname);
        AttributeHolder[] atth = rf.getAttributeHolders();
        edu.hkust.clap.monitor.Monitor.loopBegin(318);
for (int i = 0; i < atth.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(318);
{
            rl.performAction(action, atth[i], deep);
            rl.performActionOnFrames(action, atth[i], deep);
            Integer key = rl.getKeyFromHolder(atth[i]);
            if (key != null) {
                String chrep = (String) index_table.get(key);
                if (chrep.compareTo(ROOT_REP) != 0) recursivePerformAction(action, chrep, deep + 1);
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(318);

        rf.writeHolders();
    }

    /**
	* Get the working repository
	* @return a string that is the repository file name.
	*/
    public String getWorkingRep() {
        return (working_rep);
    }

    /**
	* Print the working repository
	*/
    public void printWorkingRep() {
        System.out.println(working_rep);
    }

    /**
	* Set the working repository
	* @param newrep a string that is the container name or ".." to 
	* go back in the stack.
	*/
    public void setWorkingRep(String newrep) {
        if (newrep.compareTo("..") == 0) {
            res_stack.pop();
            rep_stack.pop();
            working_rep = (String) res_stack.peek();
        } else {
            try {
                RepositoryFinder rf = (RepositoryFinder) rep_stack.peek();
                AttributeHolder[] atth = rf.getAttributeHolders();
                edu.hkust.clap.monitor.Monitor.loopBegin(319);
for (int i = 0; i < atth.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(319);
{
                    Integer key = rl.getKeyFromHolder(atth[i]);
                    if (rl.getIdentFromHolder(atth[i]).compareTo(newrep) == 0 && key != null) {
                        working_rep = newrep;
                        res_stack.push(rl.getIdentFromHolder(atth[i]));
                        String reps = (String) index_table.get(key);
                        rep_stack.push((RepositoryFinder) finders.get(reps));
                    }
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(319);

                if (working_rep.compareTo(newrep) != 0) {
                    System.out.println("container not found or not a container");
                }
            } catch (java.lang.NullPointerException ex) {
                ex.printStackTrace();
            }
        }
    }
}
