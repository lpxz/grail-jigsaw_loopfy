package org.w3c.cvs2;

class CvsCommitHandler extends CommitHandler implements CVS {

    CvsDirectory cvs = null;

    long stamp = -1;

    void notifyEntry(String filename, int status) {
        CvsEntry entry = cvs.getFileEntry(filename);
        if (entry == null) cvs.createFileEntry(stamp, filename, status); else entry.setStatus(stamp, status);
    }

    CvsCommitHandler(CvsDirectory cvs) {
        this.cvs = cvs;
        this.stamp = System.currentTimeMillis();
    }
}
