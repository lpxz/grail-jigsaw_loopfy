package org.w3c.www.mime;

/**
 * This class is used by the MimeParser, to create new MIME message holders.
 * Each MIME parse instances is custmozied wit hits own factory, which it
 * will use to create MIME header holders.
 */
public interface MimeParserFactory {

    /**
     * Create a new header holder to hold the parser's result.
     * @param parser The parser that has something to parse.
     * @return A MimeParserHandler compliant object.
     */
    public abstract MimeHeaderHolder createHeaderHolder(MimeParser parser);
}
