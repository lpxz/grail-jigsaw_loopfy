package org.w3c.www.http;

import org.w3c.util.ArrayDictionary;

/**
 * This class parses name->value pairs, used in Authentication Info
 */
public class HttpParamList extends BasicValue {

    ArrayDictionary params = null;

    /**
     * parse.
     * @exception HttpParserException if parsing failed.
     */
    protected void parse() throws HttpParserException {
        ParseState ps = new ParseState(roff, rlen);
        this.params = new ArrayDictionary(4, 4);
        ps.prepare();
        ParseState it = new ParseState();
        it.separator = (byte) '=';
        ps.separator = (byte) ',';
        edu.hkust.clap.monitor.Monitor.loopBegin(1033);
while (HttpParser.nextItem(raw, ps) >= 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(1033);
{
            it.prepare(ps);
            if (HttpParser.nextItem(raw, it) < 0) error("Invalid param list: bad param name.");
            String key = it.toString(raw, true);
            it.prepare();
            if (HttpParser.nextItem(raw, it) < 0) error("Invalid param list: no param value.");
            it.ioff = it.start;
            HttpParser.unquote(raw, it);
            params.put(key, it.toString(raw));
            ps.prepare();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1033);

    }

    protected void updateByteValue() {
        HttpBuffer buf = new HttpBuffer();
        int len = params.size();
        edu.hkust.clap.monitor.Monitor.loopBegin(1034);
for (int i = 0; len > 0; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1034);
{
            String key = (String) params.keyAt(i);
            if (key == null) continue;
            buf.appendQuoted(key, (byte) '=', (String) params.elementAt(i));
            len--;
            if (len > 0) {
                buf.append((byte) ',');
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1034);

        raw = buf.getByteCopy();
        roff = 0;
        rlen = raw.length;
    }

    public Object getValue() {
        validate();
        return this;
    }

    /**
     * Get a parameter.
     * @param name The name of the parameter to fetch.
     * @return The String value, or <strong>null</strong> if undefined.
     */
    public String getParameter(String name) {
        validate();
        return (params == null) ? null : (String) params.get(name);
    }

    /**
     * Set an authentication parameter.
     * @param name The name of the authentication parameter.
     * @param value The value of the authentication parameter.
     */
    public void setParameter(String name, String value) {
        validate();
        if (params == null) params = new ArrayDictionary(4, 4);
        params.put(name, value);
    }

    public HttpParamList(boolean isValid) {
        this.isValid = isValid;
    }

    public HttpParamList() {
        this.isValid = false;
    }
}
