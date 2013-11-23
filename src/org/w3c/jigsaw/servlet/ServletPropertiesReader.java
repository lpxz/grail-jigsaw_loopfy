package org.w3c.jigsaw.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import javax.servlet.ServletException;
import org.w3c.jigsaw.http.httpd;
import org.w3c.jigsaw.http.httpdPreloadInterface;
import org.w3c.util.LookupTable;
import org.w3c.util.ArrayDictionary;
import org.w3c.tools.resources.DirectoryResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.MultipleLockException;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.LookupState;
import org.w3c.tools.resources.LookupResult;

/**
 * @version $Revision: 1.2 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class ServletPropertiesReader implements httpdPreloadInterface {

    public static final String SERVLET_PROPS_FILE = "servlets.properties";

    public static final String SERVLET_BASE_P = "/servlet";

    public static final String ALLOW_DELETE_P = "allow_delete";

    public static final String CODE_P = "code";

    public static final String INIT_ARGS_P = "initArgs";

    public static final String DESCRIPTION_P = "description";

    public static final String CODEBASE_P = "codebase";

    public static final String ICON_P = "icon";

    public static final String ARGS_SEPARATOR = ",";

    public static final String STARTUP_P = "startup";

    protected LookupTable general = null;

    protected LookupTable servlets = null;

    protected static Class frameclass = null;

    static {
        try {
            frameclass = Class.forName("org.w3c.jigsaw.servlet.ServletWrapperFrame");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Load the servlets configuration from servlets.properties.
     * @param server the http server to configure
     */
    public void preload(httpd server) {
        File dir = server.getConfigDirectory();
        File servletProps = new File(dir, SERVLET_PROPS_FILE);
        if (servletProps.exists()) {
            readProperties(servletProps);
            Enumeration e = servlets.keys();
            ResourceReference sdir = getServletDirectoryReference(server);
            if (sdir == null) {
                throw new RuntimeException("No servlet directory defined!");
            }
            edu.hkust.clap.monitor.Monitor.loopBegin(1071);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(1071);
{
                String name = (String) e.nextElement();
                initializeServlet(name, server, sdir);
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1071);

            String startups = (String) general.get(STARTUP_P);
            if (startups != null) {
                StringTokenizer st = new StringTokenizer(startups, ARGS_SEPARATOR);
                FramedResource root = server.getRoot();
                LookupState ls = null;
                LookupResult lr = null;
                String name = null;
                String uri = null;
                edu.hkust.clap.monitor.Monitor.loopBegin(1072);
while (st.hasMoreTokens()) { 
edu.hkust.clap.monitor.Monitor.loopInc(1072);
{
                    name = st.nextToken();
                    uri = SERVLET_BASE_P + "/" + name;
                    try {
                        ls = new LookupState(uri);
                        lr = new LookupResult(root.getResourceReference());
                        root.lookup(ls, lr);
                        ResourceReference rr = lr.getTarget();
                        if (rr != null) {
                            try {
                                ServletWrapper wrapper = (ServletWrapper) rr.lock();
                                wrapper.checkServlet();
                            } catch (InvalidResourceException ex) {
                                ex.printStackTrace();
                            } catch (ClassNotFoundException cnfex) {
                                cnfex.printStackTrace();
                            } catch (ServletException sex) {
                                sex.printStackTrace();
                            } finally {
                                rr.unlock();
                            }
                        }
                    } catch (ProtocolException pex) {
                        pex.printStackTrace();
                    }
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1072);

            }
        }
    }

    /**
     * Get the servlet directory reference.
     * @return a ResourceReference
     */
    protected ResourceReference getServletDirectoryReference(httpd server) {
        ResourceReference rr = server.getEditRoot();
        try {
            FramedResource root = (FramedResource) rr.lock();
            try {
                LookupState ls = new LookupState(SERVLET_BASE_P);
                LookupResult lr = new LookupResult(root.getResourceReference());
                root.lookup(ls, lr);
                return lr.getTarget();
            } catch (ProtocolException ex) {
                ex.printStackTrace();
                return null;
            }
        } catch (InvalidResourceException ex) {
            return null;
        } finally {
            rr.unlock();
        }
    }

    /**
     * Initialize a servlet or create it if not found
     * @param name the servlet's name
     * @param server the http server
     */
    protected void initializeServlet(String name, httpd server, ResourceReference sdir) {
        String uri = SERVLET_BASE_P + "/" + name;
        ResourceReference rr = server.getEditRoot();
        try {
            FramedResource root = (FramedResource) rr.lock();
            try {
                DirectoryResource parent = (DirectoryResource) sdir.lock();
                LookupState ls = new LookupState(uri);
                LookupResult lr = new LookupResult(root.getResourceReference());
                root.lookup(ls, lr);
                ResourceReference target = lr.getTarget();
                if (target != null) {
                    try {
                        ServletWrapper wrapper = (ServletWrapper) target.lock();
                        initialize(name, wrapper, parent);
                    } finally {
                        target.unlock();
                    }
                } else {
                    initialize(name, null, parent);
                }
            } catch (ProtocolException pex) {
                pex.printStackTrace();
            } catch (InvalidResourceException ex) {
                ex.printStackTrace();
            } catch (MultipleLockException mlex) {
                mlex.printStackTrace();
            } catch (ClassCastException ccex) {
                ccex.printStackTrace();
            } finally {
                sdir.unlock();
            }
        } catch (InvalidResourceException ex) {
        } finally {
            rr.unlock();
        }
    }

    /**
     * Initialize a ServletWrapper.
     * @param name the servlet's name
     * @param wrapper the ServletWrapper (or null)
     */
    protected void initialize(String name, ServletWrapper wrapper, DirectoryResource parent) throws InvalidResourceException, MultipleLockException {
        LookupTable props = (LookupTable) servlets.get(name);
        String value = (String) props.get(CODEBASE_P);
        if (value != null) {
            if (wrapper == null) {
                wrapper = new RemoteServletWrapper();
                Hashtable defs = new Hashtable();
                defs.put("servlet-base", value);
                parent.registerResource(name, wrapper, defs);
            } else if (wrapper instanceof RemoteServletWrapper) {
                int idx = RemoteServletWrapper.ATTR_SERVLET_BASE;
                wrapper.setSilentValue(idx, value);
            } else {
                RemoteServletWrapper rwrapper = new RemoteServletWrapper();
                Hashtable defs = new Hashtable();
                defs.put("servlet-base", value);
                String sclass = wrapper.getServletClass();
                if (sclass != null) {
                    defs.put("servlet-class", wrapper.getServletClass());
                }
                ArrayDictionary params = wrapper.getServletParameters();
                if (params != null) {
                    defs.put("servlet-parameters", params);
                }
                Object timeout = wrapper.getValue(wrapper.ATTR_SERVLET_TIMEOUT, null);
                if (timeout != null) {
                    defs.put("servlet-timeout", timeout);
                }
                wrapper.delete();
                parent.registerResource(name, rwrapper, defs);
            }
        } else if (wrapper == null) {
            wrapper = new ServletWrapper();
            parent.registerResource(name, wrapper, null);
        }
        value = (String) props.get(CODE_P);
        if (value != null) {
            wrapper.setSilentValue(wrapper.ATTR_SERVLET_CLASS, value);
        }
        value = (String) props.get(INIT_ARGS_P);
        if (value != null) {
            ArrayDictionary args = new ArrayDictionary();
            StringTokenizer st = new StringTokenizer(value, ARGS_SEPARATOR);
            edu.hkust.clap.monitor.Monitor.loopBegin(1073);
while (st.hasMoreTokens()) { 
edu.hkust.clap.monitor.Monitor.loopInc(1073);
{
                String arg = st.nextToken();
                int idx = arg.indexOf('=');
                if (idx != -1) {
                    value = arg.substring(idx + 1);
                    arg = arg.substring(0, idx);
                    args.put(arg, value);
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1073);

            wrapper.setSilentValue(wrapper.ATTR_PARAMETERS, args);
        }
        ServletWrapperFrame frame = (ServletWrapperFrame) wrapper.getFrame(frameclass);
        value = (String) props.get(DESCRIPTION_P);
        if (value != null) {
            frame.setSilentValue("title", value);
        }
        value = (String) props.get(ICON_P);
        if (value != null) {
            frame.setSilentValue("icon", value);
        }
    }

    /**
     * Read the servlets.properties file
     * @param file the servlets.properties file.
     */
    protected void readProperties(File file) {
        Properties props = new Properties();
        servlets = new LookupTable();
        general = new LookupTable();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(file));
            props.load(in);
            in.close();
        } catch (FileNotFoundException fnfex) {
        } catch (IOException ioex) {
        }
        Enumeration e = props.propertyNames();
        edu.hkust.clap.monitor.Monitor.loopBegin(1074);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(1074);
{
            String property = (String) e.nextElement();
            if (property.startsWith("servlet.")) {
                String value = props.getProperty(property);
                property = property.substring(8);
                int idx = property.indexOf('.');
                if (idx != -1) {
                    String name = property.substring(0, idx);
                    property = property.substring(idx + 1);
                    if (idx != -1) {
                        LookupTable lt = (LookupTable) servlets.get(name);
                        if (lt == null) {
                            lt = new LookupTable();
                            servlets.put(name, lt);
                        }
                        lt.put(property, value);
                    }
                }
            } else if (property.startsWith("servlets.")) {
                String value = props.getProperty(property);
                String name = property.substring(9);
                general.put(name, value);
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1074);

    }
}
