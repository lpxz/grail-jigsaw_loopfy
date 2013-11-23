package org.w3c.jigadmin.editors;

import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import org.w3c.jigadmin.gui.Message;

public abstract class ResourceHelper extends org.w3c.jigadm.editors.ResourceHelper {

    /**
     * Show an error message.
     * @param msg the error message.
     * @param ex an Exception instance.
     */
    protected void errorPopup(String msg, Exception ex) {
        Message.showErrorMessage(getComponent(), ex, msg);
    }

    /**
     * Show a message.
     * @param msg the message.
     */
    protected void msgPopup(String msg) {
        Message.showInformationMessage(getComponent(), msg);
    }
}
