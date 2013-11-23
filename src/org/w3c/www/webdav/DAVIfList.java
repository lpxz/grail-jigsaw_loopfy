package org.w3c.www.webdav;

import java.util.Vector;
import java.util.LinkedList;
import java.util.ListIterator;
import org.w3c.www.http.BasicValue;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class DAVIfList extends BasicValue {

    public static final boolean debug = false;

    DAVIf davifs[] = null;

    boolean tagged = false;

    protected void parse() {
        Vector vdavifs = new Vector();
        if (debug) {
            System.out.println("PARSING IF HEADER");
        }
        ParseState list = new ParseState(0, raw.length);
        list.separator = (byte) ')';
        list.spaceIsSep = false;
        ParseState blist = new ParseState(0, 0);
        blist.separator = (byte) '(';
        blist.spaceIsSep = false;
        ParseState st = new ParseState(0, 0);
        st.separator = (byte) '>';
        st.spaceIsSep = false;
        DAVIf davif = null;
        if (DAVParser.nextItem(raw, list) >= 0) {
            if (DAVParser.startsWith(raw, list, '<')) {
                tagged = true;
                do {
                    if (DAVParser.startsWith(raw, list, '<')) {
                        davif = null;
                    }
                    blist.prepare(list);
                    edu.hkust.clap.monitor.Monitor.loopBegin(910);
while (DAVParser.nextItem(raw, blist) >= 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(910);
{
                        if ((DAVParser.startsWith(raw, blist, '<')) && (davif == null)) {
                            davif = new DAVIf(blist.toString(raw));
                            vdavifs.addElement(davif);
                            if (debug) {
                                System.out.println("Res : " + davif.getResource());
                            }
                        } else {
                            if (debug) {
                                String slist = blist.toString(raw);
                                System.out.println("LIST : " + slist);
                            }
                            davif.addList(raw, blist.start, blist.end);
                        }
                    }} 
edu.hkust.clap.monitor.Monitor.loopEnd(910);

                } while (DAVParser.nextItem(raw, list) >= 0);
            } else {
                tagged = false;
                davif = new DAVIf();
                vdavifs.addElement(davif);
                do {
                    blist.prepare(list);
                    edu.hkust.clap.monitor.Monitor.loopBegin(911);
while (DAVParser.nextItem(raw, blist) >= 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(911);
{
                        if (debug) {
                            System.out.println(blist.toString(raw));
                        }
                        davif.addList(raw, blist.start, blist.end);
                    }} 
edu.hkust.clap.monitor.Monitor.loopEnd(911);

                } while (DAVParser.nextItem(raw, list) >= 0);
            }
        }
        davifs = new DAVIf[vdavifs.size()];
        vdavifs.copyInto(davifs);
    }

    protected void updateByteValue() {
        if (davifs != null) {
            StringBuffer buf = new StringBuffer();
            edu.hkust.clap.monitor.Monitor.loopBegin(912);
for (int i = 0; i < davifs.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(912);
{
                DAVIf davif = davifs[i];
                if (davif.hasResource()) {
                    buf.append("<").append(davif.getResource()).append("> ");
                }
                ListIterator iterator = davif.getTokenListIterator();
                edu.hkust.clap.monitor.Monitor.loopBegin(913);
while (iterator.hasNext()) { 
edu.hkust.clap.monitor.Monitor.loopInc(913);
{
                    LinkedList list = (LinkedList) iterator.next();
                    ListIterator it = list.listIterator(0);
                    buf.append("(");
                    edu.hkust.clap.monitor.Monitor.loopBegin(914);
while (it.hasNext()) { 
edu.hkust.clap.monitor.Monitor.loopInc(914);
{
                        Object token = (String) it.next();
                        String item = token.toString();
                        if (it.hasNext()) {
                            buf.append(item).append(" ");
                        } else {
                            buf.append(item);
                        }
                    }} 
edu.hkust.clap.monitor.Monitor.loopEnd(914);

                    buf.append(")");
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(913);

            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(912);

            raw = buf.toString().getBytes();
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
        return davifs;
    }

    public boolean isTaggedList() {
        return tagged;
    }

    public DAVIfList() {
        this.isValid = false;
    }
}
