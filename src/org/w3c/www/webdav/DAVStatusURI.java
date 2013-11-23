package org.w3c.www.webdav;

import org.w3c.www.http.BasicValue;
import org.w3c.www.http.HttpInvalidValueException;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class DAVStatusURI {

    int status = -1;

    String url = null;

    public int getStatus() {
        return status;
    }

    public String getURI() {
        return url;
    }

    DAVStatusURI(byte raw[], int start, int end) throws HttpInvalidValueException {
        ParseState ps = new ParseState(start, end);
        ps.separator = (byte) '<';
        ps.spaceIsSep = false;
        ParseState ps2 = new ParseState(0, 0);
        ps2.separator = (byte) '>';
        ps2.spaceIsSep = false;
        if (DAVParser.nextItem(raw, ps) >= 0) {
            this.status = Integer.parseInt(ps.toString(raw).trim());
        }
        if (DAVParser.nextItem(raw, ps) >= 0) {
            ps2.prepare(ps);
            if (DAVParser.nextItem(raw, ps2) >= 0) {
                this.url = ps2.toString(raw).trim();
            }
        }
        if ((status == -1) || (url == null)) {
            throw new HttpInvalidValueException("Invalid Status-URI");
        }
    }

    public DAVStatusURI(int status, String url) {
        this.status = status;
        this.url = url;
    }
}
