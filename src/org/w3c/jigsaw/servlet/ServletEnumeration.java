package org.w3c.jigsaw.servlet;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import javax.servlet.Servlet;

/**
 *  @author Alexandre Rafalovitch <alex@access.com.au>
 *  @author Anselm Baird-Smith <abaird@w3.org>
 */
public class ServletEnumeration implements Enumeration {

    Enumeration children = null;

    Servlet next = null;

    ServletDirectoryFrame dir = null;

    ServletEnumeration(ServletDirectoryFrame dir, Enumeration children) {
        this.dir = dir;
        this.children = children;
    }

    private final synchronized Servlet computeNext() {
        if (next != null) return next;
        edu.hkust.clap.monitor.Monitor.loopBegin(261);
while (children.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(261);
{
            next = dir.getServlet((String) children.nextElement());
            if (next != null) return next;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(261);

        return null;
    }

    public synchronized boolean hasMoreElements() {
        return (next != null) || ((next = computeNext()) != null);
    }

    public synchronized Object nextElement() {
        if (next == null) next = computeNext();
        if (next != null) {
            Object ret = next;
            next = null;
            return ret;
        } else {
            throw new NoSuchElementException("NextElement");
        }
    }
}
