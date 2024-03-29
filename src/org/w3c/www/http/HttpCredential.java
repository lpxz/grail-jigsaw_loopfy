package org.w3c.www.http;

import org.w3c.util.ArrayDictionary;

/**
 * This class has a hack to handle basic authentication.
 * Basic  authentication (amongst others) is broken in the HTTP spec, to handle
 * the APIs more nicely, Jigsaw fakes a <code>cookie</code> auth param
 * with the appropriate basic-credentials.
 */
public class HttpCredential extends BasicValue {

    ArrayDictionary params = null;

    ArrayDictionary unqparams = null;

    String scheme = null;

    /**
     * parse.
     * @exception HttpParserException if parsing failed.
     */
    protected void parse() throws HttpParserException {
        ParseState ps = new ParseState(roff, rlen);
        if (HttpParser.nextItem(raw, ps) < 0) error("Invalid credentials: no scheme.");
        this.scheme = ps.toString(raw);
        ps.prepare();
        if (scheme.equalsIgnoreCase("basic")) {
            if (HttpParser.nextItem(raw, ps) < 0) error("Invalid basic auth credentials, no basic-cookie.");
            if (unqparams == null) {
                unqparams = new ArrayDictionary(5, 5);
            }
            unqparams.put("cookie", ps.toString(raw));
        } else {
            ParseState it = new ParseState();
            it.separator = (byte) '=';
            ps.separator = (byte) ',';
            edu.hkust.clap.monitor.Monitor.loopBegin(899);
while (HttpParser.nextItem(raw, ps) >= 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(899);
{
                it.prepare(ps);
                if (HttpParser.nextItem(raw, it) < 0) error("Invalid credentials: bad param name.");
                String key = it.toString(raw, true);
                it.prepare();
                if (HttpParser.nextItem(raw, it) < 0) error("Invalid credentials: no param value.");
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
edu.hkust.clap.monitor.Monitor.loopEnd(899);

        }
    }

    protected void updateByteValue() {
        HttpBuffer buf = new HttpBuffer();
        buf.append(scheme);
        buf.append(' ');
        int len;
        if (params != null) {
            len = params.size();
            edu.hkust.clap.monitor.Monitor.loopBegin(900);
for (int i = 0; len > 0; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(900);
{
                String key = (String) params.keyAt(i);
                if (key == null) {
                    continue;
                }
                if (key.equals("cookie")) {
                    buf.append((String) params.elementAt(i));
                } else {
                    buf.appendQuoted(key, (byte) '=', (String) params.elementAt(i));
                }
                len--;
                if (len > 0) {
                    buf.append((byte) ',');
                    buf.append((byte) ' ');
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(900);

        }
        if (unqparams != null) {
            len = unqparams.size();
            edu.hkust.clap.monitor.Monitor.loopBegin(901);
for (int i = 0; len > 0; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(901);
{
                String key = (String) unqparams.keyAt(i);
                if (key == null) {
                    continue;
                }
                if (key.equals("cookie")) {
                    buf.append((String) params.elementAt(i));
                } else {
                    buf.append(key, (byte) '=', (String) params.elementAt(i));
                }
                len--;
                if (len > 0) {
                    buf.append((byte) ',');
                    buf.append((byte) ' ');
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(901);

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
     * Get the authentication scheme identifier.
     * @return A String giving the auth scheme identifier.
     */
    public String getScheme() {
        validate();
        return scheme;
    }

    /**
     * Set the authentication scheme.
     * @param scheme The auth scheme for these credentials.
     */
    public void setScheme(String scheme) {
        if ((this.scheme != null) && !this.scheme.equalsIgnoreCase(scheme)) invalidateByteValue();
        this.scheme = scheme;
    }

    /**
     * Get an authentication parameter.
     * @param name The name of the parameter to fetch.
     * @return The String value, or <strong>null</strong> if undefined.
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

    public HttpCredential(boolean isValid, String scheme) {
        this.isValid = isValid;
        this.scheme = scheme;
    }

    public HttpCredential() {
        this.isValid = false;
    }
}
