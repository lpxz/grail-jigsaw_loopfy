package org.w3c.www.http;

public class HttpInteger extends BasicValue {

    Integer value = null;

    protected void parse() {
        ParseState ps = new ParseState();
        ps.ioff = 0;
        ps.bufend = raw.length;
        value = new Integer(HttpParser.parseInt(raw, ps));
    }

    protected void updateByteValue() {
        HttpBuffer buf = new HttpBuffer(11);
        if (value.intValue() == Integer.MAX_VALUE) {
            buf.append("2147483648");
        }
        buf.appendInt(value.intValue());
        raw = buf.getByteCopy();
        roff = 0;
        rlen = raw.length;
    }

    public Object getValue() {
        validate();
        return value;
    }

    public void setValue(int ival) {
        if (value.intValue() == ival) return;
        invalidateByteValue();
        value = new Integer(ival);
        isValid = true;
    }

    public void setValue(Integer ival) {
        if (ival.intValue() == value.intValue()) return;
        invalidateByteValue();
        value = ival;
        isValid = true;
    }

    HttpInteger(boolean isValid, int ival) {
        this.isValid = isValid;
        this.value = new Integer(ival);
    }

    public HttpInteger() {
        this.isValid = false;
    }
}
