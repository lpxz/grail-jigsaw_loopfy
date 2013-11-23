package org.w3c.cvs2;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;

class FileEnumeration implements Enumeration {

    CvsEntry next = null;

    Enumeration cvsenum = null;

    private CvsEntry computeNextElement() {
        if (cvsenum == null) return (next = null);
        edu.hkust.clap.monitor.Monitor.loopBegin(386);
while ((next == null) && cvsenum.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(386);
{
            CvsEntry entry = (CvsEntry) cvsenum.nextElement();
            if (!entry.isdir) next = entry;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(386);

        return next;
    }

    public synchronized boolean hasMoreElements() {
        return ((next != null) || ((next = computeNextElement()) != null));
    }

    public synchronized Object nextElement() {
        if (next == null) {
            if ((next = computeNextElement()) == null) throw new NoSuchElementException("invalid enum");
        }
        CvsEntry item = next;
        next = null;
        return item.name;
    }

    FileEnumeration(Hashtable entries) {
        this.cvsenum = (entries != null) ? entries.elements() : null;
    }
}
