package org.w3c.jigsaw.auth;

import java.io.PrintStream;
import java.util.Vector;
import java.net.InetAddress;

class IPMatcherNode {

    short part = -1;

    Object closure = null;

    Vector elems = null;

    /**
     * Set this node closure object.
     * @param obj The new closure for the node.
     */
    protected void setClosure(Object obj) {
        this.closure = obj;
    }

    /**
     * Get this node closure object.
     * @return An instance of Object.
     */
    protected Object getClosure() {
        return closure;
    }

    /**
     * Lookup this byte of IP adress in this node. 
     * If we are requested to create a new node, than we create and return it.
     */
    IPMatcherNode step(short iadr, Object closure) {
        IPMatcherNode node = null;
        int lo = 0;
        int hi = elems.size() - 1;
        int idx = -1;
        if (hi >= lo) {
            edu.hkust.clap.monitor.Monitor.loopBegin(447);
while ((hi - lo) > 1) { 
edu.hkust.clap.monitor.Monitor.loopInc(447);
{
                idx = (hi - lo) / 2 + lo;
                node = (IPMatcherNode) elems.elementAt(idx);
                if (node.part == 256) {
                    return node;
                } else if (node.part > iadr) {
                    hi = idx;
                } else if (node.part < iadr) {
                    lo = idx;
                } else {
                    return node;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(447);

            switch(hi - lo) {
                case 0:
                    node = (IPMatcherNode) elems.elementAt(hi);
                    if ((node.part == iadr) || (node.part == 256)) return node;
                    idx = (node.part < iadr) ? hi + 1 : hi;
                    break;
                case 1:
                    IPMatcherNode lonode = (IPMatcherNode) elems.elementAt(lo);
                    IPMatcherNode hinode = (IPMatcherNode) elems.elementAt(hi);
                    if ((lonode.part == iadr) || (lonode.part == 256)) return lonode;
                    if ((hinode.part == iadr) || (hinode.part == 256)) return hinode;
                    if (iadr < lonode.part) {
                        idx = lo;
                    } else if (iadr < hinode.part) {
                        idx = hi;
                    } else {
                        idx = hi + 1;
                    }
                    break;
                default:
                    throw new RuntimeException("IPMatcherNode: inconsistent.");
            }
        }
        if (closure != null) {
            if (idx < 0) idx = 0;
            node = new IPMatcherNode(iadr);
            elems.insertElementAt(node, idx);
            return node;
        } else {
            return null;
        }
    }

    /**
     * Print the tree starting at this node.
     * @param out The print stream to print to.
     * @param pref The prefix string (printed before all infos).
     */
    public void print(PrintStream out, String pref) {
        System.out.println(pref + "{" + part + "}" + ":");
        edu.hkust.clap.monitor.Monitor.loopBegin(448);
for (int i = 0; i < elems.size(); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(448);
((IPMatcherNode) elems.elementAt(i)).print(out, pref + "\t");} 
edu.hkust.clap.monitor.Monitor.loopEnd(448);

    }

    /**
     * Create a new node for a given sub-space.
     * @param part The IP address part to register in this node.
     */
    IPMatcherNode(short part) {
        this.part = part;
        this.elems = new Vector(2);
    }

    /**
     * Create the root node.
     */
    IPMatcherNode() {
        this.elems = new Vector();
    }
}

/**
 * A fast way of associating IP adresses to any Object.
 * This IPMatcher classes maps IP adresses to objects. It understands wild
 * cards, encoded as ((short) 256). Wild card will match any adress below it.
 */
public class IPMatcher {

    IPMatcherNode root = null;

    /**
     * Associate the given IP adress to the given object.
     * This method takes as parameter an array of <em>short</em> in order
     * to extend natural IP adresses bytes with the wild card character.
     * @param a The adress to use as a key for the association.
     * @param closure The associated object.
     */
    public void add(short a[], Object closure) {
        IPMatcherNode node = root;
        edu.hkust.clap.monitor.Monitor.loopBegin(449);
for (int i = 0; i < a.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(449);
node = node.step(a[i], closure);} 
edu.hkust.clap.monitor.Monitor.loopEnd(449);

        node.setClosure(closure);
        return;
    }

    /**
     * Lookup the adress for an association.
     * @param a The adress to look for.
     * @return The object associated to the given IP address, or 
     *    <strong>null</strong> if none was found.
     */
    public Object lookup(byte a[]) {
        IPMatcherNode node = root;
        edu.hkust.clap.monitor.Monitor.loopBegin(450);
for (int i = 0; (node != null) && (i < a.length); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(450);
node = node.step((short) (((short) a[i]) & 0xff), null);} 
edu.hkust.clap.monitor.Monitor.loopEnd(450);

        return (node == null) ? null : node.closure;
    }

    /**
     * Lookup the given InetAdress for any association.
     * @param inetadr The inet adress to look for.
     * @return The object associated to the given IP adress, or 
     *    <strong>null</strong> if none was found.
     */
    public Object lookup(InetAddress inetadr) {
        return lookup(inetadr.getAddress());
    }

    /**
     * Print the IP matcher internal tree.
     */
    public void print(PrintStream out) {
        root.print(out, "");
    }

    public IPMatcher() {
        this.root = new IPMatcherNode();
    }
}
