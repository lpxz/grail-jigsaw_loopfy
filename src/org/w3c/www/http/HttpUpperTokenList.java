package org.w3c.www.http;

/**
 * Parse a comma separated list of tokens.
 */
public class HttpUpperTokenList extends HttpTokenList {

    /**
     * Create a parsed token list, for emitting.
     */
    protected HttpUpperTokenList(String tokens[]) {
        super(tokens);
        this.casemode = CASE_UPPER;
    }

    /**
     * Create a token list from a comma separated list of tokens.
     */
    protected HttpUpperTokenList(String tokens) {
        super(tokens);
        this.casemode = CASE_UPPER;
    }

    /**
     * Create an empty token list for parsing.
     */
    protected HttpUpperTokenList() {
        super();
        this.casemode = CASE_UPPER;
    }
}
