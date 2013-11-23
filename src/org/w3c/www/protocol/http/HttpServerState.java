package org.w3c.www.protocol.http;

import java.util.Vector;
import java.util.Enumeration;

class HttpServerState {

    HttpServer server = null;

    Vector conns = null;

    protected int state = 0;

    protected HttpException ex = null;

    protected int num_conn = 0;

    protected static final int PREINIT = 0;

    protected static final int ERROR = 1;

    protected static final int OK = 2;

    private static final boolean debug = false;

    final HttpServer getServer() {
        return server;
    }

    synchronized void incrConnectionCount() {
        ++num_conn;
    }

    synchronized void decrConnectionCount() {
        --num_conn;
    }

    synchronized int getConnectionCount() {
        return num_conn;
    }

    synchronized boolean notEnoughConnections() {
        return (conns == null) || (conns.size() == 0);
    }

    synchronized void registerConnection(HttpConnection conn) {
        if (conns == null) {
            conns = new Vector(4);
        }
        conns.addElement(conn);
    }

    synchronized void unregisterConnection(HttpConnection conn) {
        if (conns != null) {
            conns.removeElement(conn);
        }
    }

    synchronized void deleteConnection(HttpConnection conn) {
        if (conns != null) {
            conns.removeElement(conn);
        }
    }

    synchronized boolean hasConnection() {
        return (conns != null) && (conns.size() > 0);
    }

    synchronized HttpConnection getConnection() {
        if ((conns != null) && (conns.size() > 0)) {
            Enumeration e = conns.elements();
            HttpConnection conn = null;
            edu.hkust.clap.monitor.Monitor.loopBegin(483);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(483);
{
                HttpConnection tmp_conn = (HttpConnection) e.nextElement();
                if (tmp_conn.mayReuse()) {
                    conn = tmp_conn;
                    conns.removeElement(conn);
                    break;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(483);

            if (conn == null) {
                conn = (HttpConnection) conns.elementAt(0);
                conns.removeElementAt(0);
            }
            conn.cached = true;
            return conn;
        }
        return null;
    }

    public String toString() {
        String tostring = "";
        if (conns == null) tostring = "null"; else if (conns.size() == 0) tostring = "empty"; else {
            edu.hkust.clap.monitor.Monitor.loopBegin(484);
for (int i = 0; i < conns.size(); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(484);
{
                tostring += "[" + ((HttpConnection) conns.elementAt(i)).toString() + "]";
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(484);

        }
        return "" + num_conn + tostring;
    }

    HttpServerState(HttpServer server) {
        this.server = server;
    }
}
