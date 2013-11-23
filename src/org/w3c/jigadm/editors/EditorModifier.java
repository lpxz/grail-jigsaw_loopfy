package org.w3c.jigadm.editors;

public interface EditorModifier {

    /**
     * Modify the selected item
     * @return The modifier item
     */
    public String modify(String item);
}
