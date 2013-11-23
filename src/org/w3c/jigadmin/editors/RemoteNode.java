package org.w3c.jigadmin.editors;

/**
 * The interface for remote nodes.
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public interface RemoteNode {

    /**
     * Invoked whenever this node is about to be expanded.
     */
    public void nodeWillExpand();

    /**
     * Invoked whenever this node is about to be collapsed.
     */
    public void nodeWillCollapse();
}
