package org.w3c.www.webdav;

import java.util.LinkedList;
import java.util.ListIterator;
import org.w3c.www.http.HttpInvalidValueException;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class DAVIf {

    private String taggedResource = null;

    private LinkedList lists = null;

    public void addList(byte raw[], int start, int end) throws HttpInvalidValueException {
        LinkedList list = new LinkedList();
        boolean isnot = false;
        edu.hkust.clap.monitor.Monitor.loopBegin(496);
for (int i = start; i < end; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(496);
{
            int idx = i + 1;
            switch(raw[i]) {
                case (byte) '<':
                    edu.hkust.clap.monitor.Monitor.loopBegin(497);
while ((raw[i] != (byte) '>') && (i <= end)) { 
edu.hkust.clap.monitor.Monitor.loopInc(497);
{
                        i++;
                    }} 
edu.hkust.clap.monitor.Monitor.loopEnd(497);

                    String state = new String(raw, idx, i - idx);
                    DAVStateToken token = new DAVStateToken(state, isnot);
                    list.add(token);
                    isnot = false;
                    break;
                case (byte) '[':
                    edu.hkust.clap.monitor.Monitor.loopBegin(498);
while ((raw[i] != (byte) ']') && (i <= end)) { 
edu.hkust.clap.monitor.Monitor.loopInc(498);
{
                        i++;
                    }} 
edu.hkust.clap.monitor.Monitor.loopEnd(498);

                    DAVEntityTag etag = new DAVEntityTag(raw, idx, i - idx, isnot);
                    list.add(etag);
                    isnot = false;
                    break;
                case (byte) 'N':
                    if (i > end - 2) {
                        throw new HttpInvalidValueException("Invalid header");
                    }
                    isnot = ((raw[i + 1] == (byte) 'o') && (raw[i + 2] == (byte) 't'));
                    i += 2;
                    break;
                case (byte) ' ':
                case (byte) '\t':
                    break;
                default:
                    String msg = "got '" + (char) raw[i] + "'";
                    throw new HttpInvalidValueException("Invalid header: " + msg);
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(496);

        lists.add(list);
    }

    public boolean hasResource() {
        return (taggedResource != null);
    }

    public String getResource() {
        return taggedResource;
    }

    public ListIterator getTokenListIterator() {
        return lists.listIterator(0);
    }

    DAVIf() {
        this.lists = new LinkedList();
    }

    DAVIf(String resource) throws HttpInvalidValueException {
        this.taggedResource = DAVParser.decodeURL(resource);
        this.lists = new LinkedList();
    }

    /**
     * Public Constructor
     * @param resource the tagged resource (can be null)
     * @param taglist a List of DAVStateToken and/or DAVEntityTag
     * @see DAVStateToken
     * @see DAVEntityTag
     */
    public DAVIf(String resource, LinkedList taglist) {
        this.lists = taglist;
        this.taggedResource = resource;
    }
}
