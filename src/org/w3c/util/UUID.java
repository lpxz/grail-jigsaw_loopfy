package org.w3c.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.w3c.tools.crypt.Md5;

/**
 * A UUID (from java.rmi.server.UID)
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public final class UUID {

    /**
     * @serial Integer that helps create a unique UID.
     */
    private int unique;

    /**
     * @serial Long used to record the time.  The <code>time</code>
     * will be used to create a unique UID.
     */
    private long time;

    /**
     * InetAddress to make the UID globally unique
     */
    private static String address;

    /**
     * a random number
     */
    private static int hostUnique;

    /**
     * Used for synchronization
     */
    private static Object mutex;

    private static long lastTime;

    private static long DELAY;

    private static String generateNoNetworkID() {
        Thread current = Thread.currentThread();
        String nid = current.activeCount() + System.getProperty("os.version") + System.getProperty("user.name") + System.getProperty("java.version");
        System.out.println(nid);
        Md5 md5 = new Md5(nid);
        md5.processString();
        return md5.getStringDigest();
    }

    static {
        hostUnique = (new Object()).hashCode();
        mutex = new Object();
        lastTime = System.currentTimeMillis();
        DELAY = 10;
        try {
            String s = InetAddress.getLocalHost().getHostAddress();
            Md5 md5 = new Md5(s);
            md5.processString();
            address = md5.getStringDigest();
        } catch (UnknownHostException ex) {
            address = generateNoNetworkID();
        }
    }

    public UUID() {
        synchronized (mutex) {
            boolean done = false;
            edu.hkust.clap.monitor.Monitor.loopBegin(655);
while (!done) { 
edu.hkust.clap.monitor.Monitor.loopInc(655);
{
                time = System.currentTimeMillis();
                if (time < lastTime + DELAY) {
                    try {
                        Thread.currentThread().sleep(DELAY);
                    } catch (java.lang.InterruptedException e) {
                    }
                    continue;
                } else {
                    lastTime = time;
                    done = true;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(655);

            unique = hostUnique;
        }
    }

    public String toString() {
        return Integer.toString(unique, 16) + "-" + Long.toString(time, 16) + "-" + address;
    }

    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof UUID)) {
            UUID uuid = (UUID) obj;
            return (unique == uuid.unique && time == uuid.time && address.equals(uuid.address));
        } else {
            return false;
        }
    }

    public static void main(String args[]) {
        System.out.println(new UUID());
        System.out.println(new UUID());
        System.out.println(new UUID());
        System.out.println(new UUID());
    }
}
