package org.w3c.cvs2;

abstract class StatusHandler implements CVS {

    abstract void notifyEntry(String filename, String revision, String st_opt);
}
