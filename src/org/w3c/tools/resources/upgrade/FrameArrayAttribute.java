package org.w3c.tools.resources.upgrade;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.UnknownFrame;

public class FrameArrayAttribute extends Attribute {

    public boolean checkValue(Object value) {
        return value instanceof ResourceFrame[];
    }

    public int getPickleLength(Object value) {
        throw new RuntimeException("unused for upgrade");
    }

    public void pickle(DataOutputStream out, Object obj) throws IOException {
        throw new RuntimeException("unused for upgrade");
    }

    public Object unpickle(DataInputStream in) throws IOException {
        int cnt = in.readInt();
        if (cnt == 0) return null;
        ResourceFrame frames[] = new ResourceFrame[cnt];
        edu.hkust.clap.monitor.Monitor.loopBegin(531);
for (int i = 0; i < cnt; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(531);
{
            try {
                frames[i] = (ResourceFrame) Upgrader.readResource(in);
            } catch (UpgradeException ex) {
                frames[i] = new UnknownFrame();
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(531);

        return frames;
    }

    public FrameArrayAttribute(String name, ResourceFrame def[], Integer flags) {
        super(name, def, flags);
    }
}
