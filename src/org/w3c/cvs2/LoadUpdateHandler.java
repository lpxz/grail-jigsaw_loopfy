package org.w3c.cvs2;

import java.io.File;
import java.io.FilenameFilter;

class LoadUpdateHandler extends UpdateHandler implements CVS {

    CvsDirectory cvs = null;

    String files[] = null;

    long stamp = -1;

    void notifyEnd() {
        edu.hkust.clap.monitor.Monitor.loopBegin(326);
for (int i = 0; i < files.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(326);
{
            if (files[i] != null) cvs.createFileEntry(stamp, files[i], FILE_OK);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(326);

    }

    void notifyEntry(String filename, int status) {
        File file = new File(cvs.getDirectory(), filename);
        if (file.isDirectory()) return;
        if (status == FILE_OK) status = file.exists() ? FILE_U : FILE_NCO;
        cvs.createFileEntry(stamp, filename, status);
        edu.hkust.clap.monitor.Monitor.loopBegin(327);
for (int i = 0; i < files.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(327);
{
            if (files[i] == null) continue;
            if (files[i].equals(filename)) {
                files[i] = null;
                return;
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(327);

    }

    LoadUpdateHandler(CvsDirectory cvs) {
        this.cvs = cvs;
        this.files = cvs.getDirectory().list(new FileFilter());
        this.stamp = System.currentTimeMillis();
    }
}
