package org.w3c.www.protocol.http.cache.push;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;

/**
 * PushCacheListener
 * Accepts incoming connections on specified port number and creates 
 * PushCacheHandler objects to handle dialogue with client.
 *
 * @author Paul Henshaw, The Fantastic Corporation, Paul.Henshaw@fantastic.com
 * @version $Revision: 1.1 $
 * $Id: PushCacheListener.java,v 1.1 2010/06/15 12:25:44 smhuang Exp $
 */
public class PushCacheListener extends Thread {

    private int _port_number;

    private ServerSocket _socket = null;

    private boolean _running = false;

    private ArrayList _list = null;

    private ShutdownHook _hook = null;

    private boolean _cleaning = false;

    public class ShutdownHook extends Thread {

        public ShutdownHook() {
        }

        public void run() {
            try {
                stopRunning();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** 
     * Register a hanlder with the listener.
     * stopRunning will be called on all registered handlers when
     * the listener is stopped.
     */
    protected void registerHandler(PushCacheHandler handler) {
        _list.add(handler);
    }

    /**
     * Reregister a handler from the listener
     */
    protected void deregisterHandler(PushCacheHandler handler) {
        if (!_cleaning) {
            _list.remove(handler);
        }
    }

    /**
     * Close sockets, stop handlers.  
     */
    protected void cleanup() {
        if (_cleaning) {
            return;
        }
        _cleaning = true;
        try {
            _running = false;
            if (_socket != null) {
                _socket.close();
            }
            _socket = null;
        } catch (java.io.IOException e) {
        }
        edu.hkust.clap.monitor.Monitor.loopBegin(527);
for (int i = 0; i < _list.size(); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(527);
{
            PushCacheHandler handler = (PushCacheHandler) _list.get(i);
            handler.stopRunning();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(527);

        _list.clear();
        _list = null;
    }

    /**
     * Request this thread to exit gracefully
     */
    public void stopRunning() {
        cleanup();
    }

    /**
     * Listen for connections, creating a handler for each new connection
     */
    public void run() {
        _running = true;
        try {
            edu.hkust.clap.monitor.Monitor.loopBegin(528);
while (_running) { 
edu.hkust.clap.monitor.Monitor.loopInc(528);
{
                Socket s = _socket.accept();
                PushCacheHandler handler = new PushCacheHandler(this, s);
                handler.start();
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(528);

        } catch (Exception e) {
            e.printStackTrace();
        }
        cleanup();
    }

    /**
     * Construct a PushCacheListener 
     * @param port_number  port number on which to listen.  
     */
    public PushCacheListener(int port_number) throws java.io.IOException {
        super();
        _port_number = port_number;
        _list = new ArrayList();
        _socket = new ServerSocket(_port_number);
        Class _c = java.lang.Runtime.class;
        Class _cp[] = { java.lang.Thread.class };
        try {
            Method _m = _c.getMethod("addShutdownHook", _cp);
            Runtime _r = Runtime.getRuntime();
            _hook = new ShutdownHook();
            Object[] _param = { _hook };
            _m.invoke(_r, _param);
        } catch (NoSuchMethodException ex) {
            _hook = null;
        } catch (InvocationTargetException ex) {
        } catch (IllegalAccessException ex) {
        }
    }
}
