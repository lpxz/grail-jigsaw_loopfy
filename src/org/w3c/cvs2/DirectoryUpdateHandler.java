package org.w3c.cvs2;

import java.io.File;

class DirectoryUpdateHandler extends UpdateHandler implements CVS {

    CvsDirectory cvs = null;

    long stamp = -1;

    void notifyEntry(String filename, int status) {
        File file = new File(cvs.getDirectory(), filename);
        File dir = new File(file.getParent());
        String name = file.getName();
        CvsDirectory child = null;
        try {
            child = CvsDirectory.getManager(cvs, dir);
        } catch (CvsException ex) {
            return;
        }
        CvsEntry entry = child.getFileEntry(name);
        if (entry == null) child.createFileEntry(stamp, name, status); else entry.setStatus(stamp, status);
    }

    DirectoryUpdateHandler(CvsDirectory cvs) {
        this.cvs = cvs;
        this.stamp = System.currentTimeMillis();
    }
}
