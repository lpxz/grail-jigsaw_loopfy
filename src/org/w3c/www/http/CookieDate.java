package org.w3c.www.http;

import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class CookieDate extends HttpDate {

    protected static String days[] = { "Pad", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };

    protected static String months[] = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

    protected void updateByteValue() {
        if (cal == null) {
            TimeZone tz = TimeZone.getTimeZone("UTC");
            cal = Calendar.getInstance(tz);
        }
        Date now = new Date(date.longValue());
        cal.setTime(now);
        HttpBuffer buf = new HttpBuffer(32);
        buf.append(days[cal.get(Calendar.DAY_OF_WEEK)]);
        buf.append(',');
        buf.append(' ');
        buf.appendInt(cal.get(Calendar.DAY_OF_MONTH), 2, (byte) '0');
        buf.append('-');
        buf.append(months[cal.get(Calendar.MONTH)]);
        buf.append('-');
        buf.appendInt(cal.get(Calendar.YEAR), 2, (byte) '0');
        buf.append(' ');
        buf.appendInt(cal.get(Calendar.HOUR_OF_DAY), 2, (byte) '0');
        buf.append(':');
        buf.appendInt(cal.get(Calendar.MINUTE), 2, (byte) '0');
        buf.append(':');
        buf.appendInt(cal.get(Calendar.SECOND), 2, (byte) '0');
        buf.append(" GMT");
        raw = buf.getByteCopy();
        roff = 0;
        rlen = raw.length;
    }

    public CookieDate(boolean isValid, long date) {
        super(isValid, date);
    }
}
