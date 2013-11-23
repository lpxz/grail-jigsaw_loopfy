package org.w3c.www.http;

import java.util.Vector;

public class HttpRangeList extends BasicValue {

    HttpRange ranges[] = null;

    protected void parse() {
        Vector vr = new Vector(2);
        ParseState ps = new ParseState(roff, rlen);
        ps.separator = (byte) ',';
        ps.spaceIsSep = false;
        edu.hkust.clap.monitor.Monitor.loopBegin(650);
while (HttpParser.nextItem(raw, ps) >= 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(650);
{
            vr.addElement(new HttpRange(this, raw, ps.start, ps.end));
            ps.prepare();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(650);

        if (vr.size() > 0) {
            ranges = new HttpRange[vr.size()];
            vr.copyInto(ranges);
        }
    }

    protected void updateByteValue() {
        HttpBuffer buf = new HttpBuffer();
        edu.hkust.clap.monitor.Monitor.loopBegin(651);
for (int i = 0; i < ranges.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(651);
{
            if (i > 0) buf.append(',');
            ranges[i].appendValue(buf);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(651);

        raw = buf.getByteCopy();
        roff = 0;
        rlen = raw.length;
    }

    public Object getValue() {
        validate();
        return ranges;
    }

    /**
     * Add a range to this list.
     * @param range The range to add.
     */
    public void addRange(HttpRange range) {
        if (ranges == null) {
            ranges = new HttpRange[1];
            ranges[0] = range;
        } else {
            int len = ranges.length;
            HttpRange nranges[] = new HttpRange[len + 1];
            System.arraycopy(ranges, 0, nranges, 0, len);
            nranges[len] = range;
            ranges = nranges;
        }
    }

    HttpRangeList(HttpRange ranges[]) {
        this.ranges = ranges;
        this.isValid = true;
    }

    HttpRangeList() {
        this.isValid = false;
    }
}
