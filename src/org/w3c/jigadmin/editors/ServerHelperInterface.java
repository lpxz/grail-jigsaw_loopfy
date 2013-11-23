package org.w3c.jigadmin.editors;

/**
 * The interface for server helpers.
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public interface ServerHelperInterface extends EditorInterface {

    /**
     * The tooltip property name
     */
    public static final String TOOLTIP_P = "tooltip";

    /**
     * Get the helper name.
     * @return a String instance
     */
    public String getName();

    /**
     * Get the helper tooltip
     * @return a String
     */
    public String getToolTip();
}
