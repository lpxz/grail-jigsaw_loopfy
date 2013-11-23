package org.w3c.jigsaw.servlet;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import javax.servlet.Servlet;

/**
 *  @author Alexandre Rafalovitch <alex@access.com.au>
 *  @author Anselm Baird-Smith <abaird@w3.org>
 */
public class ServletNamesEnumeration implements Enumeration {

    Enumeration children = null;

    String next = null;

    ServletDirectoryFrame dir = null;

    ServletNamesEnumeration(ServletDirectoryFrame dir, Enumeration children) {
        this.dir = dir;
        this.children = children;
    }

    private final synchronized String computeNext() {
        if (next != null) return next;
        edu.hkust.clap.monitor.Monitor.loopBegin(829);
while (children.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(829);
{
            next = (String) children.nextElement();
            if (dir.isServletLoaded(next)) {
                return next;
            } else {
                next = null;
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(829);

        return null;
    }

    public synchronized boolean hasMoreElements() {
        return (next != null) || ((next = computeNext()) != null);
    }

    public synchronized Object nextElement() {
        if (next == null) {
            next = computeNext();
        }
        if (next != null) {
            Object ret = next;
            next = null;
            return ret;
        } else {
            throw new NoSuchElementException("NextElement");
        }
    }
}
