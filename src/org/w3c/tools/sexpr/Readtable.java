package org.w3c.tools.sexpr;

/**
 * An interface for read tables.
 */
public interface Readtable {

    /**
   * Find the parser associated with the <i>key</i> dispatch character.
   */
    public SExprParser getParser(char key);

    /**
   * Associate a parser with the <i>key</i> dispatch character.
   */
    public SExprParser addParser(char key, SExprParser parser);
}
