package org.w3c.cvs2;

import java.io.File;
import java.io.FilenameFilter;

class DirectoryFilter implements FilenameFilter {

    public boolean accept(File dir, String name) {
        return ((!name.equals("CVS")) && (!name.equals("Attic")) && (new File(dir, name)).isDirectory());
    }

    DirectoryFilter() {
        super();
    }
}
