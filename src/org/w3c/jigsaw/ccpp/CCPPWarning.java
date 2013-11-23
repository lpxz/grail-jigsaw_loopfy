package org.w3c.jigsaw.ccpp;

import java.util.Vector;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class CCPPWarning {

    Vector warnings = null;

    public static final String CCPPWARNING_STATE = "org.w3c.jigsaw.ccpp.ccppwarning";

    protected String computeWarning(int warning, String reference) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(String.valueOf(warning)).append(" ");
        buffer.append(reference).append(" ");
        buffer.append("\"").append(CCPPRequest.getStandardWarning(warning));
        buffer.append("\"");
        return buffer.toString();
    }

    public void addWarning(int warning, String reference) {
        String token = computeWarning(warning, reference);
        warnings.addElement(token);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        edu.hkust.clap.monitor.Monitor.loopBegin(0);
for (int i = 0; i < warnings.size(); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(0);
{
            if (i != 0) {
                buffer.append(", ");
            }
            buffer.append(warnings.elementAt(i));
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(0);

        return buffer.toString();
    }

    public CCPPWarning() {
        warnings = new Vector();
    }
}
