package org.w3c.jigsaw.http;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;

/**
 * This describes the message structure that we exchange with the shuffler.
 * The equivalent C structure is defined in ShufflerProtocol.h
 */
class ShuffleMessage {

    byte op;

    int id;

    int length;
}

/**
 * Manage the queue of pending shuffler requests.
 * This object manages the queue of pending shuffler requests. It life goes
 * like this: wait for something to be put on the queue, once the queue is
 * not empty, send some request to the underlying <em>shuffler</em> process
 * and try getting <strong>DONE</strong> messages from the shuffler.
 * For each message got, find back the appropriate handler and terminate it.
 */
class ShufflerThread extends Thread {

    private static final boolean debug = true;

    Shuffler s;

    Vector q;

    /**
     * Add the given handler in our wait queue.
     * @param h The handler to wait for.
     */
    synchronized void registerHandler(ShuffleHandler h) {
        q.addElement(h);
        notifyAll();
    }

    /**
     * Process the given shuffler process message.
     * This method can emit a RuntimeException if some internal state becomes
     * inconsistent. This is typically the case if we can find back a request
     * from the queue.
     * @param msg The (process) shuffler message to handle.
     */
    synchronized void processMessage(ShuffleMessage msg) {
        int id = msg.id;
        edu.hkust.clap.monitor.Monitor.loopBegin(369);
for (int i = 0; i < q.size(); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(369);
{
            ShuffleHandler h = (ShuffleHandler) q.elementAt(i);
            if (h.id == id) {
                q.removeElementAt(i);
                h.done(msg.length);
                return;
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(369);

        edu.hkust.clap.monitor.Monitor.loopBegin(370);
for (int i = 0; i < q.size(); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(370);
System.out.println("waiting for : " + q.elementAt(i));} 
edu.hkust.clap.monitor.Monitor.loopEnd(370);

        throw new RuntimeException(this.getClass().getName() + ": received unexpected id " + id);
    }

    /**
     * Block the thread until we get some pending shuffles to wait for.
     */
    synchronized void waitForHandlers() {
        edu.hkust.clap.monitor.Monitor.loopBegin(371);
while (q.size() == 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(371);
{
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(371);

    }

    public void run() {
while (true) { 
{
            waitForHandlers();
            ShuffleMessage msg = s.getNextMessage();
            processMessage(msg);
        }} 

    }

    ShufflerThread(Shuffler s) {
        this.s = s;
        this.q = new Vector();
        setPriority(9);
        setName("ShufflerThread");
        setDaemon(true);
    }
}

/**
 * Objects describing pending shuffle requests.
 */
class ShuffleHandler {

    FileDescriptor in = null;

    FileDescriptor out = null;

    boolean doneflag = false;

    int id = -1;

    int length = -1;

    /**
     * Notify that this shuffle handle is now completed.
     */
    synchronized void done(int length) {
        this.length = length;
        this.doneflag = true;
        notifyAll();
    }

    /**
     * Wait for this shuffle completion.
     * This method blocks the calling thread until the shuffle is completed.
     */
    synchronized int waitForCompletion() {
        edu.hkust.clap.monitor.Monitor.loopBegin(373);
while (!doneflag) { 
edu.hkust.clap.monitor.Monitor.loopInc(373);
{
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(373);

        return length;
    }

    /**
     * Print a ShuffleHandler (for debugging).
     */
    public String toString() {
        return id + " " + doneflag;
    }

    ShuffleHandler(FileDescriptor in, FileDescriptor out) {
        this.in = in;
        this.out = out;
    }
}

/**
 * This class implements both a nice hack and some magic.
 * It uses an underlying <em>shuffler</em> process to speed up the sending
 * of big data files back to the client.
 * <p>The protocol between the server and the shuffler is quite simple, one
 * byte indicate the operation, which takes as argument two file descriptors.
 */
public class Shuffler {

    /**
     * The property giving the path of the shuffler server.
     * The shuffler server is an optional server helper, that deals with
     * serving resource contents. When resource contents can be efficiently
     * messaged between process boundaries (eg using sendmsg), the shuffler
     * server takes over the task of sending resource's content back to the 
     * client. This property gives the path of the shuffler server binary 
     * program.
     */
    public static final String SHUFFLER_P = "org.w3c.jigsaw.shuffler";

    private static Process shuffler = null;

    private static boolean inited = false;

    private ShufflerThread waiter = null;

    private int fd = -1;

    private native int initialize(String path);

    private native synchronized int shuffle(ShuffleHandler h);

    private native int getNextMessage(ShuffleMessage msg);

    ShuffleMessage getNextMessage() {
        ShuffleMessage msg = new ShuffleMessage();
        int duration = 2;
        edu.hkust.clap.monitor.Monitor.loopBegin(374);
while (true) { 
edu.hkust.clap.monitor.Monitor.loopInc(374);
{
            int ecode = getNextMessage(msg);
            if (duration < 250) duration = (duration << 1);
            if (ecode > 0) {
            	edu.hkust.clap.monitor.Monitor.loopEnd(374);
                return msg;
            } else if (ecode == 0) {
                try {
                    Thread.sleep(duration);
                } catch (InterruptedException e) {
                }
                Thread.yield();
            } else if (ecode < 0) {
                String m = (this.getClass().getName() + "[getNextMessage]: failed (e=" + ecode + ")");
                edu.hkust.clap.monitor.Monitor.loopEnd(374);
                throw new RuntimeException(m);
            }
        }} 


    }

    /**
     * Initialize this class.
     * This deserve a special method, since we want any exception to be 
     * caught when invoking theinstance constructor.
     * <p>This method tries to launch the shuffler process automatically.
     * @param path The driectory for UNIX socket bindings.
     * @return A boolean <strong>true</strong> is every thing went fine, 
     *    <strong>false</strong> otherwise.
     */
    private synchronized boolean classInitialize(String path) {
        File socket = new File(path + "/shuffler");
        socket.delete();
        Runtime.getRuntime().loadLibrary("Shuffle");
        inited = true;
        String shuffler_bin = System.getProperty(SHUFFLER_P);
        if (shuffler_bin == null) return false;
        try {
            String args[] = new String[2];
            args[0] = shuffler_bin;
            args[1] = path + "/shuffler";
            shuffler = Runtime.getRuntime().exec(args);
        } catch (Exception e) {
            throw new RuntimeException(this.getClass().getName() + "[classInitialize]: " + "unable to launch shuffler.");
        }
        int timeout = 10000;
        edu.hkust.clap.monitor.Monitor.loopBegin(375);
while ((timeout > 0) && (!socket.exists())) { 
edu.hkust.clap.monitor.Monitor.loopInc(375);
{
            timeout -= 500;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(375);

        if (!socket.exists()) {
            throw new RuntimeException(this.getClass().getName() + "[classInitialize]: " + " didn't create its socket.");
        }
        return true;
    }

    private int shuffle(FileDescriptor in, FileDescriptor out) throws IOException {
        ShuffleHandler handle = new ShuffleHandler(in, out);
        synchronized (waiter) {
            if ((handle.id = shuffle(handle)) < 0) throw new IOException(this.getClass().getName() + " unable to shuffle !");
            waiter.registerHandler(handle);
        }
        return handle.waitForCompletion();
    }

    /**
     * Shuffle the given rteply body to the given client.
     * This methods tries to outout the given reply 
     */
    public int shuffle(Client client, Reply reply) throws IOException {
        FileDescriptor in = reply.getInputFileDescriptor();
        if (in == null) return -1;
        return -1;
    }

    public synchronized void shutdown() {
        if (shuffler != null) {
            shuffler.destroy();
            edu.hkust.clap.monitor.Monitor.loopBegin(376);
while (true) { 
edu.hkust.clap.monitor.Monitor.loopInc(376);
{
                try {
                    shuffler.waitFor();
                    break;
                } catch (InterruptedException ex) {
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(376);

            shuffler = null;
        }
        inited = false;
        waiter.stop();
        waiter = null;
    }

    /**
     * Create a new data shuffler.
     * The path identifies the directory in which UNIX socket will get bind.
     * This should be an absloute path, eg <code>/tmp/shuffler</code>.
     * @param path The path to the server.
     */
    public Shuffler(String path) {
        if (((!inited) && (!classInitialize(path))) || (initialize(path) < 0)) {
            throw new RuntimeException(this.getClass().getName() + ": unable to connect to shuffler " + path);
        }
        this.waiter = new ShufflerThread(this);
        this.waiter.start();
    }

    public static void main(String args[]) throws FileNotFoundException, IOException {
        Shuffler s = new Shuffler(args[0]);
        FileInputStream f = new FileInputStream("from");
        FileOutputStream t = new FileOutputStream("to");
        s.shuffle(f.getFD(), t.getFD());
        f.close();
        t.close();
    }
}
