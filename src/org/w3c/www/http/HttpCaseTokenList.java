package org.w3c.www.http;

/**
 * Parse a comma separated list of tokens.
 */
public class HttpCaseTokenList extends HttpTokenList {

    /**
     * Create a parsed token list, for emitting.
     */
    protected HttpCaseTokenList(String tokens[]) {
        super(tokens);
        this.casemode = CASE_ASIS;
    }

    /**
     * Create a token list from a comma separated list of tokens.
     */
    protected HttpCaseTokenList(String tokens) {
        super(tokens);
        this.casemode = CASE_ASIS;
    }

    /**
     * Create an empty token list for parsing.
     */
    protected HttpCaseTokenList() {
        super();
        this.casemode = CASE_ASIS;
    }
}
