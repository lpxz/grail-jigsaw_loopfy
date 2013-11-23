package org.w3c.tools.widgets;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Stack;
import java.util.Vector;

/**
 * The TreeBrowser class.
 *
 * This class is a generic framework to browser any hierachical structure.
 *
 * <p>Genericity is obtained through the use of 'handlers': the TreeBrowser
 * itself does not perform any action in response to user events, but simply
 * forward them as <b>notifications</b> to <b>handlers</b>. Each item inserted
 * may have its own handler, but handlers may also (this is the most common
 * case) be shared between handlers.
 *
 * <p>Any item added in the Tree is displayed with an icon and a label. When a
 * handler receive a notification on a node, it may change this node, to modify
 * or update its appearance.
 *
 * @author Jean-Michel.Leon@sophia.inria.fr
 * @author Yves.Lafon@w3.org
 * @author Thierry.Kormann@sophia.inria.fr 
 */
public class TreeBrowser extends Canvas implements AdjustmentListener {

    /** 
     * Specifies that the horizontal/vertical scrollbars should always be shown
     * regardless of the respective sizes of the TreeBrowser.  
     */
    public static final int SCROLLBARS_ALWAYS = 0;

    /** 
     * Specifies that horizontal/vertical scrollbars should be shown only when
     * the size of the nodes exceeds the size of the TreeBrowser in the
     * horizontal/vertical dimension.  
     */
    public static final int SCROLLBARS_ASNEEDED = 1;

    /** 
     * This policy that lets just one node selected at the same time. 
     */
    public static final int SINGLE = 0;

    /** 
     * The policy that enables a multiple selection of nodes. 
     */
    public static final int MULTIPLE = 1;

    static final int HMARGIN = 5;

    static final int VMARGIN = 5;

    static final int HGAP = 10;

    static final int DXLEVEL = HGAP * 2;

    /**
     * The inner mouse listener in charge of all the node expansion
     * selection and execution
     */
    private class BrowserMouseListener extends MouseAdapter {

        private void clickAt(TreeNode node, MouseEvent me) {
            if (node == null) return;
            int x = me.getX() - HMARGIN;
            if (node.handler == null) return;
            if ((x >= node.level * DXLEVEL) && (x <= node.level * DXLEVEL + DXLEVEL)) {
                if (node.children != TreeNode.NOCHILD) {
                    node.handler.notifyCollapse(TreeBrowser.this, node);
                } else {
                    node.handler.notifyExpand(TreeBrowser.this, node);
                }
            } else if (x > node.level * DXLEVEL + HGAP) {
                node.handler.notifySelect(TreeBrowser.this, node);
            }
        }

        /**
	 * Handles events and send notifications ot handlers.
	 * is sent, depending on the node's current state.<br>
	 * on MOUSE_DOWN on a label, a <b>Select</b> notificaiton is sent.<br>
	 * on DOUBLE_CLICK on a label, an <b>Execute</b> notification is sent.
	 */
        public void mousePressed(MouseEvent me) {
            int y = me.getY() - VMARGIN;
            if (me.getClickCount() == 1) {
                clickAt(itemAt(y), me);
            }
        }

        public void mouseClicked(MouseEvent me) {
            if (me.getClickCount() > 1) {
                int y = me.getY() - VMARGIN;
                TreeNode node = itemAt(y);
                if ((node != null) && (node.handler != null)) {
                    node.handler.notifyExecute(TreeBrowser.this, node);
                }
            }
        }
    }

    private Scrollbar vscroll;

    private Scrollbar hscroll;

    private int maxwidth = 0;

    private int startx = 0;

    private Color selectColor = new Color(0, 0, 128);

    private Color selectFontColor = Color.white;

    private int scrollbarDisplayPolicy = SCROLLBARS_ASNEEDED;

    private boolean hierarchyChanged = true;

    protected Vector items;

    protected Vector selection;

    protected int topItem = 0;

    protected int visibleItemCount = 20;

    protected int selectionPolicy = SINGLE;

    protected int fontHeight;

    /**
    * Builds a new browser instance
    *
    * @param root the root node for this hierarchy
    * @param label the label that should be displayed for this item
    * @param handler the handler for this node
    * @param icon the icon that must be displayed for this item
    */
    public TreeBrowser(Object root, String label, NodeHandler handler, Image icon) {
        this();
        initialize(root, label, handler, icon);
    }

    protected TreeBrowser() {
        selection = new Vector(1, 1);
        items = new Vector();
        topItem = 0;
        addMouseListener(new BrowserMouseListener());
    }

    protected void initialize(Object item, String label, NodeHandler handler, Image icon) {
        items.addElement(new TreeNode(item, label, handler, icon, 0));
    }

    public Dimension getPreferredSize() {
        return new Dimension(200, 400);
    }

    /**
     * Sets the color of a selected node to the specified color.
     * @param color the color used to paint a selected node
     */
    public void setSelectionFontColor(Color color) {
        this.selectFontColor = color;
    }

    /**
     * Sets the background color of a selected node to the specified color.
     * @param color the color used to paint the background of a selected node
     */
    public void setSelectionBackgroudColor(Color color) {
        this.selectColor = color;
    }

    /**
     * Sets the scrollbars display policy to the specified policy. The default
     * is SCROLLBARS_ALWAYS
     * @param scrollbarDisplayPolicy SCROLLBARS_NEVER | SCROLLBARS_ASNEEDED |
     * SCROLLBARS_ALWAYS 
     */
    public void setScrollbarDisplayPolicy(int scrollbarDisplayPolicy) {
        this.scrollbarDisplayPolicy = scrollbarDisplayPolicy;
        hierarchyChanged = false;
    }

    /**
     * repaints the View.
     */
    public void paint(Graphics g) {
        fontHeight = g.getFontMetrics().getHeight();
        int fontAscent = g.getFontMetrics().getAscent();
        int itemCount = items.size();
        Dimension dim = getSize();
        int myHeight = dim.height - VMARGIN * 2;
        int myWidth = dim.width - HMARGIN * 2;
        g.clipRect(HMARGIN, VMARGIN, myWidth, myHeight);
        g.translate(HMARGIN, VMARGIN);
        int y = 0;
        int dx, fatherIndex;
        int level;
        Stack indexStack = new Stack();
        Graphics bg = g.create();
        bg.setColor(selectColor);
        g.setFont(getFont());
        visibleItemCount = 0;
        TreeNode node;
        level = -1;
        int labelwidth;
        if (hierarchyChanged) {
            maxwidth = 0;
        }
        edu.hkust.clap.monitor.Monitor.loopBegin(158);
for (int i = 0; i < topItem; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(158);
{
            node = (TreeNode) items.elementAt(i);
            if (hierarchyChanged) {
                dx = node.level * DXLEVEL;
                labelwidth = g.getFontMetrics().stringWidth(node.label);
                maxwidth = Math.max(maxwidth, dx + DXLEVEL + labelwidth);
            }
            if (node.level > level) {
                indexStack.push(new Integer(i - 1));
                level = node.level;
            }
            if (node.level < level) {
                edu.hkust.clap.monitor.Monitor.loopBegin(159);
for (int j = node.level; j < level; j++) { 
edu.hkust.clap.monitor.Monitor.loopInc(159);
indexStack.pop();} 
edu.hkust.clap.monitor.Monitor.loopEnd(159);

                level = node.level;
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(158);

        int nitems = myHeight / fontHeight;
        int ditems = itemCount - topItem;
        if (ditems < nitems) {
            topItem = Math.max(0, topItem - (nitems - ditems));
        }
        if (myWidth >= maxwidth) {
            startx = 0;
        } else if (startx + myWidth > maxwidth) {
            startx = (maxwidth - myWidth);
        }
        edu.hkust.clap.monitor.Monitor.loopBegin(160);
for (int i = topItem; i < itemCount; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(160);
{
            node = (TreeNode) items.elementAt(i);
            if (node.level > level) {
                indexStack.push(new Integer(i - 1));
                level = node.level;
            }
            if (node.level < level) {
                edu.hkust.clap.monitor.Monitor.loopBegin(159);
for (int j = node.level; j < level; j++) { 
edu.hkust.clap.monitor.Monitor.loopInc(159);
indexStack.pop();} 
edu.hkust.clap.monitor.Monitor.loopEnd(159);

                level = node.level;
            }
            dx = (node.level * DXLEVEL) - startx;
            if (y <= myHeight) {
                if (node.selected) {
                    bg.fillRect(dx, y - 1, Math.max(myWidth - 1, maxwidth - 1), fontHeight);
                    g.setColor(selectFontColor);
                    g.drawImage(node.icon, dx, y, this);
                    g.drawString(node.label, dx + DXLEVEL, y + fontAscent);
                    g.setColor(getForeground());
                } else {
                    g.setColor(getForeground());
                    g.drawImage(node.icon, dx, y, this);
                    g.drawString(node.label, dx + DXLEVEL, y + fontAscent);
                }
                fatherIndex = ((Integer) indexStack.peek()).intValue();
                if (fatherIndex != -1) {
                    int fi = fatherIndex - topItem;
                    g.drawLine(dx - HGAP / 2, y + fontHeight / 2, dx - DXLEVEL + HGAP / 2, y + fontHeight / 2);
                    if (node.handler.isDirectory(this, node)) {
                        g.drawRect(dx - DXLEVEL + HGAP / 2 - 2, y + fontHeight / 2 - 2, 4, 4);
                    }
                    g.drawLine(dx - DXLEVEL + HGAP / 2, y + fontHeight / 2, dx - DXLEVEL + HGAP / 2, (fi + 1) * fontHeight - 1);
                }
                visibleItemCount++;
            } else {
                fatherIndex = ((Integer) indexStack.peek()).intValue();
                if (fatherIndex != -1) {
                    int fi = fatherIndex - topItem;
                    if ((fi + 1) * fontHeight - 1 < myHeight) g.drawLine(dx - DXLEVEL + HGAP / 2, myHeight - 1, dx - DXLEVEL + HGAP / 2, (fi + 1) * fontHeight - 1);
                }
            }
            if (hierarchyChanged) {
                dx = (node.level * DXLEVEL);
                labelwidth = g.getFontMetrics().stringWidth(node.label);
                maxwidth = Math.max(maxwidth, dx + DXLEVEL + labelwidth);
            }
            y += fontHeight;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(160);

        if (hierarchyChanged) {
            edu.hkust.clap.monitor.Monitor.loopBegin(161);
for (int i = itemCount; i < items.size(); ++i) { 
edu.hkust.clap.monitor.Monitor.loopInc(161);
{
                node = (TreeNode) items.elementAt(i);
                dx = (node.level * DXLEVEL);
                labelwidth = g.getFontMetrics().stringWidth(node.label);
                maxwidth = Math.max(maxwidth, dx + DXLEVEL + labelwidth);
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(161);

        }
        hierarchyChanged = false;
        updateScrollbars();
    }

    /**
     * this should be private. having it protected is a present
     * for dummy VM that doesn't know that an inner class can access
     * private method of its parent class
     */
    protected TreeNode itemAt(int y) {
        edu.hkust.clap.monitor.Monitor.loopBegin(162);
for (int i = topItem; ((i < items.size()) && (y > 0)); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(162);
{
            if (y < fontHeight) {
                return (TreeNode) items.elementAt(i);
            }
            y -= fontHeight;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(162);

        return null;
    }

    public void update(Graphics pg) {
        Rectangle r = pg.getClipBounds();
        Graphics offgc;
        Image offscreen = null;
        Dimension d = getSize();
        offscreen = ImageCache.getImage(this, d.width, d.height);
        offgc = offscreen.getGraphics();
        if (r != null) {
            offgc.clipRect(r.x, r.y, r.width, r.height);
        }
        offgc.setColor(getBackground());
        offgc.fillRect(0, 0, d.width, d.height);
        offgc.setColor(getForeground());
        paint(offgc);
        pg.drawImage(offscreen, 0, 0, this);
    }

    /**
     * Inserts new node.
     *
     * @param parent the parent node.
     * @item the abstract object this node refers to. may be null.
     * @handler the node handler, that will receive notifications for this node
     * @label the label displayed in the list.
     * @icon the icon displayed in the list.
     */
    public void insert(TreeNode parent, Object item, NodeHandler handler, String label, Image icon) {
        boolean done;
        int j;
        if (parent == null) throw new IllegalArgumentException("null parent");
        if ((handler == null) && (label == null)) {
            throw new IllegalArgumentException("non-null item required");
        }
        if (handler == null) {
            handler = parent.handler;
        }
        if (label == null) {
            label = handler.toString();
        }
        if (parent.children == TreeNode.NOCHILD) {
            parent.children = 1;
        } else {
            parent.children += 1;
        }
        done = false;
        TreeNode node = null;
        int i = items.indexOf(parent) + parent.children;
        edu.hkust.clap.monitor.Monitor.loopBegin(163);
for (; (i < items.size() && ((TreeNode) items.elementAt(i)).level > parent.level); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(163);
{
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(163);

        items.insertElementAt(node = new TreeNode(item, label, handler, icon, parent.level + 1), i);
        hierarchyChanged = true;
        return;
    }

    /**
     * Removes the specified node.
     * This simply removes a node, without modifying its children if any. USE
     * WITH CAUTION.
     * @param node the node to remove
     */
    public void remove(TreeNode node) {
        int ind = items.indexOf(node);
        TreeNode t = null;
        edu.hkust.clap.monitor.Monitor.loopBegin(164);
while (ind >= 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(164);
{
            t = (TreeNode) items.elementAt(ind);
            if (t.level >= node.level) ind--; else {
                t.children--;
                break;
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(164);

        items.removeElement(node);
        if (node.selected) {
            unselect(node);
        }
        hierarchyChanged = true;
    }

    /**
     * Removes the specified node and its children.
     * NOTE: if two threads are doing adds and removes,
     * this can lead to IndexOutOfBound exception.
     * You will probably have to use locks to get rid of that problem
     * @param node the node to remove
     */
    public void removeBranch(TreeNode node) {
        int ist, iend;
        ist = items.indexOf(node) + 1;
        iend = items.size() - 1;
        edu.hkust.clap.monitor.Monitor.loopBegin(165);
for (int i = ist; i < iend; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(165);
{
            if (((TreeNode) items.elementAt(ist)).level > node.level) {
                remove((TreeNode) items.elementAt(ist));
            } else break;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(165);

        remove(node);
        hierarchyChanged = true;
    }

    /**
     * Contracts the representation of the specified node.
     * removes all the children nodes of 'item'. It is caller's
     * responsibility to call repaint() afterwards.
     * @param item the node to contracts
     */
    public synchronized void collapse(TreeNode item) {
        TreeNode node = (TreeNode) item;
        if (node.children != TreeNode.NOCHILD) {
            node.children = TreeNode.NOCHILD;
            edu.hkust.clap.monitor.Monitor.loopBegin(166);
for (int j = items.indexOf(item) + 1; j < items.size(); ) { 
edu.hkust.clap.monitor.Monitor.loopInc(166);
{
                TreeNode child = (TreeNode) items.elementAt(j);
                if (child.level > node.level) {
                    items.removeElementAt(j);
                    if (child.selected) {
                        unselect(child);
                    }
                } else {
                    hierarchyChanged = true;
                    return;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(166);

        }
    }

    /**
     * Sets the selection policy.
     * @param policy: SINGLE or MULTIPLE
     */
    public void setSelectionPolicy(int policy) {
        selectionPolicy = policy;
    }

    /**
     * Gets the selection policy.
     */
    public int getSelectionPolicy() {
        return selectionPolicy;
    }

    /**
     * Selects the specified node.
     * Selects the given node. If selectionPolicy is SINGLE any previously
     * selected node is unselected first.  It is caller's responsibility to
     * call repaint() 
     * @param node the node to select
     */
    public void select(TreeNode node) {
        if (node == null) return;
        if (selectionPolicy == SINGLE) {
            unselectAll();
        }
        selection.addElement(node);
        node.selected = true;
    }

    /**
     * Unselects the specified node.
     * It is caller's responsibility to call repaint()
     * @param node the node to unselect
     */
    public void unselect(TreeNode node) {
        if (node == null) return;
        selection.removeElement(node);
        node.selected = false;
    }

    /**
     * Unselects all selected items.
     */
    public void unselectAll() {
        edu.hkust.clap.monitor.Monitor.loopBegin(167);
for (Enumeration e = selection.elements(); e.hasMoreElements(); ) { 
edu.hkust.clap.monitor.Monitor.loopInc(167);
{
            TreeNode node = (TreeNode) e.nextElement();
            node.selected = false;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(167);

    }

    /**
     * Returns an Enumeraiton of selected items.
     */
    public Enumeration selection() {
        return selection.elements();
    }

    private void updateScrollbars() {
        int max = items.size() + 1;
        if (items.size() > visibleItemCount) {
            vscroll.setMaximum(max);
            vscroll.setVisibleAmount(visibleItemCount);
            vscroll.setVisible(true);
        } else {
            vscroll.setValue(0);
            vscroll.setMaximum(max);
            vscroll.setVisibleAmount(max);
            if (scrollbarDisplayPolicy == SCROLLBARS_ASNEEDED) {
                vscroll.setVisible(false);
            }
        }
        int myWidth = getSize().width - HMARGIN * 2;
        hscroll.setMaximum(maxwidth);
        hscroll.setVisibleAmount(myWidth);
        if (maxwidth > myWidth) {
            hscroll.setVisible(true);
        } else {
            if (scrollbarDisplayPolicy == SCROLLBARS_ASNEEDED) {
                hscroll.setVisible(false);
            }
        }
    }

    /**
     * Sets 'a' as vertical Scrollbar.
     * The Browser becomes an AdjustmentListener of this scrollbar.
     */
    public void setVerticalScrollbar(Scrollbar a) {
        vscroll = a;
        vscroll.addAdjustmentListener(this);
        vscroll.setMaximum(visibleItemCount);
        vscroll.setVisibleAmount(visibleItemCount);
        vscroll.setBlockIncrement(visibleItemCount);
    }

    /**
     * Sets 'a' as horizontal Scrollbar.
     * The Browser becomes an AdjustmentListener of this scrollbar.
     */
    public void setHorizontalScrollbar(Scrollbar a) {
        hscroll = a;
        hscroll.addAdjustmentListener(this);
        int myWidth = getSize().width - HMARGIN * 2;
        hscroll.setMaximum(myWidth);
        hscroll.setVisibleAmount(myWidth);
        hscroll.setBlockIncrement(20);
    }

    /**
     * Updates graphical appearance in response to a scroll.
     */
    public void adjustmentValueChanged(AdjustmentEvent evt) {
        if (evt.getSource() == vscroll) {
            topItem = evt.getValue();
        } else {
            startx = evt.getValue();
        }
        repaint();
    }

    /**
     * Returns the parent node of the specified node.
     * If 'child' is a valid node belonging to the Tree and has a parent node,
     * returns its parent. Returns null otherwise.
     * @param child the child node you want to get its parent
     */
    public TreeNode getParent(TreeNode child) {
        int n = items.indexOf(child);
        edu.hkust.clap.monitor.Monitor.loopBegin(168);
for (int i = n - 1; i >= 0; i--) { 
edu.hkust.clap.monitor.Monitor.loopInc(168);
{
            TreeNode node = (TreeNode) (items.elementAt(i));
            if (node.level < child.level) {
                return node;
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(168);

        return null;
    }

    /**
     * Gets the node associated to the specified object, or null if any.
     * @param obj the object related to a node
     */
    public TreeNode getNode(Object obj) {
        int imax = items.size();
        edu.hkust.clap.monitor.Monitor.loopBegin(169);
for (int i = 0; i < imax; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(169);
{
            if (obj.equals(((TreeNode) (items.elementAt(i))).getItem())) return (TreeNode) (items.elementAt(i));
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(169);

        return null;
    }
}
