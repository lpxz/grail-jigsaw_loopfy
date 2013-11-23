package org.w3c.www.webdav.xml;

import java.util.Vector;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.www.webdav.WEBDAV;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class DAVNode {

    public static final String ACTIVELOCK_NODE = "activelock";

    public static final String LOCKSCOPE_NODE = "lockscope";

    public static final String LOCKTYPE_NODE = "locktype";

    public static final String DEPTH_NODE = "depth";

    public static final String OWNER_NODE = "owner";

    public static final String TIMEOUT_NODE = "timeout";

    public static final String LOCKTOKEN_NODE = "locktoken";

    public static final String LOCKENTRY_NODE = "lockentry";

    public static final String LOCKINFO_NODE = "lockinfo";

    public static final String WRITE_NODE = "write";

    public static final String EXCLUSIVE_NODE = "exclusive";

    public static final String SHARED_NODE = "shared";

    public static final String HREF_NODE = "href";

    public static final String LINK_NODE = "link";

    public static final String SRC_NODE = "src";

    public static final String DST_NODE = "dst";

    public static final String MULTISTATUS_NODE = "multistatus";

    public static final String RESPONSE_NODE = "response";

    public static final String PROPSTAT_NODE = "propstat";

    public static final String RESPONSEDESC_NODE = "responsedescription";

    public static final String STATUS_NODE = "status";

    public static final String PROP_NODE = "prop";

    public static final String PROPERTYBEHAVIOR_NODE = "propertybehavior";

    public static final String OMIT_NODE = "omit";

    public static final String KEEPALIVE_NODE = "keepalive";

    public static final String PROPERTYUPDATE_NODE = "propertyupdate";

    public static final String REMOVE_NODE = "remove";

    public static final String SET_NODE = "set";

    public static final String PROPFIND_NODE = "propfind";

    public static final String ALLPROP_NODE = "allprop";

    public static final String PROPNAME_NODE = "propname";

    public static final String COLLECTION_NODE = "collection";

    public static final String CREATIONDATE_NODE = "creationdate";

    public static final String DISPLAYNAME_NODE = "displayname";

    public static final String GETCONTENTLANGUAGE_NODE = "getcontentlanguage";

    public static final String GETCONTENTLENGTH_NODE = "getcontentlength";

    public static final String GETCONTENTTYPE_NODE = "getcontenttype";

    public static final String GETETAG_NODE = "getetag";

    public static final String GETLASTMODIFIED_NODE = "getlastmodified";

    public static final String LOCKDISCOVERY_NODE = "lockdiscovery";

    public static final String RESOURCETYPE_NODE = "resourcetype";

    public static final String SOURCE_NODE = "source";

    public static final String SUPPORTEDLOCK_NODE = "supportedlock";

    public static final String ISCOLLECTION_NODE = "iscollection";

    public static final String ISSHARED_NODE = "isshared";

    public static final String ISHIDDEN_NODE = "ishidden";

    public static final String ISFOLDER_NODE = "isfolder";

    protected Element element = null;

    /**
     * Get the first node of the parent matchind the given tagname and
     * namespace.
     * @param parent the parent node
     * @param tagname the tagname to find
     * @param ns the namespace
     * @return a Node instance of null
     */
    public static Node getNodeNS(Node parent, String tagname, String ns) {
        Node current = parent.getFirstChild();
        edu.hkust.clap.monitor.Monitor.loopBegin(675);
while (current != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(675);
{
            if ((current.getNodeType() == current.ELEMENT_NODE) && (current.getLocalName().equals(tagname))) {
                if (ns != null) {
                    String cns = current.getNamespaceURI();
                    if ((cns != null) && (cns.equals(ns))) {
                        return current;
                    }
                } else {
                    return current;
                }
            }
            current = current.getNextSibling();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(675);

        return null;
    }

    /**
     * Add the given node to the children list of the parent node and
     * Add the Namespace definition if needed (work arround of xmlserializer
     * bug)
     * @param parent the parent node
     * @param child the new child
     */
    public static void addNodeNS(Node parent, Node child) {
        Document doc = parent.getOwnerDocument();
        String ns = child.getNamespaceURI();
        String prefix = child.getPrefix();
        if (ns != null) {
            if (prefix != null) {
                Element rootel = doc.getDocumentElement();
                String arg = "xmlns:" + prefix;
                if (rootel.getAttribute(arg).equals("")) {
                    rootel.setAttribute(arg, ns);
                }
            } else if (ns.equals(WEBDAV.NAMESPACE_URI)) {
                setPrefix(child, WEBDAV.NAMESPACE_PREFIX, ns);
            }
        }
        parent.appendChild(child);
    }

    public static void exportChildren(Document newdoc, Node newparent, Node node, boolean deep) throws DOMException {
        Node current = node.getFirstChild();
        edu.hkust.clap.monitor.Monitor.loopBegin(676);
while (current != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(676);
{
            if (current.getNodeType() == current.ELEMENT_NODE) {
                Node newnode = newdoc.importNode(current, deep);
                addNodeNS(newparent, newnode);
            }
            current = current.getNextSibling();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(676);

    }

    public static Node importNode(Document newdoc, Element parent, Node node, boolean deep) {
        Element rootel = newdoc.getDocumentElement();
        Node newnode = newdoc.importNode(node, deep);
        String ns = newnode.getNamespaceURI();
        if (ns != null) {
            String prefix = newnode.getPrefix();
            if (prefix != null) {
                String arg = "xmlns:" + prefix;
                String ons = rootel.getAttribute(arg);
                if (ons.equals("")) {
                    rootel.setAttribute(arg, ns);
                } else if (!ons.equals(ns)) {
                    prefix = "D" + prefix;
                    arg = "xmlns:" + prefix;
                    edu.hkust.clap.monitor.Monitor.loopBegin(677);
while ((ons = rootel.getAttribute(arg)) != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(677);
{
                        prefix = "D" + prefix;
                        arg = "xmlns:" + prefix;
                    }} 
edu.hkust.clap.monitor.Monitor.loopEnd(677);

                    rootel.setAttribute(arg, ns);
                    setPrefix(newnode, prefix, ns);
                }
            } else if (ns.equals(WEBDAV.NAMESPACE_URI)) {
                setPrefix(newnode, WEBDAV.NAMESPACE_PREFIX, ns);
            }
        }
        parent.appendChild(newnode);
        return newnode;
    }

    private static void setPrefix(Node node, String prefix, String ns) {
        String nns = node.getNamespaceURI();
        if ((nns != null) && (nns.equals(ns))) {
            node.setPrefix(prefix);
        }
        Node current = node.getFirstChild();
        edu.hkust.clap.monitor.Monitor.loopBegin(678);
while (current != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(678);
{
            if (current.getNodeType() == current.ELEMENT_NODE) {
                setPrefix(current, prefix, ns);
            }
            current = current.getNextSibling();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(678);

    }

    /**
     * Get our Node
     * @return a Node instance
     */
    public Node getNode() {
        return element;
    }

    /**
     * Get all the element that are chidren of the current node.
     * @return a array of Element
     */
    public Element[] getChildrenElements() {
        Vector v = new Vector();
        Node current = element.getFirstChild();
        edu.hkust.clap.monitor.Monitor.loopBegin(679);
while (current != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(679);
{
            if (current.getNodeType() == current.ELEMENT_NODE) {
                v.addElement((Element) current);
            }
            current = current.getNextSibling();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(679);

        Element elements[] = new Element[v.size()];
        v.copyInto(elements);
        return elements;
    }

    /**
     * Get the first node matching the given name and the given namespace
     * @param tagname the node name
     * @param ns the namespace
     * @return a Node instance or null
     */
    public Node getNodeNS(String tagname, String ns) {
        return getNodeNS(element, tagname, ns);
    }

    /**
     * Get the value of the fist child TEXT node.
     * @return a String
     */
    public String getTextValue() {
        return getTextChildValue(element);
    }

    /**
     * Get the value of the first child text node (if any)
     * @param node the parent node (can be null)
     * @return a String instance or null
     */
    public String getTextChildValue(Node node) {
        if (node == null) {
            return null;
        }
        Node current = node.getFirstChild();
        edu.hkust.clap.monitor.Monitor.loopBegin(680);
while (current != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(680);
{
            if (current.getNodeType() == current.TEXT_NODE) {
                return current.getNodeValue();
            }
            current = current.getNextSibling();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(680);

        return null;
    }

    /**
     * Get the value of the first child text node (if any)
     * @param tagname the parent node name
     * @param ns the namespace to match
     * @return a String instance or null
     */
    public String getTextChildValueNS(String tagname, String ns) {
        return getTextChildValue(getNodeNS(tagname, ns));
    }

    /**
     * Add this node to our children
     * @param node the new node
     */
    public void addNode(Node node) {
        element.appendChild(node);
    }

    /**
     * Add the given node to our children list.
     * Add the Namespace definition if needed (work arround of xmlserializer
     * bug)
     * @param parent the parent node
     * @param child the new child
     */
    public void addNodeNS(Node node) {
        addNodeNS(element, node);
    }

    /**
     * Get the list of children element that match the given tagname 
     * (and the DAV namespace)
     * @param node the parent node
     * @param tagname the tagname to match
     * @return a Vector instance
     */
    public static Vector getDAVElementsByTagName(Node node, String tagname) {
        Vector v = new Vector();
        Node current = node.getFirstChild();
        edu.hkust.clap.monitor.Monitor.loopBegin(681);
while (current != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(681);
{
            if ((current.getNodeType() == current.ELEMENT_NODE) && (current.getLocalName().equals(tagname)) && (current.getNamespaceURI() != null) && (current.getNamespaceURI().equals(WEBDAV.NAMESPACE_URI))) {
                v.addElement(current);
            }
            current = current.getNextSibling();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(681);

        return v;
    }

    /**
     * Get the first child element that match the given tagname 
     * (and the DAV namespace)
     * @param node the parent node
     * @param tagname the tagname to match
     * @return a Vector instance
     */
    public static Node getDAVNode(Node node, String tagname) {
        Node current = node.getFirstChild();
        edu.hkust.clap.monitor.Monitor.loopBegin(682);
while (current != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(682);
{
            if ((current.getNodeType() == current.ELEMENT_NODE) && (current.getLocalName().equals(tagname)) && (current.getNamespaceURI() != null) && (current.getNamespaceURI().equals(WEBDAV.NAMESPACE_URI))) {
                return current;
            }
            current = current.getNextSibling();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(682);

        return null;
    }

    /**
     * Create a new node and add it to the parent children list.
     * @param parent the parent node
     * @param name the tagname of the new node
     * @param textvalue the nodevalue of the TextNode (child of the new node)
     * @return the newly added Element
     */
    public static Element addDAVNode(Node parent, String name, String textvalue) throws DOMException {
        Document doc = parent.getOwnerDocument();
        Element el = doc.createElementNS(WEBDAV.NAMESPACE_URI, WEBDAV.NAMESPACE_PREFIX + ":" + name);
        if (textvalue != null) {
            el.appendChild(doc.createTextNode(textvalue));
        }
        parent.appendChild(el);
        return el;
    }

    /**
     * Get the text value of all our "DAV:" children matching the given
     * tagname with a text value available.
     * @param node the parent node
     * @param tagname the tagname to search
     * @return a String array
     */
    public static String[] getMultipleTextChildValue(Node node, String tagname) {
        Vector v = new Vector();
        Node current = node.getFirstChild();
        edu.hkust.clap.monitor.Monitor.loopBegin(683);
while (current != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(683);
{
            if ((current.getNodeType() == current.TEXT_NODE) && (current.getLocalName().equals(tagname)) && (current.getNamespaceURI() != null) && (current.getNamespaceURI().equals(WEBDAV.NAMESPACE_URI))) {
                v.addElement(current.getNodeValue());
            }
            current = current.getNextSibling();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(683);

        String array[] = new String[v.size()];
        v.copyInto(array);
        return array;
    }

    /**
     * Just for child, not the all tree.
     * @param tagname the tagname
     * @return a Vector of Node.
     */
    public Vector getDAVElementsByTagName(String tagname) {
        return getDAVElementsByTagName(element, tagname);
    }

    /**
     * Get the first node matching the given name
     * @param tagname the node name
     * @return a Node instance or null
     */
    public Node getDAVNode(String tagname) {
        return getDAVNode(element, tagname);
    }

    /**
     * Get the value of the first "DAV:" child text node (if any)
     * @param tagname the parent node name
     * @return a String instance or null
     */
    public String getTextChildValue(String tagname) {
        return getTextChildValue(getDAVNode(tagname));
    }

    /**
     * Get the text value of all our "DAV:" children matching the given
     * tagname with a text value available.
     * @param tagname the tagname to search
     * @return a String array
     */
    public String[] getMultipleTextChildValue(String tagname) {
        return getMultipleTextChildValue(element, tagname);
    }

    String nodenames[] = null;

    /**
     * Get the tagnames of all our DAV children
     * @return a String array
     */
    public String[] getDAVNodeNames() {
        if (nodenames == null) {
            Node current = element.getFirstChild();
            Vector v = new Vector();
            edu.hkust.clap.monitor.Monitor.loopBegin(684);
while (current != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(684);
{
                if ((current.getNodeType() == current.ELEMENT_NODE) && (current.getNamespaceURI() != null) && (current.getNamespaceURI().equals(WEBDAV.NAMESPACE_URI))) {
                    v.addElement(current.getLocalName());
                }
                current = current.getNextSibling();
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(684);

            nodenames = new String[v.size()];
            v.copyInto(nodenames);
        }
        return nodenames;
    }

    /**
     * Create a new node.
     * @param name the tagname of the new node
     * @param textvalue the nodevalue of the TextNode (child of the new node)
     * @return the newly added Element
     */
    public Element addDAVNode(String name, String textvalue) throws DOMException {
        return addDAVNode(element, name, textvalue);
    }

    /**
     * Add the given node the our children list
     * @param node the new child
     */
    public void addDAVNode(DAVNode node) {
        element.appendChild(node.getNode());
    }

    /**
     * Add the given nodes the our children list
     * @param nodes the new children
     */
    public void addDAVNodes(DAVNode nodes[]) {
        if (nodes != null) {
            int len = nodes.length;
            edu.hkust.clap.monitor.Monitor.loopBegin(685);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(685);
{
                element.appendChild(nodes[i].getNode());
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(685);

        }
    }

    public boolean equals(DAVNode node) {
        return (element == node.getNode());
    }

    /**
     * Constructor
     */
    DAVNode(Element element) {
        this.element = element;
    }
}
