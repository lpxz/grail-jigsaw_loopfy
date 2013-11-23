package org.w3c.www.webdav;

import java.util.Vector;
import org.w3c.www.http.BasicValue;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class DAVStatusURIList extends BasicValue {

    public static final boolean debug = false;

    DAVStatusURI statusURIs[] = null;

    protected void parse() {
        Vector vstatus = new Vector();
        if (debug) {
            System.out.println("PARSING STATUS URI HEADER");
        }
        ParseState list = new ParseState(0, raw.length);
        list.separator = (byte) ')';
        list.spaceIsSep = false;
        ParseState blist = new ParseState(0, 0);
        blist.separator = (byte) '(';
        blist.spaceIsSep = false;
        DAVStatusURI dsu = null;
        edu.hkust.clap.monitor.Monitor.loopBegin(387);
while (DAVParser.nextItem(raw, list) >= 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(387);
{
            blist.prepare(list);
            edu.hkust.clap.monitor.Monitor.loopBegin(388);
while (DAVParser.nextItem(raw, blist) >= 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(388);
{
                dsu = new DAVStatusURI(raw, blist.start, blist.end);
                addStatusURI(dsu);
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(388);

        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(387);

    }

    protected void updateByteValue() {
        if (statusURIs != null) {
            StringBuffer buf = new StringBuffer();
            edu.hkust.clap.monitor.Monitor.loopBegin(389);
for (int i = 0; i < statusURIs.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(389);
{
                DAVStatusURI dsu = statusURIs[i];
                buf.append("(").append(dsu.getStatus()).append(" ");
                buf.append("<").append(dsu.getURI()).append(">) ");
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(389);

            raw = buf.toString().getBytes();
            roff = 0;
            rlen = raw.length;
        } else {
            raw = new byte[0];
            roff = 0;
            rlen = 0;
        }
    }

    public void addStatusURI(DAVStatusURI su) {
        if (statusURIs == null) {
            statusURIs = new DAVStatusURI[1];
            statusURIs[0] = su;
        } else {
            int len = statusURIs.length;
            DAVStatusURI nsu[] = new DAVStatusURI[len + 1];
            System.arraycopy(statusURIs, 0, nsu, 0, len);
            nsu[len] = su;
            statusURIs = nsu;
        }
    }

    public Object getValue() {
        validate();
        return statusURIs;
    }

    /**
     * Don't use this constructor
     */
    public DAVStatusURIList() {
        this.isValid = false;
    }

    public DAVStatusURIList(DAVStatusURI dsu[]) {
        this.isValid = true;
        this.statusURIs = dsu;
    }

    public DAVStatusURIList(DAVStatusURI dsu) {
        this.isValid = true;
        this.statusURIs = new DAVStatusURI[1];
        statusURIs[0] = dsu;
    }
}
