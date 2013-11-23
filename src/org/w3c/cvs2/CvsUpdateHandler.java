package org.w3c.cvs2;

class CvsUpdateHandler extends UpdateHandler implements CVS {

    CvsDirectory cvs = null;

    long stamp = -1;

    void notifyEntry(String filename, int status) {
        CvsEntry entry = cvs.getFileEntry(filename);
        if (entry == null) cvs.createFileEntry(stamp, filename, status); else entry.setStatus(stamp, status);
    }

    CvsUpdateHandler(CvsDirectory cvs) {
        this.cvs = cvs;
        this.stamp = System.currentTimeMillis();
    }
}
