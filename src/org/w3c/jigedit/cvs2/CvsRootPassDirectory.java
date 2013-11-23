package org.w3c.jigedit.cvs2;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.StringAttribute;
import org.w3c.jigsaw.resources.PassDirectory;

/**
 * @version $Revision: 1.2 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class CvsRootPassDirectory extends org.w3c.jigsaw.resources.PassDirectory {

    /**
     * Attribute index, The cvs root.
     */
    public static int ATTR_CVSROOT = -1;

    static {
        Attribute a = null;
        Class cls = null;
        try {
            cls = Class.forName("org.w3c.jigedit.cvs.CvsRootPassDirectory");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        a = new StringAttribute("cvs-root", null, Attribute.EDITABLE);
        ATTR_CVSROOT = AttributeRegistry.registerAttribute(cls, a);
    }

    protected String getCvsRoot() {
        return getString(ATTR_CVSROOT, null);
    }

    public void setValue(int idx, Object value) {
        super.setValue(idx, value);
        if (idx == ATTR_CVSROOT) {
            CvsModule.setValue(getContext(), CvsModule.CVSROOT, value);
        }
    }

    /**
     * Initialize this resource with the given set of attributes.
     * @param values The attribute values.
     */
    public void initialize(Object values[]) {
        super.initialize(values);
        disableEvent();
        String cvsroot = getCvsRoot();
        if (cvsroot != null) CvsModule.setValue(getContext(), CvsModule.CVSROOT, cvsroot);
        enableEvent();
    }
}
