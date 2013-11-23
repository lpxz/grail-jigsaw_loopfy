package org.w3c.www.protocol.http.proxy;

/**
 * Exception thrown when parsing the rule file fails.
 */
public class RuleParserException extends Exception {

    public RuleParserException(String msg) {
        super(msg);
    }
}
