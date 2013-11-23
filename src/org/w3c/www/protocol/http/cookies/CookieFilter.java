package org.w3c.www.protocol.http.cookies;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.net.URL;
import org.w3c.util.ObservableProperties;
import org.w3c.www.protocol.http.HttpException;
import org.w3c.www.protocol.http.HttpManager;
import org.w3c.www.protocol.http.PropRequestFilter;
import org.w3c.www.protocol.http.PropRequestFilterException;
import org.w3c.www.protocol.http.Reply;
import org.w3c.www.protocol.http.Request;
import org.w3c.www.http.HttpCookie;
import org.w3c.www.http.HttpCookieList;
import org.w3c.www.http.HttpFactory;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpRequestMessage;
import org.w3c.www.http.HttpSetCookie;
import org.w3c.www.http.HttpSetCookieList;

class DomainNode {

    Hashtable nodes = null;

    HttpSetCookie cookies[] = null;

    int nbcookies = 0;

    protected boolean sameCookies(HttpSetCookie c1, HttpSetCookie c2) {
        if (c2.getPath() == null && c1.getPath() == null) return (c1.getName().equals(c2.getName())); else if (c2.getPath() == null || c1.getPath() == null) return false; else return ((c1.getName().equals(c2.getName())) && (c1.getPath().equals(c2.getPath())));
    }

    protected void addCookie(HttpSetCookie cookie) {
        int i = 0;
        edu.hkust.clap.monitor.Monitor.loopBegin(223);
while (i < nbcookies) { 
edu.hkust.clap.monitor.Monitor.loopInc(223);
{
            if (sameCookies(cookie, cookies[i])) {
                cookies[i] = cookie;
                return;
            }
            i++;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(223);

        if (nbcookies < cookies.length) {
            cookies[nbcookies++] = cookie;
        } else {
            HttpSetCookie ncookies[] = new HttpSetCookie[cookies.length + 1];
            System.arraycopy(cookies, 0, ncookies, 0, cookies.length);
            ncookies[nbcookies++] = cookie;
            cookies = ncookies;
        }
    }

    protected void sync(FileWriter writer) throws IOException {
        if (nbcookies > 0) {
            edu.hkust.clap.monitor.Monitor.loopBegin(224);
for (int i = 0; i < nbcookies; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(224);
{
                if (cookies[i].getMaxAge() > 0) writer.write(DomainTree.cookie2String(cookies[i]));
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(224);

        }
        Enumeration e = nodes.elements();
        DomainNode node = null;
        edu.hkust.clap.monitor.Monitor.loopBegin(225);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(225);
{
            node = (DomainNode) e.nextElement();
            node.sync(writer);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(225);

    }

    DomainNode() {
        nodes = new Hashtable(2);
        cookies = new HttpSetCookie[1];
        nbcookies = 0;
    }
}

class DomainTree {

    Hashtable nodes = null;

    protected static String cookie2String(HttpSetCookie cookie) {
        Date date = new Date();
        return (cookie.getDomain() + "\t" + String.valueOf(cookie.getSecurity()).toUpperCase() + "\t" + cookie.getPath() + "\t" + (cookie.getMaxAge() * 1000 + date.getTime()) + "\t" + cookie.getVersion() + "\t" + cookie.getName() + "\t" + cookie.getValue() + "\n");
    }

    protected HttpSetCookie string2Cookie(String cookie[]) {
        HttpSetCookie cook = new HttpSetCookie();
        cook.setDomain(cookie[0]);
        cook.setSecurity(Boolean.getBoolean(cookie[1].toLowerCase()));
        cook.setPath(cookie[2]);
        long expire = Long.parseLong(cookie[3]);
        long now = (new Date()).getTime();
        cook.setMaxAge((int) ((expire - now) / 1000));
        cook.setVersion(Integer.parseInt(cookie[4]));
        cook.setName(cookie[5]);
        cook.setValue(cookie[6]);
        return cook;
    }

    protected synchronized void loadCookies(File file) throws FileNotFoundException {
        try {
            FileReader reader = new FileReader(file);
            String cookie[] = new String[8];
            int i = 0;
            int ch;
            StringBuffer buffer = new StringBuffer(30);
            edu.hkust.clap.monitor.Monitor.loopBegin(226);
while ((ch = reader.read()) != -1) { 
edu.hkust.clap.monitor.Monitor.loopInc(226);
{
                switch(ch) {
                    case '#':
                        edu.hkust.clap.monitor.Monitor.loopBegin(227);
while ((ch = reader.read()) != '\n') { 
edu.hkust.clap.monitor.Monitor.loopInc(227);
if (ch == -1) return;} 
edu.hkust.clap.monitor.Monitor.loopEnd(227);

                        break;
                    case '\n':
                        if (i > 0) {
                            cookie[i++] = buffer.toString();
                            HttpSetCookie setcookie = string2Cookie(cookie);
                            if (setcookie.getMaxAge() > 0) insertCookie(setcookie);
                            buffer = new StringBuffer(30);
                            cookie = new String[8];
                            i = 0;
                        }
                        break;
                    case '\t':
                    case ' ':
                        cookie[i++] = buffer.toString();
                        buffer = new StringBuffer(30);
                        break;
                    default:
                        buffer.append((char) ch);
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(226);

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    protected synchronized void sync(File file) {
        Enumeration e = nodes.elements();
        DomainNode node = null;
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write("# Jigsaw client HTTP Cookie File\n");
            writer.write("# This is a generated file!  Do not edit.\n\n");
            edu.hkust.clap.monitor.Monitor.loopBegin(225);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(225);
{
                node = (DomainNode) e.nextElement();
                node.sync(writer);
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(225);

            writer.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (Exception ex2) {
            }
        }
    }

    protected boolean isIp(String domain) {
        int last = domain.length() - 1;
        return ((domain.charAt(last) >= '0') && (domain.charAt(last) <= '9'));
    }

    protected String[] domainParts(String dom) {
        if (dom == null) return null;
        String domain = new String(dom);
        Vector V = new Vector(5);
        int i = 0;
        int j = 0;
        int max = domain.length();
        edu.hkust.clap.monitor.Monitor.loopBegin(228);
while (i < max) { 
edu.hkust.clap.monitor.Monitor.loopInc(228);
{
            j = domain.indexOf('.', i);
            if (j == -1) j = max;
            V.addElement(domain.substring(i, j));
            i = j + 1;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(228);

        ;
        if (V.size() == 0) return null;
        String parts[] = new String[V.size()];
        V.copyInto(parts);
        return parts;
    }

    protected HttpCookie setCookie2Cookie(HttpSetCookie setcookie) {
        HttpCookie cookie = new HttpCookie();
        if (setcookie != null) {
            cookie.setName(setcookie.getName());
            cookie.setValue(setcookie.getValue());
            cookie.setDomain(setcookie.getDomain());
            cookie.setPath(setcookie.getPath());
            cookie.setVersion(setcookie.getVersion());
            return cookie;
        }
        return null;
    }

    protected void addMatchingPathCookiesInVector(HttpSetCookie cookieArray[], String path, Vector V) {
        int i = 0;
        edu.hkust.clap.monitor.Monitor.loopBegin(229);
while (i < cookieArray.length) { 
edu.hkust.clap.monitor.Monitor.loopInc(229);
{
            if (path.equals("/")) V.addElement(setCookie2Cookie(cookieArray[i])); else if (cookieArray[i].getPath() != null) {
                if (path.startsWith(cookieArray[i].getPath())) {
                    V.addElement(setCookie2Cookie(cookieArray[i]));
                }
            }
            i++;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(229);

    }

    public HttpCookieList getCookies(URL url) {
        String domain = url.getHost();
        String path = url.getFile();
        String parts[] = domainParts(domain);
        if (parts == null) return null;
        Vector V = new Vector(5);
        int i = 0;
        DomainNode node = null;
        Hashtable childs = nodes;
        if (isIp(domain)) {
            node = (DomainNode) childs.get(parts[i]);
            edu.hkust.clap.monitor.Monitor.loopBegin(230);
while (i < parts.length) { 
edu.hkust.clap.monitor.Monitor.loopInc(230);
{
                if (node == null) return null;
                if (node.nbcookies > 0) {
                    addMatchingPathCookiesInVector(node.cookies, path, V);
                }
                if ((i + 1) < parts.length) node = (DomainNode) childs.get(parts[++i]); else node = null;
                if (node == null) break;
                childs = node.nodes;
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(230);

        } else {
            i = parts.length - 1;
            node = (DomainNode) childs.get(parts[i]);
            edu.hkust.clap.monitor.Monitor.loopBegin(231);
while (i >= 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(231);
{
                if (node == null) return null;
                if (node.nbcookies > 0) {
                    addMatchingPathCookiesInVector(node.cookies, path, V);
                }
                if (i > 0) node = (DomainNode) childs.get(parts[--i]); else node = null;
                if (node == null) break;
                childs = node.nodes;
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(231);

        }
        if (V.size() == 0) return null;
        HttpCookie cookieArray[] = new HttpCookie[V.size()];
        V.copyInto(cookieArray);
        return HttpFactory.makeCookieList(cookieArray);
    }

    public void insertCookie(HttpSetCookie cookie) {
        String domain = cookie.getDomain();
        String parts[] = domainParts(domain);
        if (parts == null) return;
        int i = 0;
        DomainNode node = null;
        Hashtable childs = nodes;
        if (isIp(domain)) {
            node = (DomainNode) childs.get(parts[i]);
while (true) { 
{
                if (node == null) {
                    node = new DomainNode();
                    childs.put(parts[i], node);
                }
                if (i == parts.length - 1) {
                    node.addCookie(cookie);
                    return;
                }
                node = (DomainNode) childs.get(parts[++i]);
                if (node == null) {
                    node = new DomainNode();
                    childs.put(parts[i], node);
                }
                childs = node.nodes;
            }} 

        } else {
            i = parts.length - 1;
            node = (DomainNode) childs.get(parts[i]);
while (true) { 
{
                if (node == null) {
                    node = new DomainNode();
                    childs.put(parts[i], node);
                }
                if (i == 0) {
                    node.addCookie(cookie);
                    return;
                }
                node = (DomainNode) childs.get(parts[--i]);
                if (node == null) {
                    node = new DomainNode();
                    childs.put(parts[i], node);
                }
                childs = node.nodes;
            }} 

        }
    }

    DomainTree() {
        this.nodes = new Hashtable(10);
    }
}

/**
 * Client side CookieFilter :
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */
public class CookieFilter implements PropRequestFilter {

    /**
     * The absolute Path of the file use to store cookies.
     */
    public static final String COOKIES_FILE_P = "org.w3c.www.protocol.http.cookie.file";

    private static final String defaultFileName = "cookie";

    private static DomainTree root = null;

    static {
        root = new DomainTree();
    }

    protected HttpManager manager = null;

    private static File cookiefile = null;

    /**
     * The request pre-processing hook.
     * Before each request is launched, all filters will be called back 
     * through
     * this method. They will generally set up additional request header
     * fields to enhance the request.
     * @param request The request that is about to be launched.
     * @return An instance of Reply if the filter could handle the request,
     * or <strong>null</strong> if processing should continue normally.
     * @exception HttpException If the filter is supposed to fulfill the
     * request, but some error happened during that processing.
     */
    public Reply ingoingFilter(Request request) throws HttpException {
        HttpCookieList cookielist = root.getCookies(request.getURL());
        if (cookielist != null) request.setCookie(cookielist);
        return null;
    }

    /**
     * The request post-processing hook.
     * After each request has been replied to by the target server (be it a 
     * proxy or the actual origin server), each filter's outgoingFilter
     * method is called.
     * <p>It gets the original request, and the actual reply as a parameter,
     * and should return whatever reply it wants the caller to get.
     * @param request The original (handled) request.
     * @param reply The reply, as emited by the target server, or constructed
     * by some other filter.
     * @exception HttpException If the reply emitted by the server is not
     * a valid HTTP reply.
     */
    public Reply outgoingFilter(Request request, Reply reply) throws HttpException {
        HttpSetCookieList list = reply.getSetCookie();
        if (list != null) {
            HttpSetCookie[] cooks = list.getSetCookies();
            if (cooks != null) {
                int i = 0;
                edu.hkust.clap.monitor.Monitor.loopBegin(234);
while (i < cooks.length) { 
edu.hkust.clap.monitor.Monitor.loopInc(234);
{
                    if (cooks[i].getDomain() == null) cooks[i].setDomain(request.getURL().getHost());
                    root.insertCookie(cooks[i++]);
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(234);

            }
        }
        return reply;
    }

    /**
     * An exception occured while talking to target server.
     * This method is triggered by the HttpManager, when the target server
     * (which can be a proxy for that request) was not reachable, or some
     * network error occured while emitting the request or reading the reply
     * headers.
     * @param request The request whose processing triggered the exception.
     * @param ex The exception that was triggered.
     * @return A boolean, <strong>true</strong> if that filter did influence
     * the target server used to fulfill the request, and it has fixed the 
     * problem in such a way that the request should be retried.
     */
    public boolean exceptionFilter(Request request, HttpException ex) {
        return false;
    }

    /** 
     * Synchronized any pending state into stable storage.
     * If the filter maintains some in-memory cached state, this method
     * should ensure that cached data are saved to stable storage.
     */
    public void sync() {
        root.sync(cookiefile);
    }

    /**
     * Initialize this filter, using the provided manager.
     * During initialization, it is up to the filter to install itself
     * in the manager, by invoking the appropriate <code>setFilter</code>
     * method.
     * @param manager The HttpManager initializing the filter.
     * @exception PropRequestFilterException If the filter couldn't be 
     * initialized properly.
     */
    public void initialize(HttpManager manager) throws PropRequestFilterException {
        this.manager = manager;
        ObservableProperties props = manager.getProperties();
        String filepath = (String) props.getString(COOKIES_FILE_P, null);
        if (filepath == null) {
            cookiefile = new File(defaultFileName);
        } else {
            cookiefile = new File(filepath);
        }
        if (cookiefile.exists()) {
            try {
                root.loadCookies(cookiefile);
            } catch (FileNotFoundException ex) {
                System.out.println(ex.getMessage());
                throw new PropRequestFilterException(ex.getMessage());
            }
        }
        manager.setFilter(this);
    }
}
