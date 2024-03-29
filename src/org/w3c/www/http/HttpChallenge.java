package org.w3c.www.http;

import org.w3c.util.ArrayDictionary;
import java.lang.CloneNotSupportedException;

public class HttpChallenge extends BasicValue {

    String scheme = null;

    ArrayDictionary params = null;

    ArrayDictionary unqparams = null;

    /**
     * get a copy of the challenge, used to add some output value
     * without hurting the challenge
     * @return the clone of this challenge
     */
    public HttpChallenge getClone() {
        try {
            return (HttpChallenge) this.clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }

    /**
     * parse.
     * @exception HttpParserException if parsing failed.
     */
    protected void parse() throws HttpParserException {
        ParseState ps = new ParseState(roff, rlen);
        if (HttpParser.nextItem(raw, ps) < 0) error("Invalid challenge: no scheme.");
        this.scheme = ps.toString(raw);
        ParseState it = new ParseState();
        it.separator = (byte) '=';
        ps.separator = (byte) ',';
        ps.prepare();
        edu.hkust.clap.monitor.Monitor.loopBegin(554);
while (HttpParser.nextItem(raw, ps) >= 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(554);
{
            it.prepare(ps);
            if (HttpParser.nextItem(raw, it) < 0) error("Invalid challenge: no param name.");
            String key = it.toString(raw, true);
            it.prepare();
            if (HttpParser.nextItem(raw, it) < 0) error("Invalid challenge: no param value.");
            it.ioff = it.start;
            int offset = HttpParser.skipSpaces(raw, it);
            if ((offset < raw.length) && (raw[offset] == '\"')) {
                HttpParser.unquote(raw, it);
                if (params == null) {
                    params = new ArrayDictionary(5, 5);
                }
                params.put(key, it.toString(raw));
            } else {
                if (unqparams == null) {
                    unqparams = new ArrayDictionary(5, 5);
                }
                unqparams.put(key, it.toString(raw));
            }
            ps.prepare();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(554);

    }

    protected void updateByteValue() {
        HttpBuffer buf = new HttpBuffer();
        buf.append(scheme);
        buf.append(' ');
        int len = 0;
        if (params != null) {
            len = params.size();
        }
        if (unqparams != null) {
            len += unqparams.size();
        }
        if (params != null) {
            int plen = params.size();
            edu.hkust.clap.monitor.Monitor.loopBegin(555);
for (int i = 0; i < plen; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(555);
{
                String key = (String) params.keyAt(i);
                if (key == null) continue;
                buf.appendQuoted(key, (byte) '=', (String) params.elementAt(i));
                len--;
                if (len > 0) {
                    buf.append((byte) ',');
                    buf.append((byte) ' ');
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(555);

        }
        if (unqparams != null) {
            int unqlen = unqparams.size();
            edu.hkust.clap.monitor.Monitor.loopBegin(556);
for (int i = 0; i < unqlen; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(556);
{
                String key = (String) unqparams.keyAt(i);
                if (key == null) continue;
                buf.append(key, (byte) '=', (String) unqparams.elementAt(i));
                len--;
                if (len > 0) {
                    buf.append((byte) ',');
                    buf.append((byte) ' ');
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(556);

        }
        raw = buf.getByteCopy();
        roff = 0;
        rlen = raw.length;
    }

    public Object getValue() {
        validate();
        return this;
    }

    /**
     * Get the challenge scheme.
     * @return A String encoding the challenge scheme identifier.
     */
    public String getScheme() {
        validate();
        return scheme;
    }

    /**
     * Get an authentication parameter.
     * @param name The name of the parameter.
     * @return A String encoded value for this parameter, or <strong>null
     * </strong>
     */
    public String getAuthParameter(String name) {
        validate();
        String res = null;
        if (params != null) {
            res = (String) params.get(name);
        }
        if (res == null) {
            if (unqparams != null) {
                res = (String) unqparams.get(name);
            }
        }
        return res;
    }

    /**
     * Set an auth parameter value.
     * @param name The name of the parameter to set.
     * @param value The new value for this parameter.
     * @param quoted If true, the value will be quoted
     */
    public void setAuthParameter(String name, String value, boolean quoted) {
        invalidateByteValue();
        if (quoted) {
            if (params == null) {
                params = new ArrayDictionary(4, 4);
            }
            params.put(name, value);
        } else {
            if (unqparams == null) {
                unqparams = new ArrayDictionary(4, 4);
            }
            unqparams.put(name, value);
        }
    }

    /**
     * Set an auth parameter value.
     * @param name The name of the parameter to set.
     * @param value The new value for this parameter.
     * The value will be quoted
     */
    public void setAuthParameter(String name, String value) {
        setAuthParameter(name, value, true);
    }

    HttpChallenge(boolean isValid) {
        this.isValid = isValid;
    }

    HttpChallenge(boolean isValid, String scheme) {
        this.isValid = isValid;
        this.scheme = scheme;
    }

    public HttpChallenge() {
        super();
    }
}
