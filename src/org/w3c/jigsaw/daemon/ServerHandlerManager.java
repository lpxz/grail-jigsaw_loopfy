package org.w3c.jigsaw.daemon;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import org.w3c.util.ObservableProperties;
import org.w3c.util.Unix;
import org.w3c.util.UnixException;
import org.w3c.jigsaw.http.httpd;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * A ServerHandlerManager instance manages a set of ServerHandler.
 */
public class ServerHandlerManager {

    private static final boolean debug = false;

    /**
     * The property containing the servers to be launched at startup time.
     * This property is a <code>|</code> separated list of server identifiers.
     * Declaring a server to this list requires that either:
     * <ul>
     * <li>An appropriate
     * <code>org.w3c.jigsaw.daemon.</code><em>identifier</em>.<code>class
     *</code>
     * is declared and specify the class of the server to be launched (this 
     * class should implement the ServerHandler interface.).
     * <li>An appropriate
     * <code>org.w3c.jigsaw.daemon.</code><em>identifier</em>.<code>clones
     *</code>
     * is declared and its value specify an existing server to be cloned in 
     * order to create the new server.
     * </ul>
     */
    protected static final String HANDLERS_P = "org.w3c.jigsaw.daemon.handlers";

    /**
     * The server handler property <code>class</code> suffix.
     */
    public static final String CLASS_P = "org.w3c.jigsaw.daemon.class";

    /**
     * The server handler property <code>clones</code> prefix.
     */
    public static final String CLONES_P = "org.w3c.jigsaw.daemon.clones";

    public static final String SERVER_USER_P = "org.w3c.jigsaw.daemon.user";

    public static final String SERVER_GROUP_P = "org.w3c.jigsaw.daemon.group";

    /**
     * The Application-Wide server manager.
     */
    protected static ServerHandlerManager manager = null;

    /**
     * The list of running server handlers.
     */
    protected Hashtable handlers = null;

    /**
     * The server handler manager property list.
     */
    protected DaemonProperties props = null;

    /**
     * Command line options that were provided at launch time.
     */
    protected String commandLine[] = null;

    /**
     * Emit a non-fatal error.
     * @param msg The message to emit.
     */
    protected void error(String msg) {
        System.err.println(msg);
    }

    /**
     * Emit a fatal error.
     * This will abort the whole process !
     * @param msg The fata error message.
     */
    protected void fatal(String msg) {
        System.out.println("*** FATAL Error:");
        System.out.println(msg);
        System.exit(1);
    }

    /**
     * Get the command line options that were provided at launch time.
     * @return A String array instance.
     */
    public String[] getCommandLine() {
        if (commandLine == null) commandLine = new String[0];
        return commandLine;
    }

    /**
     * For subclasses only. Used to update properties at runtime.
     * This method is called by 
     * launchServerHandler(String id, DaemonProperties props).
     * @param p the ServerHandlerManager properties.
     * @see launchServerHandler
     */
    protected void fixProperties(Properties p) {
    }

    /**
     * Launch a new server handler.
     * This method tries to launch a new server handler. If launching
     * succeeds, it returns happily, otherwise, it emits an error message
     * to the standard error stream.
     * @param identifier The identifier of the server handler to be launched.
     * @param props The properties from which the server handler should
     * initialize itself.
     */
    protected void launchServerHandler(String id, DaemonProperties props) {
        ServerHandler server = null;
        String cls = props.getString(id + "." + CLASS_P, null);
        if (cls != null) {
            try {
                Class c = Class.forName(cls);
                server = (ServerHandler) c.newInstance();
                server = new org.w3c.jigsaw.http.httpd();
                ObservableProperties p = props.loadPropertySpace(id);
                this.fixProperties(p);
                server.initialize(this, id, p);
            } catch (FileNotFoundException ex) {
                error("Unable to launch " + id + ", no properties found:" + ex.getMessage());
                return;
            } catch (IOException ex) {
                error("Unable to launch " + id + ", IO error in reading props.");
                return;
            } catch (ClassNotFoundException ex) {
                error("Unable to launch " + id + ": class " + cls + " not found.");
                return;
            } catch (InstantiationException ex) {
                error("Unable to launch " + id + ": unable to instanciate " + cls);
                return;
            } catch (IllegalAccessException ex) {
                error("Unable to launch " + id + ": illegal access to " + cls);
                return;
            } catch (ServerHandlerInitException ex) {
                error("Unable to launch " + id + ": " + ex.getMessage());
                return;
            }
        } else {
            String clones = props.getString(id + "." + CLONES_P, null);
            if (clones == null) {
                error("Unable to launch " + id + ": no class or clones defined.");
                return;
            }
            server = lookupServerHandler(clones);
            if (server == null) {
                error("Unable to launch " + id + ": " + clones + " doesn't exit.");
                return;
            }
            try {
                server = server.clone(this, id, props.loadPropertySpace(id));
            } catch (Exception ex) {
                error("Unable to launch " + id + ": clone failed on " + server + "\r\n" + ex.getMessage());
                return;
            }
        }
        handlers.put(id, server);
    }

    /**
     * Lookup the server handler having the given identifier.
     * @param id The identifier of the server handler to look for.
     * @return A ServerHandler instance, or <strong>null</strong> if
     * undefined.
     */
    public ServerHandler lookupServerHandler(String id) {
        return (ServerHandler) handlers.get(id);
    }

    /**
     * Enumerate all the server handler manager's identifiers.
     * @return An enumeration of String.
     */
    public Enumeration enumerateServerHandlers() {
        return handlers.keys();
    }

    /**
     * Remove a server handler from this manager
     * @param server, the Server Handler to remove
     */
    public void removeServerHandler(ServerHandler server) {
        handlers.remove(server.getIdentifier());
    }

    /**
     * Create and initialize a fresh server handler manager.
     * Each server handler declared in the properties is launched in turn.
     * If no server handlers is declared, or if none of them is initializable
     * the server manager is not created and a RuntimeException is thrown,
     * otherwise, if at least one server handler was initialized, the server
     * manager emits appropriate error messages to the error stream for each
     * of the server handlers whose launch failed.
     * @param props The properties this manager should be initialized from.
     * @exception RuntimeException If no server handlers was declared through
     * the properties.
     */
    public ServerHandlerManager(String args[], File config, Properties p) {
        this.commandLine = args;
        this.handlers = new Hashtable(7);
        this.props = new DaemonProperties(config, p);
        String hprop = props.getProperty(HANDLERS_P);
        if ((hprop == null) || hprop.equals("")) fatal("The property [org.w3c.jigsaw.daemon.handlers] is " + " undefined.");
        StringTokenizer st = new StringTokenizer(hprop, "|");
        edu.hkust.clap.monitor.Monitor.loopBegin(646);
while (st.hasMoreTokens()) { 
edu.hkust.clap.monitor.Monitor.loopInc(646);
{
            String id = st.nextToken();
            launchServerHandler(id, props);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(646);

        if (handlers.size() <= 0) fatal("No servers initialized !");
        unixStuff();
        Class c = java.lang.Runtime.class;
        Class cp[] = { java.lang.Thread.class };
        try {
            if (debug) System.out.println("*** shutdownHook succesfully registered");
        } catch (Exception ex) {
            if (debug) ex.printStackTrace();
        }
        Enumeration e = enumerateServerHandlers();
        ServerHandler s;
        edu.hkust.clap.monitor.Monitor.loopBegin(647);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(647);
{
            String id = (String) e.nextElement();
            s = lookupServerHandler(id);
            try {
                s.start();
            } catch (ServerHandlerInitException ex) {
                error("Unable to start " + id + ": " + ex.getMessage());
                s.shutdown();
                removeServerHandler(s);
            } catch (Exception ex) {
                error("Unknown error while starting " + id + ": " + ex.getMessage());
                s.shutdown();
                removeServerHandler(s);
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(647);

        if (handlers.size() <= 0) fatal("No servers launched !");
    }

    /**
     * Shutdown this server handler manager.
     * This method will call the shutdown method of all the running servers
     * stopping the manager.
     * <p>This server handler clones are considered shutdown too.
     */
    public synchronized void shutdown() {
        Enumeration sk = handlers.keys();
        ServerHandler server;
        edu.hkust.clap.monitor.Monitor.loopBegin(648);
while (sk.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(648);
{
            String servname = (String) sk.nextElement();
            server = (ServerHandler) lookupServerHandler(servname);
            server.shutdown();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(648);

    }

    /**
     * Do some UNIX specific initialization.
     * THis method exists straight if it cannot succeed !
     */
    public void unixStuff() {
        String user = props.getString(SERVER_USER_P, null);
        String group = props.getString(SERVER_GROUP_P, null);
        Unix unix = null;
        if ((user != null) || (group != null)) {
            unix = Unix.getUnix();
            if (!unix.isResolved()) {
                String msg = ("You used either the -user or -group command " + " line option" + " usefull only when running Jigsaw under UNIX." + "To be able to set these, Jigsaw requires " + " that libUnix, distributed with Jigsaw, " + "be accessible from your LD_LIBRARY_PATH.");
                fatal(msg);
            }
        }
        if (group != null) {
            boolean error = false;
            int gid = unix.getGID(group);
            if (gid >= 0) {
                try {
                    unix.setGID(gid);
                } catch (UnixException ex) {
                    error = true;
                }
            }
            if ((gid < 0) || error) {
                String msg = ("UNIX initialization, unable to setgid(2) to " + group + ". Check the -group command line option." + ((gid < 0) ? "(the group doesn't exist)." : "(setgid call failed)."));
                fatal(msg);
            }
            System.out.println("+ running as group \"" + group + "\".");
        }
        if (user != null) {
            boolean error = false;
            int uid = unix.getUID(user);
            if (uid >= 0) {
                try {
                    unix.setUID(uid);
                } catch (UnixException ex) {
                    error = true;
                }
            }
            if ((uid < 0) || error) {
                String msg = ("UNIX initialization, unable to setuid(2) to " + user + ". Check the -user command line option." + ((uid < 0) ? "(the user doesn't exist)." : "(setuid call failed)."));
                fatal(msg);
            }
            System.out.println("+ running as user \"" + user + "\".");
        }
    }

    public static void usage() {
        PrintStream o = System.out;
        o.println("usage: httpd [OPTIONS]");
        o.println("-root <directory> : root directory of server.");
        o.println("-p     <propfile> : property file to read.");
        o.println("-trace            : turns debugging on.");
        o.println("-chroot <root>    : chroot Jigsaw at startup.");
        o.println("-config <dir>     : use given directory as config dir.");
        o.println("-user <user>      : identity of the server (UNIX only).");
        o.println("-group <group>    : group of the server (UNIX only).");
        o.println("-maxstores <int>  : Max number of stores in memory.");
        System.exit(1);
    }

    public static void main(String args[]) {
        Integer cmdport = null;
        String cmdhost = null;
        String cmdroot = null;
        String cmdprop = "server.props";
        String chroot = null;
        Boolean cmdtrace = null;
        String cmdconfig = null;
        String cmduser = null;
        String cmdgroup = null;
        String maxstores = null;
        edu.hkust.clap.monitor.Monitor.loopBegin(649);
for (int i = 0; i < args.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(649);
{
            if (args[i].equals("-port")) {
                try {
                    cmdport = new Integer(args[++i]);
                } catch (NumberFormatException ex) {
                    System.out.println("invalid port number [" + args[i] + "]");
                    System.exit(1);
                }
            } else if (args[i].equals("-host") && (i + 1 < args.length)) {
                cmdhost = args[++i];
            } else if (args[i].equals("-maxstores") && (i + 1 < args.length)) {
                maxstores = args[++i];
            } else if (args[i].equals("-root") && (i + 1 < args.length)) {
                cmdroot = args[++i];
            } else if (args[i].equals("-p") && (i + 1 < args.length)) {
                cmdprop = args[++i];
            } else if (args[i].equals("-trace")) {
                cmdtrace = Boolean.TRUE;
            } else if (args[i].equals("-chroot") && (i + 1 < args.length)) {
                chroot = args[++i];
            } else if (args[i].equals("-group") && (i + 1 < args.length)) {
                cmdgroup = args[++i];
            } else if (args[i].equals("-user") && (i + 1 < args.length)) {
                cmduser = args[++i];
            } else if (args[i].equals("-config") && (i + 1 < args.length)) {
                cmdconfig = args[++i];
            } else if (args[i].equals("?") || args[i].equals("-help")) {
                usage();
            } else {
                System.out.println("unknown option: [" + args[i] + "]");
                System.exit(1);
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(649);

        if (chroot != null) {
            Unix unix = Unix.getUnix();
            if (!unix.isResolved()) {
                System.out.println("You cannot chroot Jigsaw if the libUnix " + " native extension is not available. " + "Check the documentation, or remove " + " this argument from command line.");
                System.exit(1);
            }
            try {
                unix.chroot(chroot);
            } catch (Exception ex) {
                System.out.println("The chroot system call failed, details:");
                ex.printStackTrace();
                System.exit(1);
            }
        }
        Properties props = System.getProperties();
        if (cmdroot == null) cmdroot = props.getProperty("user.dir", null);
        File configdir = new File(cmdroot, (cmdconfig == null) ? "config" : cmdconfig);
        File propfile = new File(configdir, cmdprop);
        System.out.println("loading properties from: " + propfile.getAbsolutePath());
        try {
            props.load(new FileInputStream(propfile));
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to load properties: " + cmdprop);
            System.out.println("\t" + ex.getMessage());
            System.exit(1);
        } catch (IOException ex) {
            System.out.println("Unable to load properties: " + cmdprop);
            System.out.println("\t" + ex.getMessage());
            System.exit(1);
        }
        if (cmdconfig != null) props.put(httpd.CONFIG_P, new File(cmdroot, cmdconfig).getAbsolutePath());
        if (cmdport != null) props.put(httpd.PORT_P, cmdport.toString());
        if (cmdhost != null) props.put(httpd.HOST_P, cmdhost);
        if (cmdroot != null) props.put(httpd.ROOT_P, cmdroot);
        if (cmdtrace != null) {
            props.put(httpd.TRACE_P, "true");
            props.put(httpd.CLIENT_DEBUG_P, "true");
        }
        if (maxstores != null) props.put(httpd.MAX_LOADED_STORE_P, maxstores);
        if (cmdgroup != null) props.put(SERVER_GROUP_P, cmdgroup);
        if (cmduser != null) props.put(SERVER_USER_P, cmduser);
        new ServerHandlerManager(args, configdir, props);
    }
}
