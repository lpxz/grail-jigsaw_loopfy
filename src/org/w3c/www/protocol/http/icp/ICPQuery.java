package org.w3c.www.protocol.http.icp;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;

class ICPQuery extends ICPMessage {

    protected int getByteArrayLength() {
        return (super.getByteArrayLength() + 4 + 4 + ((url == null) ? 0 : url.toExternalForm().length()) + 1);
    }

    protected int toByteArray(byte buf[]) {
        int off = super.getByteArrayLength();
        String strurl = (url == null) ? null : url.toExternalForm();
        int urllen = ((strurl == null) ? 0 : strurl.length());
        super.toByteArray(buf);
        off += 4;
        off += 4;
        if (urllen > 0) strurl.getBytes(0, urllen, buf, off);
        buf[off + urllen] = 0;
        return off + urllen + 1;
    }

    protected int parse(byte buf[], int off, int len) throws ICPProtocolException {
        off = super.parse(buf, off, len);
        off += 4;
        off += 4;
        edu.hkust.clap.monitor.Monitor.loopBegin(1111);
for (int i = off; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1111);
{
            if (buf[i] == 0) {
                if (i - off < 1) return i;
                String strurl = new String(buf, 0, off, i - off);
                try {
                    this.url = new URL(strurl);
                } catch (MalformedURLException ex) {
                    throw new ICPProtocolException("Invalid URL:" + strurl);
                }
                return i;
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1111);

        throw new ICPProtocolException("Invalid URL encoding");
    }

    /**
     * Create an ICP query.
     * @param id The identifier of the message.
     * @param url The queried URL.
     */
    ICPQuery(int id, URL url) {
        this.opcode = ICP_OP_QUERY;
        this.id = id;
        this.url = url;
    }

    ICPQuery(InetAddress addr, int port, int opcode, int version, byte buf[], int off, int len) throws ICPProtocolException {
        this.addr = addr;
        this.port = port;
        this.opcode = opcode;
        this.version = version;
        parse(buf, off, len);
    }
}
