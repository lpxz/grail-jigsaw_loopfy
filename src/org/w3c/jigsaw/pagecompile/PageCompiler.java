package org.w3c.jigsaw.pagecompile;

import java.io.OutputStream;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public interface PageCompiler {

    /**
     * compile some files.
     * @param args The compiler arguments (files+options)
     * @param out The outputStream, the compiler will write its output
     * in it.
     * @return false if compilation failed.
     */
    public boolean compile(String args[], OutputStream out);
}
