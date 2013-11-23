package org.w3c.tools.resources.serialization;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class EmptyDescription extends ResourceDescription {

    /**
     * Constructor. build the description as an empty description
     * only classname and identifeir are provided.
     * @param classname the resource class name
     */
    public EmptyDescription(String classname, String identifier) {
        super(classname);
        this.identifier = identifier;
        this.classes = null;
        this.attributes = null;
    }
}
