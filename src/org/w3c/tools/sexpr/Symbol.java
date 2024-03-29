package org.w3c.tools.sexpr;

import java.io.PrintStream;
import java.util.Dictionary;

/**
 * Base class for lisp-like symbols.
 */
public class Symbol implements SExpr {

    private String name;

    /**
   * Creates a symbol and potentially interns it in a symbol table.
   */
    public static Symbol makeSymbol(String name, Dictionary symbols) {
        if (symbols == null) return new Symbol(name); else {
            String key = name.toLowerCase();
            Symbol s = (Symbol) symbols.get(key);
            if (s == null) {
                s = new Symbol(name);
                symbols.put(key, s);
            }
            return s;
        }
    }

    protected Symbol(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    public void printExpr(PrintStream out) {
        out.print(toString());
    }
}
