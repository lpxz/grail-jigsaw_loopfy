package org.w3c.www.http;

import java.util.Vector;

public class HttpAcceptCharsetList extends BasicValue {

    HttpAcceptCharset charsets[] = null;

    protected void parse() {
        Vector vl = new Vector(4);
        ParseState ps = new ParseState(roff, rlen);
        ps.separator = (byte) ',';
        ps.spaceIsSep = false;
        edu.hkust.clap.monitor.Monitor.loopBegin(1177);
while (HttpParser.nextItem(raw, ps) >= 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(1177);
{
            vl.addElement(new HttpAcceptCharset(this, raw, ps.start, ps.end));
            ps.prepare();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1177);

        charsets = new HttpAcceptCharset[vl.size()];
        vl.copyInto(charsets);
    }

    protected void updateByteValue() {
        HttpBuffer buf = new HttpBuffer();
        if (charsets != null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(1178);
for (int i = 0; i < charsets.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1178);
{
                if (i > 0) buf.append(',');
                charsets[i].appendValue(buf);
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1178);

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
        return charsets;
    }

    /**
     * Add an accpted charset clause to the list.
     * @param charset The new accepted charset clause.
     */
    public void addCharset(HttpAcceptCharset charset) {
        if (charsets == null) {
            charsets = new HttpAcceptCharset[1];
            charsets[0] = charset;
        } else {
            int len = charsets.length;
            HttpAcceptCharset newset[] = new HttpAcceptCharset[len + 1];
            System.arraycopy(charsets, 0, newset, 0, len);
            newset[len] = charset;
            charsets = newset;
        }
    }

    HttpAcceptCharsetList() {
        this.isValid = false;
    }

    HttpAcceptCharsetList(HttpAcceptCharset charsets[]) {
        this.isValid = isValid;
        this.charsets = charsets;
    }
}
