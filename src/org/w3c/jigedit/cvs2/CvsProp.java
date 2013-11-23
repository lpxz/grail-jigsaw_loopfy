package org.w3c.jigedit.cvs2;

import org.w3c.jigsaw.http.httpd;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.StringAttribute;
import org.w3c.jigsaw.config.PropertySet;
import org.w3c.cvs2.CvsDirectory;
import org.w3c.www.protocol.http.HttpManager;

class CvsProp extends PropertySet {

    private static String title = "Cvs properties";

    static {
        Class c = null;
        Attribute a = null;
        try {
            c = Class.forName("org.w3c.jigedit.cvs.CvsProp");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        a = new StringAttribute(CvsDirectory.CVSPATH_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new StringAttribute(CvsDirectory.CVSROOT_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
        a = new StringAttribute(CvsDirectory.CVSWRAP_P, null, Attribute.EDITABLE);
        AttributeRegistry.registerAttribute(c, a);
    }

    /**
   * Get this property set title.
   * @return A String encoded title.
   */
    public String getTitle() {
        return title;
    }

    CvsProp(String name, httpd server) {
        super(name, server);
    }
}
