package org.w3c.jigsaw.servlet;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;

/**
 * @author Alexandre Rafalovitch <alex@access.com.au>
 * @author Anselm Baird-Smith <abaird@w3.org>
 */
public class ServletLoader extends ClassLoader {

    private static final boolean debug = false;

    private Hashtable classes = null;

    RemoteServletWrapper wrapper = null;

    private void trace(String msg) {
        System.out.println("[" + wrapper.getURLPath() + "]: " + msg);
    }

    /**
     * Given the class name, return its URL.
     * @param name The class to be loaded.
     * @return The URL for the class.
     */
    protected URL locateClass(String name) {
        String base = wrapper.getServletBase();
        try {
            String cname = name.replace('.', '/') + ".class";
            return new URL(new URL(base), cname);
        } catch (Exception ex) {
        }
        return null;
    }

    /**
     * Try to load class.
     * @param name The class name.
     * @param boolean The resolve flag
     * @return a Class instance
     * @exception ClassNotFoundException if the class file can't be found.
     */
    protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (name.endsWith(".class")) name = name.substring(0, name.lastIndexOf('.'));
        Class c = null;
        if (debug) trace("loading " + name);
        if ((c = (Class) classes.get(name)) != null) return c;
        SecurityManager s = System.getSecurityManager();
        if (s != null) {
            int i = name.lastIndexOf('.');
            if (i >= 0) s.checkPackageAccess(name.substring(0, i));
        }
        try {
            if ((c = findSystemClass(name)) != null) return c;
        } catch (Exception ex) {
        }
        byte data[] = null;
        URL url = locateClass(name);
        if (url == null) throw new ClassNotFoundException("invalid servlet base");
        if (debug) trace(name + " located at " + url);
        try {
            URLConnection conn = url.openConnection();
            InputStream in = conn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream(512);
            byte buf[] = new byte[512];
            edu.hkust.clap.monitor.Monitor.loopBegin(272);
for (int got = 0; (got = in.read(buf)) > 0; ) { 
edu.hkust.clap.monitor.Monitor.loopInc(272);
out.write(buf, 0, got);} 
edu.hkust.clap.monitor.Monitor.loopEnd(272);

            data = out.toByteArray();
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ClassNotFoundException(url.toExternalForm());
        }
        c = defineClass(data, 0, data.length);
        if (resolve) resolveClass(c);
        classes.put(name, c);
        if (debug) trace(name + ": loading done.");
        return c;
    }

    protected ServletLoader(RemoteServletWrapper wrapper) {
        super();
        this.wrapper = wrapper;
        this.classes = new Hashtable(13);
    }
}
