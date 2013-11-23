package org.w3c.www.mime;

/**
 * A Mime header factory, that will build instances of the MimeHeaders class
 * to hold MIME headers.
 */
public class MimeHeadersFactory implements MimeParserFactory {

    /**
     * Create a new header holder to hold the parser's result.
     * @param parser The parser that has something to parse.
     * @return A MimeParserHandler compliant object.
     */
    public MimeHeaderHolder createHeaderHolder(MimeParser parser) {
        return new MimeHeaders(parser);
    }
}
