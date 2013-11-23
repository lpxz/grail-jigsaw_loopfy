package org.w3c.jigsaw.pagecompile;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;

class ClassEntry {

    long classStamp = 0;

    Class generatedClass = null;

    File classFile = null;

    boolean systemClass = false;

    public boolean isModified() {
        if (!systemClass) return (classFile.lastModified() > classStamp);
        return false;
    }

    public void update() {
        if (!systemClass) classStamp = classFile.lastModified();
    }

    public ClassEntry(Class generatedClass) {
        this.generatedClass = generatedClass;
        this.systemClass = true;
    }

    public ClassEntry(File classFile, Class generatedClass) {
        this.classFile = classFile;
        this.generatedClass = generatedClass;
        if (classFile != null) this.classStamp = classFile.lastModified();
        this.systemClass = false;
    }
}

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class GeneratedClassLoader extends ClassLoader {

    private static final boolean debug = false;

    private Hashtable classes = null;

    private int CLASSES_DEFAULT_SIZE = 13;

    private File generatedClassDirectory = null;

    private void trace(String msg) {
        trace(msg, true);
    }

    private void trace(String msg, boolean display) {
        if (display) System.out.println(msg);
    }

    private String removeSpace(String string) {
        int start = -1;
        int end = string.length();
        boolean spaces = false;
        edu.hkust.clap.monitor.Monitor.loopBegin(270);
while (string.charAt(++start) == ' ') { 
edu.hkust.clap.monitor.Monitor.loopInc(270);
{
            spaces = true;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(270);

        ;
        edu.hkust.clap.monitor.Monitor.loopBegin(271);
while (string.charAt(--end) == ' ') { 
edu.hkust.clap.monitor.Monitor.loopInc(271);
{
            spaces = true;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(271);

        ;
        if (spaces) return string.substring(start, ++end); else return new String(string);
    }

    private String packaged(String name) {
        String cname = removeSpace(name);
        try {
            if (cname.endsWith(".class")) {
                int idx = cname.lastIndexOf('.');
                if (idx != -1) cname = cname.substring(0, idx);
            }
            return cname.replace('.', '/') + ".class";
        } catch (Exception ex) {
            return name;
        }
    }

    private String noext(String name) {
        int idx = name.lastIndexOf(".class");
        if (idx != -1) {
            return name.substring(0, idx);
        }
        return name;
    }

    protected boolean classChanged(String name) {
        ClassEntry entry = (ClassEntry) classes.get(noext(name));
        if (entry != null) {
            if (debug) {
                System.out.println("entry    : " + name);
                if (!entry.systemClass) {
                    System.out.println("file     : " + entry.classFile.getAbsolutePath());
                    System.out.println("Stamp    : " + entry.classStamp);
                }
                System.out.println("System   : " + entry.systemClass);
                System.out.println("modified : " + entry.isModified());
            }
            return entry.isModified();
        }
        return false;
    }

    /**
     * Get a cached class.
     * @return a Class instance
     * @exception ClassNotFoundException if the Class can't be found
     */
    protected final Class getCachedClass(String name, boolean resolve) throws ClassNotFoundException {
        ClassEntry entry = (ClassEntry) classes.get(noext(name));
        if (entry != null) {
            if (!entry.isModified()) {
                trace(name + ": not modified", debug);
                return entry.generatedClass;
            } else if (!entry.systemClass) {
                entry.generatedClass = loadClassFile(entry.classFile);
                if (resolve) resolveClass(entry.generatedClass);
                entry.update();
                trace(name + ": reloaded", debug);
                return entry.generatedClass;
            }
        }
        return null;
    }

    protected void checkPackageAccess(String name) {
        SecurityManager s = System.getSecurityManager();
        if (s != null) {
            int i = name.lastIndexOf('.');
            if (i >= 0) s.checkPackageAccess(name.substring(0, i));
        }
    }

    /**
     * Given the class name, return its File name.
     * @param name The class to be loaded.
     * @return The File for the class.
     */
    protected File locateClass(String name) {
        File classfile = null;
        File base = generatedClassDirectory;
        String cname = null;
        cname = packaged(name);
        classfile = new File(base, cname);
        if (classfile != null) if (classfile.exists()) return classfile;
        return null;
    }

    /**
     * Load a Class from its class file..
     * @return a Class instance
     * @exception ClassNotFoundException if the Class can't be found
     */
    protected Class loadClassFile(File file) throws ClassNotFoundException {
        byte data[] = null;
        if (file == null) throw new ClassNotFoundException("invalid class base");
        trace("located at " + file, debug);
        try {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
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
            if (debug) ex.printStackTrace();
            throw new ClassNotFoundException(file.getAbsolutePath());
        }
        return defineClass(data, 0, data.length);
    }

    /**
     * Get a new class.
     * @return a Class instance
     * @exception ClassNotFoundException if the Class can't be found
     */
    protected final Class getNewClass(File classfile, String name, boolean resolve) throws ClassNotFoundException {
        Class c = null;
        c = loadClassFile(classfile);
        if (resolve) resolveClass(c);
        classes.put(noext(name), new ClassEntry(classfile, c));
        trace(name + ": loading new class done.", debug);
        return c;
    }

    /**
     * Load a class.
     * @return a Class instance
     * @exception ClassNotFoundException if the Class can't be found
     */
    protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class c = null;
        trace(name + ": loading class", debug);
        c = getCachedClass(name, resolve);
        if (c != null) return c;
        checkPackageAccess(name);
        File file = locateClass(name);
        if (file == null) {
            try {
                if ((c = findSystemClass(name)) != null) {
                    trace(name + ": system class", debug);
                    classes.put(noext(name), new ClassEntry(c));
                    return c;
                } else throw new ClassNotFoundException(name);
            } catch (Exception ex) {
                throw new ClassNotFoundException(name);
            }
        }
        return getNewClass(file, name, resolve);
    }

    public URL getResource(String name) {
        URL resource = getSystemResource(name);
        if (resource != null) return resource;
        String url = generatedClassDirectory.getAbsolutePath();
        if (File.separatorChar != '/') url = url.replace(File.separatorChar, '/');
        File file = new File(url, name);
        if (!file.exists()) return null;
        try {
            return new URL("file", "localhost", url + "/" + name);
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    /**
     * Get a resource as a stream.
     * @param name The name of the resource to locate.
     */
    public InputStream getResourceAsStream(String name) {
        InputStream in = getSystemResourceAsStream(name);
        if (in != null) return in;
        URL resource = getResource(name);
        if (resource == null) return null;
        try {
            URLConnection c = resource.openConnection();
            return c.getInputStream();
        } catch (IOException ex) {
            return null;
        }
    }

    protected GeneratedClassLoader(GeneratedClassLoader loader) {
        super();
        this.generatedClassDirectory = loader.generatedClassDirectory;
        this.classes = loader.classes;
    }

    protected GeneratedClassLoader(File generatedClassDirectory) {
        super();
        this.generatedClassDirectory = generatedClassDirectory;
        this.classes = new Hashtable(CLASSES_DEFAULT_SIZE);
    }
}
