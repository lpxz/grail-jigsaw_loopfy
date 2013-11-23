package org.w3c.www.http;

import java.util.Vector;

public class HttpAcceptLanguageList extends BasicValue {

    HttpAcceptLanguage languages[] = null;

    protected void parse() {
        Vector vl = new Vector(4);
        ParseState ps = new ParseState(roff, rlen);
        ps.separator = (byte) ',';
        ps.spaceIsSep = false;
        edu.hkust.clap.monitor.Monitor.loopBegin(830);
while (HttpParser.nextItem(raw, ps) >= 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(830);
{
            vl.addElement(new HttpAcceptLanguage(this, raw, ps.start, ps.end));
            ps.prepare();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(830);

        languages = new HttpAcceptLanguage[vl.size()];
        vl.copyInto(languages);
    }

    protected void updateByteValue() {
        HttpBuffer buf = new HttpBuffer();
        if (languages == null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(831);
for (int i = 0; i < languages.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(831);
{
                if (i > 0) buf.append(',');
                languages[i].appendValue(buf);
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(831);

            raw = buf.getByteCopy();
            roff = 0;
            rlen = raw.length;
        } else {
            raw = new byte[0];
            roff = 0;
            rlen = 0;
        }
    }

    public Object getValue() {
        validate();
        return languages;
    }

    /**
     * Add a clause to that list of accepted languages.
     * @param lang The accepted language.
     */
    public void addLanguage(HttpAcceptLanguage lang) {
        if (languages == null) {
            languages = new HttpAcceptLanguage[1];
            languages[0] = lang;
        } else {
            int len = languages.length;
            HttpAcceptLanguage newlang[] = new HttpAcceptLanguage[len + 1];
            System.arraycopy(languages, 0, newlang, 0, len);
            newlang[len] = lang;
            languages = newlang;
        }
    }

    HttpAcceptLanguageList() {
        this.isValid = false;
    }

    HttpAcceptLanguageList(HttpAcceptLanguage languages[]) {
        this.languages = languages;
        this.isValid = true;
    }
}
