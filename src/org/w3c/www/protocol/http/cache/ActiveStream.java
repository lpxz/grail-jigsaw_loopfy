package org.w3c.www.protocol.http.cache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.w3c.util.ThreadCache;
import org.w3c.www.protocol.http.Request;
import org.w3c.www.protocol.http.Reply;

class ActiveInputStream extends InputStream {

    private static final int buflen = 2048;

    private static final int halflen = (buflen / 2);

    byte buffer[] = new byte[2048];

    int off = 0;

    int len = 0;

    boolean closed = false;

    boolean interrupted = false;

    private synchronized void waitForInput() throws IOException {
while (true) { 
{
            if (interrupted) throw new IOException("Broken active pipe.");
            int avail = len - off;
            if (closed || (avail > 0)) return;
            try {
                wait();
            } catch (InterruptedException ex) {
            }
        }} 

    }

    /**
     * We wait for half buffer size to be available before pushing data.
     * This is to prevent a silly window syndrom problem, where the pusher
     * and the puller would exachnge single bytes of data.
     */
    public synchronized void receive(byte buf[], int boff, int blen) throws IOException {
        edu.hkust.clap.monitor.Monitor.loopBegin(827);
while (boff < blen) { 
edu.hkust.clap.monitor.Monitor.loopInc(827);
{
            if (closed) throw new IOException("Write to closed stream.");
            int space = buffer.length - len;
            if ((space >= (blen - boff)) || (space > halflen)) {
                int push = Math.min(blen - boff, space);
                System.arraycopy(buf, boff, buffer, len, push);
                len += push;
                boff += push;
                notifyAll();
            } else {
                try {
                    wait();
                } catch (InterruptedException ex) {
                }
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(827);

    }

    public synchronized void close() {
        closed = true;
        notifyAll();
    }

    public synchronized void interrupt() {
        interrupted = true;
        closed = true;
        notifyAll();
    }

    public synchronized int read() throws IOException {
        waitForInput();
        if (closed && ((len - off) == 0)) return -1;
        int b = (buffer[off++] & 0xff);
        if (off >= len) {
            off = 0;
            len = 0;
            notifyAll();
        }
        return b;
    }

    public synchronized int read(byte to[], int toff, int tlen) throws IOException {
        waitForInput();
        int avail = len - off;
        if (closed && (avail == 0)) {
            return -1;
        }
        if (tlen >= avail) {
            int snd = avail;
            System.arraycopy(buffer, off, to, toff, avail);
            off = 0;
            len = 0;
            notifyAll();
            return snd;
        } else {
            System.arraycopy(buffer, off, to, toff, tlen);
            if ((off += tlen) > halflen) {
                System.arraycopy(buffer, off, buffer, 0, len - off);
                len -= off;
                off = 0;
                notifyAll();
            }
            return tlen;
        }
    }

    public synchronized int available() {
        return (closed || interrupted) ? -1 : len - off;
    }
}

/**
 * ActiveStream is used to tee a stream to the client, while caching it.
 * This class basically mimics the piped streams provided in the java library
 * in a more efficient manner (well, sort of).
 * <p>If any error occurs while writing data back to the client, then the
 * active thread finishes it works, but only streaming data into the sink,
 */
public class ActiveStream implements Runnable {

    private static ThreadCache threadcache = null;

    ActiveInputStream pout = null;

    boolean poutClosed = false;

    InputStream src = null;

    boolean srcClosed = false;

    OutputStream dst = null;

    boolean dstClosed = false;

    TeeMonitor monitor = null;

    public void run() {
        byte buffer[] = new byte[2048];
        int chunksz = 256;
        boolean notified = false;
        int total = 0;
        try {
            int count = 0;
            edu.hkust.clap.monitor.Monitor.loopBegin(828);
while ((count = src.read(buffer, 0, chunksz)) >= 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(828);
{
                if (!poutClosed) {
                    try {
                        pout.receive(buffer, 0, count);
                    } catch (IOException ex) {
                        try {
                            pout.close();
                        } catch (Exception e) {
                        }
                        poutClosed = true;
                    }
                }
                dst.write(buffer, 0, count);
                total += count;
                chunksz = Math.min(buffer.length, (chunksz * 2));
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(828);

            src.close();
            srcClosed = true;
            dst.close();
            dstClosed = true;
            if (!poutClosed) {
                pout.close();
                poutClosed = true;
            }
            monitor.notifyTeeSuccess(total);
            notified = true;
        } catch (IOException ex) {
            ex.printStackTrace();
            try {
                monitor.notifyTeeFailure(total);
            } catch (Exception nex) {
            }
            notified = true;
        } finally {
            if (!srcClosed) {
                try {
                    src.close();
                } catch (Exception ex) {
                }
                srcClosed = true;
            }
            if (!dstClosed) {
                try {
                    dst.close();
                } catch (Exception ex) {
                }
                dstClosed = true;
            }
            if (!poutClosed) {
                try {
                    pout.interrupt();
                } catch (Exception ex) {
                }
                poutClosed = true;
            }
            if (!notified) monitor.notifyTeeFailure(total);
        }
    }

    public static InputStream createTee(TeeMonitor monitor, InputStream src, OutputStream dst) {
        ActiveStream tee = new ActiveStream();
        tee.monitor = monitor;
        tee.pout = new ActiveInputStream();
        tee.src = src;
        tee.dst = dst;
        if (!threadcache.getThread(tee, false)) {
            return null;
        } else {
            return tee.pout;
        }
    }

    public static synchronized void initialize() {
        if (threadcache == null) {
            threadcache = new ThreadCache("active-streams");
            threadcache.setCachesize(10);
            threadcache.initialize();
        }
    }

    ActiveStream() {
    }
}
