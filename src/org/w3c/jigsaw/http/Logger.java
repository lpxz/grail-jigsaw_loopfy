package org.w3c.jigsaw.http;

/**
 * The Logger class is the abstract class that loggers must implement. 
 * You can (should be able to) use several loggers at the same time.
 */
public abstract class Logger {

    /**
     * Log normally a full handled request. 
     * @param client The client which made the request.
     * @param request The request that was handled.
     * @param reply The emitted reply to the client.
     * @param nbytes The number of bytes sent to this client.
     * @param duration The time it took to process the request.
     */
    public abstract void log(Request request, Reply reply, int nbytes, long duration);

    /**
     * Log a message to the log.
     * @param msg The message to log.
     */
    public abstract void log(String msg);

    /**
     * Log an error on behalf of some client object in the error log. 
     * @param client The client for which the error occured. 
     * @param msg The error message to log.  
     */
    public abstract void errlog(Client client, String msg);

    /**
     * Log an error on behalf of the server object.
     * @param msg The message to emit.
     */
    public abstract void errlog(String msg);

    /**
     * Log a client trace. The client may be in some error state, so all access
     * to the client parameter should be checked.
     * @param client The client that wants to emit a trace.
     * @param trace The trace to log.
     */
    public abstract void trace(Client client, String trace);

    /**
     * Log a server trace.
     * @param msg The trace to emit.
     */
    public abstract void trace(String msg);

    /**
     * Flush any in core logger state back to disk.
     * Some loggers may use memory cache before writing any data to the disk
     * this method ensures that there state is saved to stable storage.
     */
    public abstract void sync();

    /**
     * Shutdown this logger object.
     * Each server will close the shutdown method of the logger before
     * shuting itself down.
     */
    public abstract void shutdown();

    /**
     * Initialize this logger for the provided server.
     * No call to any methods of the logger will be made before this 
     * logger is initialized for some server.
     * @param server The server to which this logger should be initialized.
     */
    public abstract void initialize(httpd server);

    /**
     * Construct a new Logger instance.
     */
    public Logger() {
    }
}
