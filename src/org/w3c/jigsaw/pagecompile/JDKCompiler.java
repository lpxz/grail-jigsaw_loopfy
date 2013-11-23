package org.w3c.jigsaw.pagecompile;

import java.io.OutputStream;
import java.io.PrintStream;
import sun.tools.javac.Main;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class JDKCompiler implements PageCompiler {

    /**
     * compile some files.
     * @param args The compiler arguments (files+options)
     * @param out The outputStream, the compiler will write its output
     * in it.
     * @return false if compilation failed.
     */
    public boolean compile(String args[], OutputStream out) {
        if (out == null) out = System.out;
        int len = args.length;
        String newargs[] = new String[len + 2];
        System.arraycopy(args, 0, newargs, 0, len);
        newargs[len] = "-classpath";
        newargs[len + 1] = System.getProperty("java.class.path");
        return (new Main(out, "compiler")).compile(newargs);
    }

    public static void main(String args[]) {
        (new JDKCompiler()).compile(args, null);
    }
}
