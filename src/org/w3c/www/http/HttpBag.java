package org.w3c.www.http;

import java.util.Enumeration;
import org.w3c.util.ArrayDictionary;

/**
 * Internal representation of protocol headers conforming to the bag spec.
 * The <strong>bag</strong> specification is part of the Protocol Extension
 * Protocol defined by w3c, it can be found
 * <a href="http://www.w3.org/hypertext/WWW/TR/WD-http-pep.html>here</a>.
 */
public class HttpBag extends BasicValue {

    boolean isToplevel = false;

    String name = null;

    ArrayDictionary items = null;

    protected final void updateByteValue() {
        HttpBuffer buf = new HttpBuffer();
        if (isToplevel) {
            Enumeration e = items.elements();
            boolean s = true;
            edu.hkust.clap.monitor.Monitor.loopBegin(1144);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(1144);
{
                HttpBag bag = (HttpBag) e.nextElement();
                if (s) s = false; else buf.append((byte) ',');
                bag.appendValue(buf);
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1144);

        } else {
            buf.append((byte) '{');
            buf.append(name);
            buf.append((byte) ' ');
            Enumeration e = items.keys();
            boolean s = true;
            edu.hkust.clap.monitor.Monitor.loopBegin(1145);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(1145);
{
                if (s) s = false; else buf.append((byte) ' ');
                String name = (String) e.nextElement();
                Object value = items.get(name);
                if (value instanceof Boolean) {
                    buf.append(name);
                } else if (value instanceof HttpBag) {
                    ((HttpBag) value).appendValue(buf);
                } else {
                    String msg = "Invalid bag item value, key=\"" + name + "\".";
                    throw new HttpInvalidValueException(msg);
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1145);

            buf.append((byte) '}');
        }
        raw = buf.getByteCopy();
        roff = 0;
        rlen = raw.length;
    }

    /**
     * parse bag.
     * @exception HttpParserException if parsing failed.
     */
    protected HttpBag parseBag(ParseState ps) throws HttpParserException {
        StringBuffer sb = new StringBuffer();
        int i = ps.ioff;
        byte b = raw[i];
        edu.hkust.clap.monitor.Monitor.loopBegin(1146);
while ((i < ps.bufend) && ((b = raw[i]) <= 32)) { 
edu.hkust.clap.monitor.Monitor.loopInc(1146);
i++;} 
edu.hkust.clap.monitor.Monitor.loopEnd(1146);

        if (i >= ps.bufend) return null;
        if (b != '{') error("Invalid Bag format (no {).");
        edu.hkust.clap.monitor.Monitor.loopBegin(1147);
for (++i; (i < ps.bufend) && ((b = raw[i]) <= 32); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1147);
;} 
edu.hkust.clap.monitor.Monitor.loopEnd(1147);

        if (i >= ps.bufend) error("Invalid Bag format (no name).");
        edu.hkust.clap.monitor.Monitor.loopBegin(1148);
while ((i < ps.bufend) && ((b = raw[i]) > 32) && (b != '}')) { 
edu.hkust.clap.monitor.Monitor.loopInc(1148);
{
            sb.append((char) b);
            i++;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1148);

        HttpBag bag = new HttpBag(true, sb.toString());
        edu.hkust.clap.monitor.Monitor.loopBegin(1149);
while (i < ps.bufend) { 
edu.hkust.clap.monitor.Monitor.loopInc(1149);
{
            b = raw[i];
            if (b <= 32) {
                i++;
                continue;
            } else if (b == '}') {
                ps.ooff = i + 1;
                return bag;
            } else if (b == '{') {
                ParseState inc = new ParseState(i, ps.bufend);
                HttpBag subbag = parseBag(inc);
                bag.items.put(subbag.name, subbag);
                i = inc.ooff;
            } else if (b == '\"') {
                sb.setLength(0);
                i++;
                edu.hkust.clap.monitor.Monitor.loopBegin(1150);
while ((i < ps.bufend) && ((b = raw[i]) != '\"')) { 
edu.hkust.clap.monitor.Monitor.loopInc(1150);
{
                    sb.append((char) b);
                    i++;
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1150);

                bag.items.put(sb.toString(), Boolean.TRUE);
                i++;
            } else {
                sb.setLength(0);
                edu.hkust.clap.monitor.Monitor.loopBegin(1148);
while ((i < ps.bufend) && ((b = raw[i]) > 32) && (b != '}')) { 
edu.hkust.clap.monitor.Monitor.loopInc(1148);
{
                    sb.append((char) b);
                    i++;
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1148);

                bag.addItem(sb.toString());
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1149);

        return bag;
    }

    /**
     * parse.
     * @exception HttpParserException if parsing failed.
     */
    protected final void parse() throws HttpParserException {
        int i = roff;
        isToplevel = true;
        HttpBag top = this;
        edu.hkust.clap.monitor.Monitor.loopBegin(1151);
while (i < rlen) { 
edu.hkust.clap.monitor.Monitor.loopInc(1151);
{
            switch(raw[i]) {
                case (byte) '{':
                    ParseState ps = new ParseState(i, rlen);
                    HttpBag bag = parseBag(ps);
                    top.items.put(bag.name, bag);
                    i = ps.ooff;
                    break;
                case (byte) ' ':
                case (byte) '\t':
                case (byte) ',':
                    i++;
                    break;
                default:
                    error("Unexpected separator \"" + raw[i] + "\".");
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1151);

    }

    public Object getValue() {
        return this;
    }

    /**
     * Get this bag names
     * @return The name of this bag.
     */
    public String getName() {
        validate();
        return name;
    }

    /**
     * Add a named bag into this bag.
     * @param bag The bag to add (in case item is a bag).
     */
    public void addBag(HttpBag subbag) {
        validate();
        items.put(subbag.getName(), subbag);
    }

    /**
     * Does this bag have a named sub-bag of the given name ?
     * @param name The name of the sub-bag to be tested for.
     * @return <strong>true</strong> if this sub-bag exists.
     */
    public boolean hasBag(String name) {
        validate();
        Object item = items.get(name);
        if ((item != null) && (item instanceof HttpBag)) return true;
        return false;
    }

    /**
     * Get a named sub-bag from this bag.
     * @param name The name of the sub-bag to get.
     * @return A bag instance, or <strong>null</strong> if none was found.
     */
    public HttpBag getBag(String name) {
        validate();
        if (hasBag(name)) return (HttpBag) items.get(name);
        return null;
    }

    /**
     * Add an item into this bag.
     * @param name The new item name.
     */
    public void addItem(String name) {
        validate();
        items.put(name, Boolean.TRUE);
    }

    /**
     * Add a sub-bag to this bag.
     * @param subbag The sub-bag to add.
     */
    public void addItem(HttpBag subbag) {
        validate();
        items.put(subbag.getName(), subbag);
    }

    /**
     * Does this bag contains the given item, being a bag or a simple word.
     * @param name The name of the item to test.
     * @return <strong>true</strong> if the bag contains the given item, 
     *   <strong>false</strong> otherwise.
     */
    public boolean hasItem(String name) {
        validate();
        return items.get(name) != null;
    }

    /**
     * Remove an item from that bag.
     * @param name The name of the item to remove.
     */
    public void removeItem(String name) {
        validate();
        items.remove(name);
    }

    /**
     * Get all named items from this bag. 
     * This include both named sub-bags and items by their own.
     */
    public Enumeration keys() {
        validate();
        return items.keys();
    }

    /**
     * Is that a top level bag or not ?
     * @return A boolean, <strong>true</strong> if the bag was a toplevel
     * bag.
     */
    public boolean isToplevelBag() {
        return isToplevel;
    }

    HttpBag() {
        isValid = false;
        this.items = new ArrayDictionary(5, 5);
    }

    HttpBag(boolean isValid, String name) {
        this.isValid = isValid;
        this.name = name;
        this.items = new ArrayDictionary(5, 5);
    }
}
