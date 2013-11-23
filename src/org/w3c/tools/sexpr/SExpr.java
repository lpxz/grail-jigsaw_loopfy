package org.w3c.tools.sexpr;

import java.io.PrintStream;

/**
 * Interface for all new s-expression subtypes.
 */
public interface SExpr {

    /**
   * Print a representation of the s-expression into the output stream.
   */
    public void printExpr(PrintStream out);
}
