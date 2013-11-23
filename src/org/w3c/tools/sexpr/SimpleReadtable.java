package org.w3c.tools.sexpr;

/**
 * Basic implementation of the Readtable interface, a dispatch table.
 */
public class SimpleReadtable implements Readtable {

    private SExprParser parsers[];

    /**
   * Initializes an empty dispatch table (no associations).
   */
    public SimpleReadtable() {
        this.parsers = new SExprParser[256];
    }

    /**
   * Copy constructor.
   */
    public SimpleReadtable(SimpleReadtable table) {
        this.parsers = new SExprParser[256];
        edu.hkust.clap.monitor.Monitor.loopBegin(242);
for (int i = 0; i < 256; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(242);
this.parsers[i] = table.parsers[i];} 
edu.hkust.clap.monitor.Monitor.loopEnd(242);

    }

    public SExprParser getParser(char key) {
        return parsers[(int) key];
    }

    public SExprParser addParser(char key, SExprParser parser) {
        parsers[(int) key] = parser;
        return parser;
    }
}
