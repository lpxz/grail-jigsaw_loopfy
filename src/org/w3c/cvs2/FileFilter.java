package org.w3c.cvs2;

import java.io.File;
import java.io.FilenameFilter;

class FileFilter implements FilenameFilter {

    public boolean accept(File dir, String name) {
        boolean reject = (name.startsWith(".") || name.endsWith("~") || (new File(dir, name)).isDirectory());
        return !reject;
    }
}
