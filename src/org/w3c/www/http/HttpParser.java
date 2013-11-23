package org.w3c.www.http;

import java.util.Date;

/**
 * A private class to help with the parsing.
 * Contains only some static method, helping to parse various byte
 * buffers into Java object (Yes, I am still and again trying to reduce 
 * memory consumption).
 * <p>I don't know wether this sucks or not. One hand I am sparing a tremedous
 * amount of Strings creation, on the other end I am recoding a number of
 * parsers that are available on String instances.
 */
public class HttpParser {

    private static final boolean debug = false;

    /**
     * Emit an error.
     * @param mth The method trigerring the error.
     * @param msg An associated message.
     * @exception HttpInvalidValueException To indicate the error to caller.
     */
    protected static void error(String mth, String msg) throws HttpInvalidValueException {
        throw new HttpInvalidValueException(mth + ": " + msg);
    }

    /**
     * Compare two byte arrays.
     * I am not comfident about how the equality of byte arrays is performed
     * by other means, sorry. 
     * @param b1 The first byte array.
     * @param o1 The offset of the bytes to compare.
     * @param l1 The number of bytes to compare.
     * @param b2 What to compare against.
     * @param o2 The offset of the bytes to compare.
     * @param l2 The length of the bytes to compare.
     * @return An integer, <strong><0</strong> if b1 is less than b2,
     * <strong>0</strong> if equals, <strong>>0</strong>otherwise.
     */
    public static final int compare(byte b1[], int o1, int l1, byte b2[], int o2, int l2) {
        edu.hkust.clap.monitor.Monitor.loopBegin(936);
while ((o1 < l1) && (o2 < l2)) { 
edu.hkust.clap.monitor.Monitor.loopInc(936);
{
            int cmp = (((int) b1[o1]) & 0xff) - (((int) b2[o2]) & 0xff);
            if (cmp != 0) return cmp;
            o1++;
            o2++;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(936);

        return ((o1 == l1) && (o2 == l2)) ? 0 : l2 - l1;
    }

    /**
     * Compare two byte arrays.
     * Short-cut version of the above version.
     * @param b1 The first byte array.
     * @param o1 The offset of the bytes to compare.
     * @param l1 The number of bytes to compare.
     * @param b2 What to compare against.
     * @return An integer, <strong><0</strong> if b1 is less than b2,
     * <strong>0</strong> if equals, <strong>>0</strong>otherwise.
     */
    public static final int compare(byte b1[], int o1, int l1, byte b2[]) {
        return compare(b1, o1, l1, b2, 0, b2.length);
    }

    /**
     * Parse an integer, and return an updated pointer.
     */
    public static final int parseInt(byte buf[], int radix, ParseState ps) {
        int off = -1;
        if (ps.isSkipable) ps.start = off = skipSpaces(buf, ps); else ps.start = off = ps.ioff;
        int len = (ps.bufend > 0) ? ps.bufend : buf.length;
        int ret = 0;
        int oldret = 0;
        boolean neg = false;
        if (buf[off] == (byte) '-') {
            neg = true;
            off++;
        }
        edu.hkust.clap.monitor.Monitor.loopBegin(937);
while (off < len) { 
edu.hkust.clap.monitor.Monitor.loopInc(937);
{
            int digit = ((int) buf[off]) & 0xff;
            if ((digit >= (byte) '0') && (digit <= (byte) '9')) {
                ret = ret * radix + (digit - (byte) '0');
            } else if (radix >= 10) {
                if ((digit >= 'A') && (digit <= 'Z')) {
                    if ((digit - 'A') + 10 < radix) ret = ret * radix + (digit - 'A' + 10); else break;
                } else if ((digit >= 'a') && (digit <= 'z')) {
                    if ((digit - 'a') + 10 < radix) ret = ret * radix + digit - 'a' + 10; else break;
                } else {
                    break;
                }
            } else {
                break;
            }
            if (ret < oldret) {
                error("parseInt", "Integer overflow: " + new String(buf, 0, ps.start, len));
            } else {
                oldret = ret;
            }
            off++;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(937);

        if (ret < oldret) {
            error("parseInt", "Integer overflow: " + new String(buf, 0, ps.start, len));
        }
        ps.ooff = off;
        ps.end = off;
        if (ps.ooff == ps.ioff) error("parseInt", "No number available.");
        return neg ? -ret : ret;
    }

    public static final int parseInt(byte buf[], ParseState ps) {
        return parseInt(buf, 10, ps);
    }

    /**
     * Parse an integer, and return an updated pointer.
     */
    public static final long parseLong(byte buf[], int radix, ParseState ps) {
        int off = -1;
        if (ps.isSkipable) ps.start = off = skipSpaces(buf, ps); else ps.start = off = ps.ioff;
        int len = (ps.bufend > 0) ? ps.bufend : buf.length;
        long ret = 0;
        long oldret = 0;
        boolean neg = false;
        if (buf[off] == (byte) '-') {
            neg = true;
            off++;
        }
        edu.hkust.clap.monitor.Monitor.loopBegin(938);
while (off < len) { 
edu.hkust.clap.monitor.Monitor.loopInc(938);
{
            int digit = ((int) buf[off]) & 0xff;
            if ((digit >= (byte) '0') && (digit <= (byte) '9')) {
                ret = ret * radix + (digit - (byte) '0');
            } else if (radix >= 10) {
                if ((digit >= 'A') && (digit <= 'Z')) {
                    if ((digit - 'A') + 10 < radix) ret = ret * radix + (digit - 'A' + 10); else break;
                } else if ((digit >= 'a') && (digit <= 'z')) {
                    if ((digit - 'a') + 10 < radix) ret = ret * radix + digit - 'a' + 10; else break;
                } else {
                    break;
                }
            } else {
                break;
            }
            if (ret < oldret) {
                error("parseLong", "Long overflow: " + new String(buf, 0, ps.start, len));
            } else {
                oldret = ret;
            }
            off++;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(938);

        if (ret < oldret) {
            error("parseLong", "Long overflow: " + new String(buf, 0, ps.start, len));
        }
        ps.ooff = off;
        ps.end = off;
        if (ps.ooff == ps.ioff) error("parseLong", "No number available.");
        return neg ? -ret : ret;
    }

    public static final long parseLong(byte buf[], ParseState ps) {
        return parseLong(buf, 10, ps);
    }

    public static boolean unquote(byte buf[], ParseState ps) {
        int off = -1;
        int len = -1;
        if (ps.isSkipable) off = skipSpaces(buf, ps); else off = ps.ioff;
        len = (ps.bufend > 0) ? ps.bufend : buf.length;
        if ((off < len) && (buf[off] == (byte) '"')) {
            ps.start = ps.ioff = ++off;
            edu.hkust.clap.monitor.Monitor.loopBegin(939);
while (off < len) { 
edu.hkust.clap.monitor.Monitor.loopInc(939);
{
                if (buf[off] == (byte) '"') {
                    ps.end = ps.bufend = off;
                    return true;
                } else {
                    off++;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(939);

        } else {
            ps.start = off;
            ps.end = len;
        }
        return false;
    }

    /**
     * Skip leading LWS, <em>not</em> including CR LF.
     * Update the input offset, <em>after</em> any leading space.
     * @param buf The buffer to be parsed.
     * @param ptr The buffer pointer to be updated on return.
     * @return The potentially advanced buffer input offset.
     */
    public static final int skipSpaces(byte buf[], ParseState ps) {
        int len = (ps.bufend > 0) ? ps.bufend : buf.length;
        int off = ps.ioff;
        edu.hkust.clap.monitor.Monitor.loopBegin(940);
while (off < len) { 
edu.hkust.clap.monitor.Monitor.loopInc(940);
{
            if ((buf[off] != (byte) ' ') && (buf[off] != (byte) '\t') && (buf[off] != (byte) ps.separator)) {
                ps.ioff = off;
                return off;
            }
            off++;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(940);

        return off;
    }

    /**
     * Parse list of items, taking care of quotes and optional LWS.
     * The output offset points to the <em>next</em> element of the list.
     * @eturn The starting location (i.e. <code>ps.start</code> value), or
     * <strong>-1</strong> if no item available (end of list).
     */
    public static final int nextItem(byte buf[], ParseState ps) {
        int off = -1;
        int len = -1;
        if (ps.isSkipable) ps.start = off = skipSpaces(buf, ps); else ps.start = off = ps.ioff;
        len = (ps.bufend > 0) ? ps.bufend : buf.length;
        if (debug) System.out.println("parsing: [" + new String(buf, 0, off, len - off) + "]");
        if (off >= len) return -1;
        ps.start = off;
        edu.hkust.clap.monitor.Monitor.loopBegin(941);
        loop:
while (off < len) { 
edu.hkust.clap.monitor.Monitor.loopInc(941);
{
            if (buf[off] == (byte) '"') {
                off++;
                edu.hkust.clap.monitor.Monitor.loopBegin(942);
while (off < len) { 
edu.hkust.clap.monitor.Monitor.loopInc(942);
{
                    if (buf[off] == (byte) '\\') {
                        off += 2;
                    } else if (buf[off] == (byte) '"') {
                        off++;
                        continue loop;
                    } else {
                        off++;
                    }
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(942);

                if (off == len) error("nextItem", "Un-terminated quoted item.");
            } else if ((buf[off] == ps.separator) || (ps.spaceIsSep && ((buf[off] == ' ') || (buf[off] == '\t')))) {
                break loop;
            }
            off++;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(941);

        ps.end = off;
        if (ps.isSkipable) {
            ps.ioff = off;
            ps.ooff = skipSpaces(buf, ps);
        }
        if (ps.ooff < ps.bufend) {
            if (buf[ps.ooff] == (byte) ps.separator) ps.ooff++;
        }
        if (debug) System.out.println("nextItem = [" + new String(buf, 0, ps.start, ps.end - ps.start) + "]");
        return (ps.end > ps.start) ? ps.start : -1;
    }

    /**
     * Parse the name of a month.
     * Monthes are parsed as their three letters format.
     * @return An integer between <strong>0</strong> and <strong>11</strong>.
     */
    private static byte monthes[][] = { { (byte) 'J', (byte) 'a', (byte) 'n' }, { (byte) 'F', (byte) 'e', (byte) 'b' }, { (byte) 'M', (byte) 'a', (byte) 'r' }, { (byte) 'A', (byte) 'p', (byte) 'r' }, { (byte) 'M', (byte) 'a', (byte) 'y' }, { (byte) 'J', (byte) 'u', (byte) 'n' }, { (byte) 'J', (byte) 'u', (byte) 'l' }, { (byte) 'A', (byte) 'u', (byte) 'g' }, { (byte) 'S', (byte) 'e', (byte) 'p' }, { (byte) 'O', (byte) 'c', (byte) 't' }, { (byte) 'N', (byte) 'o', (byte) 'v' }, { (byte) 'D', (byte) 'e', (byte) 'c' } };

    private static final byte lowerCase(int x) {
        if ((x >= 'A') && (x <= 'Z')) x = (x - 'A' + 'a');
        return (byte) (x & 0xff);
    }

    public static int parseMonth(byte buf[], ParseState ps) {
        int off = -1;
        if (ps.isSkipable) off = ps.start = skipSpaces(buf, ps); else off = ps.start = ps.ioff;
        int len = (ps.bufend > 0) ? ps.bufend : buf.length;
        if (len < 3) {
            error("parseMonth", "Invalid month name (too short).");
            return -1;
        }
        edu.hkust.clap.monitor.Monitor.loopBegin(943);
for (int i = 0; i < monthes.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(943);
{
            int mo = off;
            byte m[] = monthes[i];
            boolean ok = true;
            edu.hkust.clap.monitor.Monitor.loopBegin(944);
            month_loop: 
for (int j = 0; j < m.length; j++, mo++) { 
edu.hkust.clap.monitor.Monitor.loopInc(944);
{
                if (lowerCase(m[j]) != lowerCase(buf[mo])) {
                    ok = false;
                    break month_loop;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(944);

            if (ok) {
                if (mo - off == m.length) {
                    off += 3;
                    edu.hkust.clap.monitor.Monitor.loopBegin(945);
while (off < len) { 
edu.hkust.clap.monitor.Monitor.loopInc(945);
{
                        byte l = lowerCase(buf[off++]);
                        if ((l < 'a') || (l > 'z')) break;
                    }} 
edu.hkust.clap.monitor.Monitor.loopEnd(945);

                    ps.ooff = ps.end = off;
                }
                return i;
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(943);

        error("parseMonth", "Invalid month name (unknown).");
        return -1;
    }

    /**
     * Parse a delta-second value.
     * @return A long giving the date at which to retry as a number of
     * milliseconds since Java epoch.
     */
    public static long parseDeltaSecond(byte buf[], ParseState ps) {
        return parseInt(buf, ps);
    }

    /**
     * Parse the given byte array as an HTTP compliant date.
     * @param buf The byte buffer to parse.
     * @param sp The current parsing state.
     * @return A long giving the date as a number of milliseconds since epoch.
     */
    public static long parseDate(byte buf[], ParseState ps) {
        int d = -1;
        int m = -1;
        int y = -1;
        int hh = -1;
        int mm = -1;
        int ss = -1;
        ParseState it = new ParseState();
        it.ioff = ps.ioff;
        it.bufend = ((ps.bufend > -1) ? ps.bufend : buf.length);
        if (nextItem(buf, ps) < 0) error("parseDate", "Invalid date format (no day)");
        ps.prepare();
        int off = skipSpaces(buf, ps);
        if ((buf[off] >= (byte) '0') && (buf[off] <= (byte) '9')) {
            d = parseInt(buf, ps);
            ps.prepare();
            if (buf[ps.ioff] == (byte) ' ') {
                m = parseMonth(buf, ps);
                ps.prepare();
                if ((y = parseInt(buf, ps) - 1900) < 0) y += 1900;
                ps.prepare();
                ps.separator = (byte) ':';
                hh = parseInt(buf, ps);
                ps.prepare();
                mm = parseInt(buf, ps);
                ps.prepare();
                ss = parseInt(buf, ps);
            } else {
                ps.separator = (byte) '-';
                m = parseMonth(buf, ps);
                ps.prepare();
                y = parseInt(buf, ps);
                ps.prepare();
                ps.separator = (byte) ':';
                hh = parseInt(buf, ps);
                ps.prepare();
                mm = parseInt(buf, ps);
                ps.prepare();
                ss = parseInt(buf, ps);
            }
        } else {
            m = parseMonth(buf, ps);
            ps.prepare();
            d = parseInt(buf, ps);
            ps.prepare();
            ps.separator = (byte) ':';
            hh = parseInt(buf, ps);
            ps.prepare();
            mm = parseInt(buf, ps);
            ps.prepare();
            ss = parseInt(buf, ps);
            ps.prepare();
            ps.separator = (byte) ' ';
            y = parseInt(buf, ps) - 1900;
        }
        return Date.UTC(y, m, d, hh, mm, ss);
    }

    /**
     * Parse a date as either a delta-second value, or a date.
     * In case of delta seconds, we use the current time (except if one
     * is provided), to compute the date.
     * @return A date encoded as the number of millisconds since Java epoch.
     */
    public static long parseDateOrDeltaSeconds(byte buf[], ParseState ps, long relto) {
        int off = -1;
        if (ps.isSkipable) off = ps.start = skipSpaces(buf, ps); else off = ps.ioff;
        int len = (ps.bufend >= 0) ? ps.bufend : buf.length;
        edu.hkust.clap.monitor.Monitor.loopBegin(946);
for (int i = off; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(946);
{
            if ((buf[i] > '9') || (buf[i] < '0')) return parseDate(buf, ps);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(946);

        long secs = (long) parseInt(buf, ps);
        return ((relto >= 0) ? relto + (secs * 1000) : System.currentTimeMillis() + (secs * 1000));
    }

    public static long parseDateOrDeltaSeconds(byte buf[], ParseState ps) {
        return parseDateOrDeltaSeconds(buf, ps, (long) -1);
    }

    public static double parseQuality(byte buf[], ParseState ps) {
        int off = -1;
        if (ps.isSkipable) ps.start = off = skipSpaces(buf, ps); else ps.start = off = ps.ioff;
        int len = (ps.bufend > 0) ? ps.bufend : buf.length;
        String str = new String(buf, 0, off, len - off);
        try {
            return Double.valueOf(str).doubleValue();
        } catch (Exception ex) {
            error("parseQuality", "Invalid floating point number.");
        }
        return 1.0;
    }
}
