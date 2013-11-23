package org.w3c.tools.widgets;

/**
 * The interface to be implemented by nodes.
 * What is a node is application dependent, however, the informations the
 * browser needs in order to be able do display nodes are obtained through 
 * this interface.
 *
 * @see TreeBrowser
 */
public interface NodeHandler {

    /**
    * Notifies that a node has to be selected.
    *
    * @param browser the TreeBrowser sending the notification.
    */
    public void notifySelect(TreeBrowser browser, TreeNode node);

    /**
    * Notifies that a node has to be expanded.
    *
    * @param browser the TreeBrowser sending the notification.
    */
    public void notifyExpand(TreeBrowser browser, TreeNode node);

    /**
    * Notifies that a node has to be collapsed.
    *
    * @param browser the TreeBrowser sending the notification.
    */
    public void notifyCollapse(TreeBrowser browser, TreeNode node);

    /**
    * Notifies that a node has to be executed.
    *
    * @param browser the TreeBrowser sending the notification.
    */
    public void notifyExecute(TreeBrowser browser, TreeNode node);

    /**
    * Checks if the node is a directory.
    *
    * @param browser the TreeBrowser sending the notification.
    */
    public boolean isDirectory(TreeBrowser browser, TreeNode node);
}
