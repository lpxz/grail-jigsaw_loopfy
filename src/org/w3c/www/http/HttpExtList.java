package org.w3c.www.http;

import java.util.Vector;
import java.util.Enumeration;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 * Parse a comma separated list of Http extension headers.
 */
public class HttpExtList extends BasicValue {

    public static final int MAN = 0;

    public static final int CMAN = 1;

    public static final int OPT = 2;

    public static final int COPT = 3;

    Vector httpexts = null;

    int manopt = MAN;

    public int getManOptFlag() {
        return manopt;
    }

    protected void setManOptFlag(int manopt) {
        this.manopt = manopt;
    }

    /**
     * Parse this header value into its various components.
     * @exception HttpParserException if unable to parse.
     */
    protected void parse() throws HttpParserException {
        ParseState edl = new ParseState(roff, rlen);
        ParseState av = new ParseState(0, 0);
        ParseState it = new ParseState(0, 0);
        edl.separator = (byte) ',';
        edl.spaceIsSep = false;
        av.separator = (byte) ';';
        av.spaceIsSep = false;
        it.separator = (byte) '=';
        it.spaceIsSep = false;
        edu.hkust.clap.monitor.Monitor.loopBegin(532);
while (HttpParser.nextItem(raw, edl) >= 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(532);
{
            av.ioff = edl.start;
            av.bufend = edl.end;
            HttpExt ext = new HttpExt();
            edu.hkust.clap.monitor.Monitor.loopBegin(533);
while (HttpParser.nextItem(raw, av) >= 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(533);
{
                it.ioff = av.start;
                it.bufend = av.end;
                boolean unquoted = HttpParser.unquote(raw, it);
                if (HttpParser.nextItem(raw, it) < 0) error("Invalid extension item [" + av.toString(raw) + "]");
                String itemNaturalCase = it.toString(raw);
                String item = it.toString(raw, true);
                if (unquoted) {
                    ext.setName(itemNaturalCase);
                } else {
                    it.prepare();
                    HttpParser.unquote(raw, it);
                    if (HttpParser.nextItem(raw, it) < 0) error("No value for attribute [" + item + "]");
                    if (item.equals("ns")) {
                        ext.setNamespace(it.toString(raw));
                    } else {
                        ext.addDeclExt(itemNaturalCase, it.toString(raw));
                    }
                }
                av.prepare();
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(533);

            edl.prepare();
            httpexts.addElement(ext);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(532);

    }

    protected void updateByteValue() {
        HttpBuffer buf = new HttpBuffer();
        int len = httpexts.size();
        edu.hkust.clap.monitor.Monitor.loopBegin(534);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(534);
{
            HttpExt ext = (HttpExt) httpexts.elementAt(i);
            if (i != 0) buf.append(", ");
            buf.appendQuoted(ext.getName());
            if (ext.needsHeaders()) {
                buf.append(";");
                buf.append("ns", (byte) '=', ext.getNamespace());
            }
            Enumeration e = ext.getDeclExtNames();
            edu.hkust.clap.monitor.Monitor.loopBegin(535);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(535);
{
                String name = (String) e.nextElement();
                buf.append("; ");
                buf.append(name, (byte) '=', ext.getDeclExt(name));
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(535);

        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(534);

        raw = buf.getByteCopy();
        roff = 0;
        rlen = raw.length;
    }

    /**
     * Add an Http extension header.
     * @param ext an HttpExt.
     */
    public void addHttpExt(HttpExt ext) {
        validate();
        httpexts.addElement(ext);
    }

    /**
     * Get all Http extensions header.
     * @return an HttpExt array.
     */
    public HttpExt[] getHttpExts() {
        validate();
        HttpExt exts[] = new HttpExt[httpexts.size()];
        httpexts.copyInto(exts);
        return exts;
    }

    public int getLength() {
        validate();
        return httpexts.size();
    }

    /**
     * Get an Http extension header.
     * @param name The extension identifier (AbsoluteURI or field name)
     * @return an HttpExt or <strong>null</strong>.
     */
    public HttpExt getHttpExt(String name) {
        validate();
        edu.hkust.clap.monitor.Monitor.loopBegin(536);
for (int i = 0; i < httpexts.size(); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(536);
{
            HttpExt ext = (HttpExt) httpexts.elementAt(i);
            if (ext.getName().equals(name)) return ext;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(536);

        return null;
    }

    public Object getValue() {
        return this;
    }

    /**
     * for user.
     * @param exts the HttpExt array.
     */
    public HttpExtList(HttpExt exts[]) {
        this.isValid = true;
        int len = exts.length;
        this.httpexts = new Vector(len);
        if (exts != null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(537);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(537);
httpexts.addElement(exts[i]);} 
edu.hkust.clap.monitor.Monitor.loopEnd(537);

        }
    }

    /**
     * Constructor, for User
     * @param old the old Http extension declaration list 
     * If you want to reply the same extensions, use this
     * contructor.
     */
    public HttpExtList(HttpExtList old) {
        this.isValid = true;
        old.validate();
        int len = old.httpexts.size();
        this.httpexts = new Vector(len);
        edu.hkust.clap.monitor.Monitor.loopBegin(538);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(538);
{
            HttpExt newext = new HttpExt((HttpExt) old.httpexts.elementAt(i));
            this.httpexts.addElement(newext);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(538);

        this.manopt = old.manopt;
    }

    /**
     * for parser only
     */
    protected HttpExtList() {
        this.isValid = false;
        this.httpexts = new Vector(2);
    }
}
