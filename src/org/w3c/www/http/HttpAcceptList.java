package org.w3c.www.http;

import java.util.Vector;

public class HttpAcceptList extends BasicValue {

    HttpAccept accept[] = null;

    protected void parse() {
        Vector va = new Vector(4);
        ParseState ps = new ParseState(roff, rlen);
        ps.separator = (byte) ',';
        ps.spaceIsSep = false;
        edu.hkust.clap.monitor.Monitor.loopBegin(488);
while (HttpParser.nextItem(raw, ps) >= 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(488);
{
            HttpAccept a = new HttpAccept(this, raw, ps.start, ps.end);
            va.addElement(a);
            ps.prepare();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(488);

        accept = new HttpAccept[va.size()];
        va.copyInto(accept);
    }

    protected void updateByteValue() {
        HttpBuffer buf = new HttpBuffer();
        if (accept != null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(489);
for (int i = 0; i < accept.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(489);
{
                if (i > 0) buf.append(',');
                accept[i].appendValue(buf);
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(489);

            raw = buf.getByteCopy();
            roff = 0;
            rlen = raw.length;
        } else {
            raw = new byte[0];
            roff = 0;
            rlen = 0;
        }
    }

    public Object getValue() {
        validate();
        return accept;
    }

    /**
     * Add an accept clause to that list.
     * @param a The new accept clause, as an instance of HttpAccept.
     */
    public void addAccept(HttpAccept a) {
        if (accept == null) {
            accept = new HttpAccept[1];
            accept[0] = a;
        } else {
            HttpAccept newacc[] = new HttpAccept[accept.length + 1];
            System.arraycopy(accept, 0, newacc, 0, accept.length);
            newacc[accept.length] = a;
            accept = newacc;
        }
    }

    HttpAcceptList() {
        this.isValid = false;
    }

    HttpAcceptList(HttpAccept accept[]) {
        this.isValid = true;
        this.accept = accept;
    }
}
