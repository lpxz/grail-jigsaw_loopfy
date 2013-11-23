package org.w3c.www.http;

import java.util.Vector;

public class HttpEntityTagList extends BasicValue {

    HttpEntityTag etags[] = null;

    protected void parse() {
        Vector vtags = new Vector(8);
        ParseState ps = new ParseState();
        ps.ioff = 0;
        ps.bufend = raw.length;
        ps.separator = (byte) ',';
        edu.hkust.clap.monitor.Monitor.loopBegin(442);
while (HttpParser.nextItem(raw, ps) >= 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(442);
{
            HttpEntityTag tag = new HttpEntityTag(this, raw, ps.start, ps.end);
            vtags.addElement(tag);
            ps.prepare();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(442);

        etags = new HttpEntityTag[vtags.size()];
        vtags.copyInto(etags);
    }

    protected void updateByteValue() {
        HttpBuffer buf = new HttpBuffer();
        if (etags != null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(443);
for (int i = 0; i < etags.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(443);
{
                if (i > 0) buf.append(',');
                etags[i].appendValue(buf);
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(443);

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
        return etags;
    }

    /**
     * Add a tag to that list.
     * @param tag The new tag to add.
     */
    public void addTag(HttpEntityTag tag) {
        if (etags == null) {
            etags = new HttpEntityTag[1];
            etags[0] = tag;
        } else {
            int len = etags.length;
            HttpEntityTag ntags[] = new HttpEntityTag[len + 1];
            System.arraycopy(etags, 0, ntags, 0, len);
            ntags[len] = tag;
            etags = ntags;
        }
    }

    HttpEntityTagList() {
        this.isValid = false;
    }

    HttpEntityTagList(HttpEntityTag etags[]) {
        this.isValid = true;
        this.etags = etags;
    }
}
