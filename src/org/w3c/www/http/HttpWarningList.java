package org.w3c.www.http;

import java.util.Vector;

public class HttpWarningList extends BasicValue {

    HttpWarning warnings[] = null;

    protected void parse() {
        Vector ws = new Vector(4);
        ParseState ps = new ParseState(roff, rlen);
        ps.spaceIsSep = false;
        edu.hkust.clap.monitor.Monitor.loopBegin(54);
while (HttpParser.nextItem(raw, ps) >= 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(54);
{
            ws.addElement(new HttpWarning(this, raw, ps.start, ps.end));
            ps.prepare();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(54);

        warnings = new HttpWarning[ws.size()];
        ws.copyInto(warnings);
    }

    protected void updateByteValue() {
        HttpBuffer buf = new HttpBuffer();
        edu.hkust.clap.monitor.Monitor.loopBegin(55);
for (int i = 0; i < warnings.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(55);
{
            if (i > 0) buf.append(',');
            warnings[i].appendValue(buf);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(55);

        raw = buf.getByteCopy();
        roff = 0;
        rlen = raw.length;
    }

    public Object getValue() {
        validate();
        return warnings;
    }

    /**
     * Add a warning to that list.
     * @param w The warning to add.
     */
    public void addWarning(HttpWarning w) {
        if (warnings == null) {
            warnings = new HttpWarning[1];
            warnings[0] = w;
        } else {
            int len = warnings.length;
            HttpWarning newwarn[] = new HttpWarning[len + 1];
            System.arraycopy(warnings, 0, newwarn, 0, len);
            newwarn[len] = w;
            warnings = newwarn;
        }
    }

    HttpWarningList() {
        this.isValid = false;
    }

    HttpWarningList(HttpWarning warnings[]) {
        this.isValid = true;
        this.warnings = warnings;
    }

    HttpWarningList(HttpWarning warning) {
        this.isValid = true;
        this.warnings = new HttpWarning[1];
        this.warnings[0] = warning;
    }
}
