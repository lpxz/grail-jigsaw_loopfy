package org.w3c.jigsaw.frames;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.FileResource;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidParentException;
import org.w3c.tools.resources.LookupResult;
import org.w3c.tools.resources.LookupState;
import org.w3c.tools.resources.PropertiesAttribute;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.ServerInterface;
import org.w3c.tools.resources.StringArrayAttribute;
import org.w3c.tools.resources.StringAttribute;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.www.mime.MimeHeaderHolder;
import org.w3c.www.mime.MimeParser;
import org.w3c.www.mime.MimeParserException;
import org.w3c.www.mime.MimeParserFactory;
import org.w3c.www.mime.MimeType;
import org.w3c.jigsaw.http.Client;
import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.httpd;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HeaderDescription;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpEntityTag;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;
import org.w3c.jigsaw.auth.AuthFilter;
import org.w3c.util.ArrayDictionary;

/**
 * Parsing the CGI output - The CGIHeaderHolder, to hold CGI headers.
 */
class CGIHeaderHolder implements MimeHeaderHolder {

    String status = null;

    String location = null;

    Hashtable headers = null;

    MimeParser parser = null;

    /**
     * The parsing is now about to start, take any appropriate action.
     * This hook can return a <strong>true</strong> boolean value to enforce
     * the MIME parser into transparent mode (eg the parser will <em>not</em>
     * try to parse any headers.
     * <p>This hack is primarily defined for HTTP/0.9 support, it might
     * also be usefull for other hacks.
     * @param parser The Mime parser.
     * @return A boolean <strong>true</strong> if the MimeParser shouldn't
     * continue the parsing, <strong>false</strong> otherwise.
     * @exception IOException if an IO error occurs.
     */
    public boolean notifyBeginParsing(MimeParser parser) throws IOException {
        return false;
    }

    /**
     * All the headers have been parsed, take any appropriate actions.
     * @param parser The Mime parser.
     * @exception IOException if an IO error occurs.
     */
    public void notifyEndParsing(MimeParser parser) throws IOException {
        return;
    }

    /**
     * A new header has been emited by the script.
     * If the script is not an NPH, then it <strong>must</strong> at least
     * emit one header, so we are safe here, although people may not be safe 
     * against the spec.
     * @param name The header name.
     * @param buf The header bytes.
     * @param off The begining of the value bytes  in above buffer.
     * @param len The length of the value bytes  in above buffer.
     * @exception MimeParserException if parsing failed.
     */
    public void notifyHeader(String name, byte buf[], int off, int len) throws MimeParserException {
        if (name.equalsIgnoreCase("status")) {
            status = new String(buf, 0, off, len);
        } else if (name.equalsIgnoreCase("location")) {
            location = new String(buf, 0, off, len);
        } else {
            String extraval = new String(buf, 0, off, len);
            if (headers == null) {
                headers = new Hashtable(11);
            } else {
                String val = (String) headers.get(name.toLowerCase());
                if (val != null) extraval = val + "," + extraval;
            }
            headers.put(name.toLowerCase(), extraval);
        }
    }

    /**
     * Get the status emited by the script.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Get the location header value emited by the script.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Get any header value (except status and location).
     * @param name The name of the header to fetch.
     * @return The string value of requested header, or <strong>null</strong>
     * if header was not defined.
     */
    public String getValue(String name) {
        return (headers == null) ? null : (String) headers.get(name);
    }

    /**
     * Enumerate the headers defined by the holder.
     * @return A enumeration of header names, or <strong>null</strong> if no
     * header is defined.
     */
    public Enumeration enumerateHeaders() {
        if (headers == null) return null;
        return headers.keys();
    }

    /**
     * Get the remaining output of the stream.
     * This should be called only once header parsing is done.
     */
    public InputStream getInputStream() {
        return parser.getInputStream();
    }

    CGIHeaderHolder(MimeParser parser) {
        this.parser = parser;
    }
}

/**
 * Parsing the CGI output - Always create a CGIHeaderHolder.
 */
class CGIHeaderHolderFactory implements MimeParserFactory {

    /**
     * Create a new header holder to hold the parser's result.
     * @param parser The parser that has something to parse.
     * @return A MimeParserHandler compliant object.
     */
    public MimeHeaderHolder createHeaderHolder(MimeParser parser) {
        return new CGIHeaderHolder(parser);
    }

    CGIHeaderHolderFactory() {
    }
}

/**
 * A simple process feeder class.
 */
class ProcessFeeder extends Thread {

    Process proc = null;

    OutputStream out = null;

    InputStream in = null;

    int count = -1;

    public void run() {
        try {
            byte buffer[] = new byte[4096];
            int got = -1;
            if (count >= 0) {
                edu.hkust.clap.monitor.Monitor.loopBegin(490);
while ((count > 0) && ((got = in.read(buffer)) > 0)) { 
edu.hkust.clap.monitor.Monitor.loopInc(490);
{
                    out.write(buffer, 0, got);
                    count -= got;
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(490);

            } else {
                edu.hkust.clap.monitor.Monitor.loopBegin(491);
while ((got = in.read(buffer)) > 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(491);
{
                    out.write(buffer, 0, got);
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(491);

            }
        } catch (Exception e) {
            System.out.println("ProcessFeeder: caught exception !");
            e.printStackTrace();
        } finally {
            try {
                out.flush();
            } catch (IOException ex) {
            }
            try {
                out.close();
            } catch (IOException ex) {
            }
            try {
                proc.waitFor();
            } catch (Exception ex) {
            }
        }
    }

    ProcessFeeder(Process proc, InputStream in) {
        this(proc, in, -1);
    }

    ProcessFeeder(Process proc, InputStream in, int count) {
        this.proc = proc;
        this.out = proc.getOutputStream();
        this.in = in;
        this.count = count;
    }
}

/**
 * Handle CGI error stream
 */
class ProcessErrorReader extends Thread {

    BufferedReader r = null;

    public void run() {
        String errline = null;
        try {
            errline = r.readLine();
            if (errline != null) {
                System.err.println("*** CgiError: " + errline);
            }
        } catch (Exception ex) {
        } finally {
            try {
                r.close();
            } catch (IOException ioex) {
            }
        }
    }

    ProcessErrorReader(Process proc) {
        r = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
    }
}

/**
 * Handle CGI scripts.
 */
public class CgiFrame extends HTTPFrame {

    private static final String STATE_EXTRA_PATH = "org.w3c.jigsaw.frames.CgiFrame.extraPath";

    /**
     * Attribute index - The interpreter to use, if any.
     */
    protected static int ATTR_INTERPRETER = -1;

    /**
     * Attribute index - The array of string that makes the command to run.
     */
    protected static int ATTR_COMMAND = -1;

    /**
     * Attribute index - Does the script takes care of its headers ?
     */
    protected static int ATTR_NOHEADER = -1;

    /**
     * Attribute index - Does the script generates the form on GET ?
     */
    protected static int ATTR_GENERATES_FORM = -1;

    /**
     * Attribute index - Do DNS, to fill in REMOTE_HOST env var.
     */
    protected static int ATTR_REMOTE_HOST = -1;

    /**
     * Attribute index - Turn the script in debug mode.
     */
    protected static int ATTR_CGI_DEBUG = -1;

    /**
     * Attribute index - Additional environment vars
     */
    protected static int ATTR_ENV = -1;

    static {
        Attribute a = null;
        Class cls = null;
        try {
            cls = Class.forName("org.w3c.jigsaw.frames.CgiFrame");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        a = new StringAttribute("interpreter", null, Attribute.EDITABLE);
        ATTR_INTERPRETER = AttributeRegistry.registerAttribute(cls, a);
        a = new StringArrayAttribute("command", null, Attribute.MANDATORY | Attribute.EDITABLE);
        ATTR_COMMAND = AttributeRegistry.registerAttribute(cls, a);
        a = new BooleanAttribute("noheader", Boolean.FALSE, Attribute.EDITABLE);
        ATTR_NOHEADER = AttributeRegistry.registerAttribute(cls, a);
        a = new BooleanAttribute("generates-form", Boolean.TRUE, Attribute.EDITABLE);
        ATTR_GENERATES_FORM = AttributeRegistry.registerAttribute(cls, a);
        a = new BooleanAttribute("remote-host", null, Attribute.EDITABLE);
        ATTR_REMOTE_HOST = AttributeRegistry.registerAttribute(cls, a);
        a = new BooleanAttribute("cgi-debug", Boolean.FALSE, Attribute.EDITABLE);
        ATTR_CGI_DEBUG = AttributeRegistry.registerAttribute(cls, a);
        a = new PropertiesAttribute("environment", null, Attribute.EDITABLE);
        ATTR_ENV = AttributeRegistry.registerAttribute(cls, a);
    }

    /**
     * Get the interpreter to use to execute the script.
     * This is most usefull for operating systems that don't have a
     * <code>!#</code> convention ala UNIX.
     * @return The interpreter to run the script.
     */
    public String getInterpreter() {
        return getString(ATTR_INTERPRETER, null);
    }

    /**
     * Get the command string array.
     */
    public String[] getCommand() {
        return (String[]) getValue(ATTR_COMMAND, null);
    }

    public ArrayDictionary getUserEnv() {
        return (ArrayDictionary) getValue(ATTR_ENV, null);
    }

    /**
     * Get the noheader flag.
     * @return The boolean value of the noheader flag.
     */
    public boolean checkNoheaderFlag() {
        return getBoolean(ATTR_NOHEADER, false);
    }

    /**
     * Get the generates form flag.
     * @return The boolean value of the generates form flag.
     */
    public boolean checkGeneratesFormFlag() {
        return getBoolean(ATTR_GENERATES_FORM, true);
    }

    /**
     * Get the remote host attribute value.
     * If turned on, this flag will enable the REMOTE_HOST env var computation.
     * @return A boolean.
     */
    public boolean checkRemoteHost() {
        return getBoolean(ATTR_REMOTE_HOST, false);
    }

    /**
     * Get the CGI debug flag.
     * @return The boolean value of the CGI debug flag.
     */
    public boolean checkCgiDebug() {
        return getBoolean(ATTR_CGI_DEBUG, false);
    }

    /**
     * Turn the given header name into it's env var canonical name.
     * This guy is crazy enough to run CGI scripts, he can pay for that 
     * overhead.
     * @param name The header name.
     * @return A String giving the official env variable name for that header.
     */
    public String getEnvName(String name) {
        int sl = name.length();
        StringBuffer sb = new StringBuffer(5 + sl);
        sb.append("HTTP_");
        edu.hkust.clap.monitor.Monitor.loopBegin(492);
for (int i = 0; i < sl; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(492);
{
            char ch = name.charAt(i);
            sb.append((ch == '-') ? '_' : Character.toUpperCase(ch));
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(492);

        return sb.toString();
    }

    /**
     * Get this resource Etag
     * @return an instance of HttpEntityTag, or <strong>null</strong> if not
     *    defined.
     */
    public HttpEntityTag getETag() {
        if (etag == null) {
            HttpEntityTag netag = super.getETag();
            if (netag != null) {
                etag.setWeak(true);
            }
        }
        return etag;
    }

    /**
     * Handle the CGI script output.
     * This methods handles the CGI script output. Depending on the
     * value of the <strong>noheader</strong> attribute it either:
     * <ul>
     * <li>Sends back the script output directly,</li>
     * <li>Parses the script output, looking for a status header or a 
     * location header, or a content-length header, or any combination
     * of those three.
     * </ul>
     * @param process The underlying CGI process.
     * @param request The processed request.
     * @exception ProtocolException If an HTTP error 
     * should be sent back to the client.
     */
    protected Reply handleCGIOutput(Process process, Request request) throws ProtocolException {
        (new ProcessErrorReader(process)).start();
        if (checkNoheaderFlag()) {
            Reply reply = request.makeReply(HTTP.NOHEADER);
            reply.setStream(process.getInputStream());
            return reply;
        }
        if (checkCgiDebug()) {
            Reply reply = request.makeReply(HTTP.OK);
            reply.setContentType(MimeType.TEXT_PLAIN);
            reply.setStream(process.getInputStream());
            return reply;
        }
        MimeParser p = new MimeParser(process.getInputStream(), new CGIHeaderHolderFactory());
        Reply reply = null;
        try {
            CGIHeaderHolder h = (CGIHeaderHolder) p.parse();
            String svalue = h.getStatus();
            String location = h.getLocation();
            if (svalue != null) {
                int status = -1;
                try {
                    String _st = svalue.trim();
                    int _space = _st.indexOf(' ');
                    if (_space != -1) {
                        _st = _st.substring(0, _space);
                    }
                    status = Integer.parseInt(_st);
                } catch (Exception ex) {
                    String msg = ("Emited an invalid status line [" + svalue + "].");
                    getServer().errlog(this, msg);
                    reply = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
                    reply.setContent("CGI script emited invalid status.");
                    throw new HTTPException(reply);
                }
                reply = createDefaultReply(request, status);
                reply.setContentLength(-1);
            } else {
                if (location != null) {
                    reply = request.makeReply(HTTP.FOUND);
                } else {
                    reply = createDefaultReply(request, HTTP.OK);
                    reply.setContentLength(-1);
                }
            }
            if (location != null) {
                try {
                    reply.setLocation(new URL(getURL(request), location));
                } catch (MalformedURLException ex) {
                    getServer().errlog(this, "unable to create location url " + location + " in base " + getURL(request));
                }
            }
            Enumeration e = h.enumerateHeaders();
            if (e != null) {
                edu.hkust.clap.monitor.Monitor.loopBegin(493);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(493);
{
                    String hname = (String) e.nextElement();
                    reply.setValue(hname, (String) h.getValue(hname));
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(493);

            }
            reply.setStream(p.getInputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (MimeParserException ex) {
            String msg = (getURL(request) + ": emited invalid output [" + ex.getMessage() + "]");
            getServer().errlog(this, msg);
            Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
            error.setContent("CGI error: unable to parse script headers.");
            throw new HTTPException(error);
        }
        if (reply != null) {
            reply.setDynamic(true);
        }
        return reply;
    }

    /**
     * Add an enviornment binding to the given vector.
     * @param name The name of the enviornment variable.
     * @param val Its value.
     * @param into The vector to which accumulate bindings.
     */
    private void addEnv(String name, String val, Vector into) {
        into.addElement(name + "=" + val);
    }

    /**
     * Prepare the command to run for this CGI script, and run it.
     * @param request The request to handle.
     * @return The running CGI process object.
     * @exception ProtocolException If we weren't able 
     * to build the command or the environment.
     * @exception IOException if an IO erro occurs.
     */
    protected Process makeCgiCommand(Request request) throws ProtocolException, IOException {
        String query = null;
        String command[] = getCommand();
        if (command == null) {
            Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
            error.setContent("CgiResource mis-configured: it doesn't have a " + " command attribute");
            throw new HTTPException(error);
        }
        Vector env = new Vector(32);
        httpd server = request.getClient().getServer();
        InetAddress sadr = server.getInetAddress();
        String svalue = (String) request.getState(AuthFilter.STATE_AUTHTYPE);
        if (svalue != null) addEnv("AUTH_TYPE", svalue, env);
        svalue = request.getValue("content-length");
        if (svalue != null) addEnv("CONTENT_LENGTH", svalue, env);
        svalue = request.getValue("content-type");
        if (svalue != null) addEnv("CONTENT_TYPE", svalue, env);
        addEnv("GATEWAY_INTERFACE", "CGI/1.1", env);
        svalue = (String) request.getState(STATE_EXTRA_PATH);
        if (svalue == null) addEnv("PATH_INFO", "/", env); else addEnv("PATH_INFO", svalue, env);
        query = request.getQueryString();
        if (query != null) addEnv("QUERY_STRING", query, env);
        svalue = request.getClient().getInetAddress().toString();
        addEnv("REMOTE_ADDR", svalue, env);
        svalue = (String) request.getState(AuthFilter.STATE_AUTHUSER);
        if (svalue != null) addEnv("REMOTE_USER", svalue, env);
        if (checkRemoteHost()) {
            String host = request.getClient().getInetAddress().getHostName();
            addEnv("REMOTE_HOST", host, env);
        }
        addEnv("REQUEST_METHOD", request.getMethod(), env);
        addEnv("SCRIPT_NAME", getURLPath(), env);
        addEnv("SERVER_NAME", getServer().getHost(), env);
        svalue = Integer.toString(getServer().getLocalPort());
        addEnv("SERVER_PORT", svalue, env);
        addEnv("SERVER_PROTOCOL", request.getVersion(), env);
        addEnv("SERVER_SOFTWARE", server.getSoftware(), env);
        if (getFileResource() != null) {
            addEnv("PATH_TRANSLATED", getFileResource().getFile().getAbsolutePath(), env);
        }
        ArrayDictionary uenv = getUserEnv();
        if (uenv != null) {
            Enumeration names = uenv.keys();
            edu.hkust.clap.monitor.Monitor.loopBegin(494);
while (names.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(494);
{
                String var = (String) names.nextElement();
                addEnv(var, (String) uenv.get(var), env);
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(494);

        }
        Enumeration e = request.enumerateHeaderDescriptions(false);
        edu.hkust.clap.monitor.Monitor.loopBegin(495);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(495);
{
            HeaderDescription d = (HeaderDescription) e.nextElement();
            addEnv(getEnvName(d.getName()), request.getHeaderValue(d).toString(), env);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(495);

        if (query != null) {
            String querycmd[] = new String[command.length + 1];
            System.arraycopy(command, 0, querycmd, 0, command.length);
            querycmd[command.length] = query;
            command = querycmd;
        }
        String aenv[] = new String[env.size()];
        env.copyInto(aenv);
        if (getInterpreter() != null) {
            String run[] = new String[command.length + 1];
            run[0] = getInterpreter();
            System.arraycopy(command, 0, run, 1, command.length);
            return Runtime.getRuntime().exec(run, aenv);
        } else {
            return Runtime.getRuntime().exec(command, aenv);
        }
    }

    /**
     * Lookup sub-resources.
     * Accumulate the remaning path in some special state of the request.
     * <p>This allows us to implement the <code>PATH_INFO</code> 
     * CGI variable properly.
     * @param ls Current lookup state.
     * @param lr Lookup result under construction.
     * @return A boolean <strong>true</strong> if lookup should continue,
     * <strong>false</strong> otherwise.
     * @exception ProtocolException (fixme doc)
     */
    public boolean lookup(LookupState ls, LookupResult lr) throws ProtocolException {
        String extraPath = ls.getRemainingPath(true);
        if ((extraPath == null) || extraPath.equals("")) extraPath = "/";
        Request request = (Request) ls.getRequest();
        if (request != null) request.setState(STATE_EXTRA_PATH, extraPath);
        lr.setTarget(getResource().getResourceReference());
        return super.lookup(ls, lr);
    }

    /** 
     * GET method implementation.
     * this method is splitted into two cases:
     * <p>If the resource is able to generates its form, than run the script
     * to emit the form. Otherwsie, use our super class (FileResource) ability
     * to send the file that contains the form.
     * <p>Note that there is no need to feed the underlying process with
     * data in the GET case.
     * @param request The request to handle.
     * @exception ProtocolException If processing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public Reply get(Request request) throws ProtocolException, ResourceException {
        if (!checkGeneratesFormFlag()) return super.get(request);
        Process process = null;
        try {
            process = makeCgiCommand(request);
        } catch (IOException e) {
            e.printStackTrace();
            Reply error = request.makeReply(HTTP.NOT_FOUND);
            error.setContent("The resource's script wasn't found.");
            throw new HTTPException(error);
        }
        return handleCGIOutput(process, request);
    }

    /**
     * Handle the POST method according to CGI/1.1 specification.
     * The request body is sent back to the launched CGI script, as is, and
     * the script output is handled by the handleCGIOutput method.
     * @param request The request to process.
     * @exception ProtocolException If the processing failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public Reply post(Request request) throws ProtocolException, ResourceException {
        Process process = null;
        try {
            process = makeCgiCommand(request);
        } catch (IOException ex) {
            String msg = ("The process " + getCommand()[0] + " couldn't be executed [" + ex.getMessage() + "]");
            getServer().errlog(this, msg);
            Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
            error.setContent("CGI script is misconfigured.");
            throw new HTTPException(error);
        }
        try {
            Client client = request.getClient();
            if (client != null) client.sendContinue();
            InputStream in = request.getInputStream();
            if (in == null) {
                process.getOutputStream().close();
            } else {
                (new ProcessFeeder(process, in)).start();
            }
        } catch (IOException ex) {
            Reply error = request.makeReply(HTTP.BAD_REQUEST);
            error.setContent("The request didn't have a valid input.");
            throw new HTTPException(error);
        }
        return handleCGIOutput(process, request);
    }

    /**
     * At register time, if no command, use a suitable default.
     * THis method will set the command to the identifier, if it is not
     * provided.
     * @param values Default attribute values.
     */
    public void registerResource(FramedResource resource) {
        super.registerResource(resource);
        if (getCommand() == null) {
            if (getFileResource() != null) {
                try {
                    if (getFileResource().getFile() != null) {
                        String cmd[] = new String[1];
                        cmd[0] = getFileResource().getFile().getAbsolutePath();
                        setValue(ATTR_COMMAND, cmd);
                    }
                } catch (InvalidParentException ex) {
                }
            }
        }
    }
}
