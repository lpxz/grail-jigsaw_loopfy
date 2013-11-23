package org.w3c.jigadm.editors;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class LanguageAttributeModifier implements EditorModifier {

    /**
     * Modify the selected item, returns the 2 first characters
     * @return The modifier item
     */
    public String modify(String item) {
        return item.substring(0, 2);
    }

    /**
     * Create a new LanguageAttributeModifier
     */
    public LanguageAttributeModifier() {
    }
}
