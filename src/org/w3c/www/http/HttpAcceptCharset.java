package org.w3c.www.http;

public class HttpAcceptCharset extends BasicValue {

    String charset = null;

    double quality = 1.0;

    HttpAcceptCharsetList list = null;

    /**
     * parse.
     * @exception HttpParserException if parsing failed.
     */
    protected void parse() throws HttpParserException {
        ParseState ps = new ParseState(roff, rlen);
        ps.separator = (byte) ';';
        ps.spaceIsSep = false;
        if (HttpParser.nextItem(raw, ps) < 0) error("Invalid Accept-Charset: no charset.");
        this.charset = new String(raw, 0, ps.start, ps.end - ps.start);
        ps.prepare();
        ps.separator = (byte) '=';
        if (HttpParser.nextItem(raw, ps) < 0) {
            this.quality = 1.0;
        } else {
            ps.prepare();
            this.quality = HttpParser.parseQuality(raw, ps);
        }
    }

    protected void invalidateByteValue() {
        super.invalidateByteValue();
        if (list != null) list.invalidateByteValue();
    }

    protected void updateByteValue() {
        HttpBuffer buf = new HttpBuffer();
        buf.append(charset);
        buf.append(';');
        buf.append(quality);
        raw = buf.getByteCopy();
        roff = 0;
        rlen = raw.length;
    }

    public Object getValue() {
        validate();
        return this;
    }

    /**
     * Get this accept charset clause charset.
     * @return A String encoding the charset token.
     */
    public String getCharset() {
        validate();
        return charset;
    }

    /**
     * Set the charset accepted by this clause.
     * @param charset The accepted charset.
     */
    public void setCharset(String charset) {
        if (this.charset.equals(charset)) return;
        invalidateByteValue();
        this.charset = charset;
    }

    /**
     * Get the quality at which this charset is accepted.
     * @return A double value, encoding the quality, or <strong>1.0</strong>
     * if undefined.
     */
    public double getQuality() {
        validate();
        return quality;
    }

    /**
     * Set the quality under which this charset is accepted.
     * @param q The quality for this charset.
     */
    public void setQuality(double quality) {
        if (this.quality != quality) invalidateByteValue();
        this.quality = quality;
    }

    HttpAcceptCharset(HttpAcceptCharsetList list, byte raw[], int o, int l) {
        this.list = list;
        this.raw = raw;
        this.roff = o;
        this.rlen = l;
        this.isValid = false;
    }

    HttpAcceptCharset(boolean isValid, String charset, double quality) {
        this.isValid = isValid;
        setCharset(charset);
        setQuality(quality);
    }

    public HttpAcceptCharset() {
        this.isValid = false;
    }
}
