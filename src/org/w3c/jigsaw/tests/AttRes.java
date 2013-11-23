package org.w3c.jigsaw.tests;

import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceException;
import org.w3c.jigsaw.frames.MimeTypeArrayAttribute;
import org.w3c.www.mime.MimeType;

/**
 * @version $Revision: 1.2 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class AttRes extends FramedResource {

    /**
     * Attribute index - The MTA attribute
     */
    protected static int ATTR_MTA = -1;

    static {
        Attribute a = null;
        Class cls = null;
        try {
            cls = Class.forName("org.w3c.jigsaw.tests.AttRes");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        a = new MimeTypeArrayAttribute("mime-type-array", null, Attribute.EDITABLE);
        ATTR_MTA = AttributeRegistry.registerAttribute(cls, a);
    }

    public MimeType[] getMimeTypeArray() {
        return (MimeType[]) getValue(ATTR_MTA, null);
    }

    public ReplyInterface perform(RequestInterface request) throws ProtocolException, ResourceException {
        MimeType mimes[] = getMimeTypeArray();
        System.out.println(mimes);
        edu.hkust.clap.monitor.Monitor.loopBegin(748);
for (int i = 0; i < mimes.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(748);
System.out.println("=> " + mimes[i]);} 
edu.hkust.clap.monitor.Monitor.loopEnd(748);

        return super.perform(request);
    }
}
