package org.w3c.cvs2;

import java.util.Enumeration;
import java.util.Vector;

class CvsStatusHandler extends StatusHandler {

    class RevisionEntry {

        String file = null;

        String rev = null;

        String st_opt = null;

        RevisionEntry(String file, String rev, String st_opt) {
            this.file = file;
            this.rev = rev;
            this.st_opt = st_opt;
        }
    }

    CvsDirectory cvs = null;

    Vector rentries = null;

    void notifyEnd() {
        Enumeration renum = rentries.elements();
        edu.hkust.clap.monitor.Monitor.loopBegin(1179);
while (renum.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(1179);
{
            RevisionEntry rentry = (RevisionEntry) renum.nextElement();
            CvsEntry entry = cvs.getFileEntry(rentry.file);
            if (entry != null) {
                entry.setRevision(rentry.rev);
                entry.setStickyOptions(rentry.st_opt);
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1179);

    }

    void notifyEntry(String filename, String revision, String st_opt) {
        rentries.addElement(new RevisionEntry(filename, revision, st_opt));
    }

    CvsStatusHandler(CvsDirectory cvs) {
        this.cvs = cvs;
        rentries = new Vector(10);
    }
}
