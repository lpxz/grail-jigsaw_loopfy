package org.w3c.tools.resources.upgrade;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.w3c.util.CountOutputStream;
import org.w3c.jigsaw.ssi.Segment;

/**
 * Attribute used to make the document segment information persistent.
 * @author Antonio Ramirez <anto@mit.edu>
 */
class SegmentArrayAttribute extends Attribute {

    public boolean checkValue(Object value) {
        return (value instanceof Segment[] || value == null);
    }

    /**
     * Get the number of bytes required to save that attribute value.
     * @param The value about to be pickled.
     * @return The number of bytes needed to pickle that value.
     */
    public final int getPickleLength(Object value) {
        CountOutputStream out = new CountOutputStream();
        DataOutputStream dout = new DataOutputStream(out);
        Segment ss[] = (Segment[]) value;
        try {
            pickle(dout, ss);
            dout.close();
            return out.getCount();
        } catch (IOException ex) {
            throw new RuntimeException("IO erred in CountOutputStream.");
        }
    }

    public void pickle(DataOutputStream out, Object obj) throws IOException {
        Segment[] segs = (Segment[]) obj;
        out.writeInt(segs.length);
        edu.hkust.clap.monitor.Monitor.loopBegin(170);
for (int i = 0; i < segs.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(170);
segs[i].pickle(out);} 
edu.hkust.clap.monitor.Monitor.loopEnd(170);

    }

    public Object unpickle(DataInputStream in) throws IOException {
        int n = in.readInt();
        Segment segs[] = new Segment[n];
        edu.hkust.clap.monitor.Monitor.loopBegin(171);
for (int i = 0; i < n; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(171);
segs[i] = Segment.unpickle(in);} 
edu.hkust.clap.monitor.Monitor.loopEnd(171);

        return segs;
    }

    public SegmentArrayAttribute(String name, Segment[] def, Integer flags) {
        super(name, def, flags);
        this.type = "[Lorg.w3c.jigsaw.ssi.Segment;";
    }

    public String stringify(Object value) {
        Segment[] segs = (Segment[]) value;
        StringBuffer buf = new StringBuffer(160);
        buf.append('[');
        edu.hkust.clap.monitor.Monitor.loopBegin(172);
for (int i = 0; i < segs.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(172);
{
            buf.append(segs[i].toString());
            buf.append(' ');
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(172);

        buf.append(']');
        return buf.toString();
    }
}
