package org.w3c.jigsaw.ssi.commands;

import java.util.Dictionary;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpCookie;
import org.w3c.www.http.HttpCookieList;
import org.w3c.www.http.HttpMessage;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.ssi.SSIFrame;
import org.w3c.util.ArrayDictionary;

/**
 * Cookies access from server side includes.
 * Powerfull thingy !
 */
public class CookieCommand implements Command {

    private static final String NAME = "cookie";

    private static final boolean debug = true;

    private static final String[] keys = { "get", "if", "alt", "then" };

    public String getName() {
        return NAME;
    }

    public Reply execute(SSIFrame ssiframe, Request request, ArrayDictionary parameters, Dictionary variables) {
        Object values[] = parameters.getMany(keys);
        String pget = (String) values[0];
        String pif = (String) values[1];
        String palt = (String) values[2];
        String pthen = (String) values[3];
        if (debug) System.out.println("cookie: get=" + pget + ", if=" + pif + ", alt=" + palt + ", then=" + pthen);
        String content = null;
        if (pget != null) {
            HttpCookieList list = request.getCookie();
            HttpCookie cookie = null;
            if (list != null) cookie = list.getCookie(pget);
            content = (cookie == null) ? palt : cookie.getValue();
        } else if (pif != null) {
            HttpCookieList list = request.getCookie();
            HttpCookie cookie = null;
            if (list != null) cookie = list.getCookie(pif);
            content = (cookie != null) ? pthen : palt;
        }
        Reply reply = ssiframe.createCommandReply(request, HTTP.OK);
        reply.setContent(content);
        return reply;
    }

    public String getValue(Dictionary variables, String variable, Request request) {
        return "null";
    }

    /**
     * return true if reply can be cached.
     * @return a boolean.
     */
    public boolean acceptCaching() {
        return true;
    }
}
