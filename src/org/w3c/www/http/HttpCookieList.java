package org.w3c.www.http;

import java.util.Vector;

public class HttpCookieList extends BasicValue {

    Vector cookies = null;

    protected void updateByteValue() {
        HttpBuffer buf = new HttpBuffer();
        int sz = cookies.size();
        edu.hkust.clap.monitor.Monitor.loopBegin(1180);
for (int i = 0; i < sz; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1180);
{
            HttpCookie c = (HttpCookie) cookies.elementAt(i);
            if (i != 0) {
                buf.append((byte) ';');
                buf.append((byte) ' ');
            }
            buf.append(c.getName(), (byte) '=', c.getValue());
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1180);

        raw = buf.getByteCopy();
        roff = 0;
        rlen = raw.length;
    }

    protected void deprecatedUpdateByteValue() {
        HttpBuffer buf = new HttpBuffer();
        int sz = cookies.size();
        edu.hkust.clap.monitor.Monitor.loopBegin(1181);
for (int i = 0; i < sz; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1181);
{
            HttpCookie c = (HttpCookie) cookies.elementAt(i);
            if (i == 0) {
                buf.append("$Version", (byte) '=', c.getVersion());
                buf.append((byte) ';');
            } else {
                buf.append((byte) ',');
                buf.append((byte) ' ');
            }
            buf.append(c.getName(), (byte) '=', c.getValue());
            String s = c.getPath();
            if (s != null) {
                buf.append((byte) ';');
                buf.append("$Path", (byte) '=', s);
            }
            if ((s = c.getDomain()) != null) {
                buf.append((byte) ';');
                buf.append("$Domain", (byte) '=', s);
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1181);

        raw = buf.getByteCopy();
        roff = 0;
        rlen = raw.length;
    }

    /**
     * parse the Cookie Header according to the Netscape Specification:
     * http://www.netscape.com/newsref/std/cookie_spec.html
     * @exception HttpParserException if parsing failed.
     */
    protected void parse() throws HttpParserException {
        ParseState cv = new ParseState(roff, rlen);
        ParseState it = new ParseState(0, 0);
        cv.separator = (byte) ';';
        cv.spaceIsSep = false;
        it.separator = (byte) '=';
        edu.hkust.clap.monitor.Monitor.loopBegin(1182);
while (HttpParser.nextItem(raw, cv) >= 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(1182);
{
            it.ioff = cv.start;
            it.bufend = cv.end;
            if (HttpParser.nextItem(raw, it) < 0) error("Invalid item in cookie value.");
            String item = it.toString(raw);
            if (item.charAt(0) == '$') continue;
            HttpCookie c = new HttpCookie();
            it.prepare();
            if (HttpParser.nextItem(raw, it) < 0) {
                c.setValue("");
            } else {
                it.end = cv.end;
                c.setValue(it.toString(raw));
            }
            c.setName(item);
            cookies.addElement(c);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1182);

    }

    /**
     * parse.
     * @exception HttpParserException if parsing failed.
     */
    protected void deprecatedParse() throws HttpParserException {
        ParseState cv = new ParseState(roff, rlen);
        ParseState it = new ParseState(0, 0);
        ParseState val = new ParseState(0, 0);
        cv.separator = (byte) ';';
        cv.spaceIsSep = false;
        it.separator = (byte) '=';
        val.separator = (byte) ';';
        val.spaceIsSep = false;
        HttpCookie c = new HttpCookie();
        edu.hkust.clap.monitor.Monitor.loopBegin(1183);
while (HttpParser.nextItem(raw, cv) >= 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(1183);
{
            it.ioff = cv.start;
            it.bufend = cv.end;
            if (HttpParser.nextItem(raw, it) < 0) error("Invalid item in cookie value.");
            String item = it.toString(raw, true);
            it.prepare();
            if (HttpParser.nextItem(raw, it) < 0) error("Cookie item [" + item + "] has no value.");
            if (item.equals("$path")) {
                c.setPath(it.toString(raw));
            } else if (item.equals("$domain")) {
                c.setDomain(it.toString(raw));
            } else if (item.equals("$version")) {
                c.setVersion(Integer.parseInt(it.toString(raw)));
            } else {
                if (c.getName() != null) error("Invalid cookie item [" + item + "]");
                c.setName(item);
                val.ioff = it.start;
                val.bufend = cv.end;
                HttpParser.nextItem(raw, val);
                c.setValue(it.toString(raw));
            }
            cv.prepare();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1183);

        cookies.addElement(c);
    }

    /**
     * Get this HTTP value, parsed value.
     */
    public Object getValue() {
        return this;
    }

    /**
     * Add a cookie to this header value.
     * @param name The name of the cookie to add.
     * @param value It's value.
     * @return A HttpCookie instance, tha represents this cookie in the header
     * value.
     */
    public HttpCookie addCookie(String name, String value) {
        validate();
        HttpCookie c = new HttpCookie(true, name, value);
        cookies.addElement(c);
        return c;
    }

    /**
     * Remove a cookie from this header value.
     * @param name The name of the cookie to remove.
     * @return A boolean, <strong>true</strong> If the cookie was found, and
     * removed, <strong>false</strong> otherwise.
     */
    public boolean removeCookie(String name) {
        validate();
        int sz = cookies.size();
        edu.hkust.clap.monitor.Monitor.loopBegin(1184);
for (int i = 0; i < sz; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1184);
{
            HttpCookie c = (HttpCookie) cookies.elementAt(i);
            if (c.getName().equals(name)) {
                cookies.removeElementAt(i);
                return true;
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1184);

        return false;
    }

    /**
     * Lookup a cookie by name.
     * @param name The name of the cooie to lookup.
     * @return A HttpCookie instance, or <strong>null</strong> if not found.
     */
    public HttpCookie getCookie(String name) {
        validate();
        int sz = cookies.size();
        edu.hkust.clap.monitor.Monitor.loopBegin(1185);
for (int i = 0; i < sz; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1185);
{
            HttpCookie c = (HttpCookie) cookies.elementAt(i);
            if (c.getName().equalsIgnoreCase(name)) return c;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1185);

        return null;
    }

    public HttpCookie[] getCookies() {
        validate();
        HttpCookie cooks[] = new HttpCookie[cookies.size()];
        cookies.copyInto(cooks);
        return cooks;
    }

    HttpCookieList(HttpCookie c[]) {
        this.isValid = true;
        this.cookies = new Vector(8);
        if (c != null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(1186);
for (int i = 0; i < c.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1186);
cookies.addElement(c[i]);} 
edu.hkust.clap.monitor.Monitor.loopEnd(1186);

        }
    }

    public HttpCookieList() {
        this.isValid = false;
        this.cookies = new Vector(2);
    }
}
