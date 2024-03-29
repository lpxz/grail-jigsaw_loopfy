package org.w3c.tools.log;

import java.util.Date;
import java.util.Hashtable;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.w3c.util.ThreadCache;

public class DNSResolver {

    /**
     * the thread in charge of doing DNS resolution
     * It works better if the getHostName() call is not blocking
     * the whole JVM :)
     */
    private class ResolverThread implements Runnable {

        String line;

        DateFormat dfp;

        public void run() {
            boolean ok;
            String host;
            DNSEntry entry;
            String res;
            String ip_str = line.substring(0, line.indexOf(' '));
            byte a[] = ip_str.getBytes();
            if (resolve) {
                ok = true;
                edu.hkust.clap.monitor.Monitor.loopBegin(57);
for (int i = 0; ok && i < a.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(57);
{
                    if (a[i] == '.') continue;
                    if (a[i] < '0' || a[i] > '9') ok = false;
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(57);

            } else ok = false;
            if (!ok) {
                try {
                    if (dfp != null) {
                        String date_str = line.substring(line.indexOf('[') + 1, line.indexOf(']'));
                        long stamp = 0;
                        try {
                            stamp = dfp.parse(date_str).getTime();
                        } catch (Exception ex) {
                        }
                        synchronized (System.out) {
                            System.out.println(Long.toString(stamp) + " " + line);
                        }
                    } else {
                        synchronized (System.out) {
                            System.out.println(line);
                        }
                    }
                } catch (Exception parsex) {
                }
                return;
            }
            host = (String) badHosts.get(ip_str);
            if (host == null) {
                entry = (DNSEntry) hosts.get(ip_str);
                if (entry == null || !entry.isResolved()) {
                    try {
                        host = InetAddress.getByName(ip_str).getHostName();
                        if (host.equals(ip_str)) {
                            badHosts.put(ip_str, ip_str);
                            if (entry != null) entry.notFound(); else {
                                entry = new DNSEntry(ip_str, false);
                                hosts.put(ip_str, entry);
                            }
                        } else {
                            if (entry != null) entry.setHost(host); else {
                                entry = new DNSEntry(host);
                                hosts.put(ip_str, entry);
                            }
                        }
                    } catch (UnknownHostException uhe) {
                        host = ip_str;
                        badHosts.put(ip_str, ip_str);
                        if (entry != null) entry.notFound(); else {
                            entry = new DNSEntry(ip_str, false);
                            hosts.put(ip_str, entry);
                        }
                    }
                } else host = entry.host;
            }
            res = line.substring(line.indexOf(' '));
            if (dfp != null) {
                String date_str = res.substring(res.indexOf('[') + 1, res.indexOf(']'));
                long stamp = 0;
                try {
                    stamp = dfp.parse(date_str).getTime();
                } catch (Exception ex) {
                }
                synchronized (System.out) {
                    System.out.println(Long.toString(stamp) + " " + host + res);
                }
            } else {
                synchronized (System.out) {
                    System.out.println(host + res);
                }
            }
        }

        /**
	 * create a new resolver thread, with the full ECLF entry
	 */
        ResolverThread(String line, boolean timestamp) {
            this.line = line;
            dfp = (timestamp) ? new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss z") : null;
        }
    }

    private BufferedReader bf;

    private Hashtable hosts;

    private Hashtable badHosts;

    private ThreadCache threadCache;

    private boolean timestamp;

    private boolean resolve;

    private int cacheSize;

    /**
     * the main loop, works on the reader provided at initialization
     */
    public void readLog() {
        String read;
        boolean ok, done;
        int pos, qpos;
        String host;
        String pass_date;
        String request;
        int resp_code;
        int resp_size;
        String referer = null;
        String user_agent = null;
        String res;
        String tmp;
        DNSEntry entry;
        try {
            edu.hkust.clap.monitor.Monitor.loopBegin(58);
while ((read = bf.readLine()) != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(58);
{
                done = false;
                if (read.length() < 40) {
                    continue;
                }
                ResolverThread rt = new ResolverThread(read, timestamp);
                if (!threadCache.getThread(rt, true)) {
                    System.err.println("*** unable to process :" + read);
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(58);

            threadCache.waitForCompletion();
            FileOutputStream fileOut = null;
            try {
                fileOut = new FileOutputStream("dns.oj");
                ObjectOutputStream out = null;
                try {
                    out = new ObjectOutputStream(fileOut);
                    out.writeObject(hosts);
                } catch (Exception e) {
                } finally {
                    try {
                        out.close();
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {
            } finally {
                try {
                    fileOut.close();
                } catch (Exception e) {
                }
            }
        } catch (IOException ex) {
        }
    }

    /**
     * create a new Resovler engine
     * @param bf, a buffered reader, the log source
     * @param cacheSize the number of threads used to do resolution
     * @param timestamp if set, the resolver will add a numeric timestamp
     *                  useful to sort mixed entries
     * @param resolve if set, it will do DNS resolution of the entry
     */
    public DNSResolver(BufferedReader bf, int cacheSize, boolean timestamp, boolean resolve) {
        this.bf = bf;
        this.resolve = resolve;
        this.badHosts = new Hashtable(201);
        this.timestamp = timestamp;
        this.cacheSize = cacheSize;
        FileInputStream fileIn = null;
        try {
            fileIn = new FileInputStream("dns.oj");
            ObjectInputStream in = null;
            try {
                in = new ObjectInputStream(fileIn);
                hosts = (Hashtable) in.readObject();
            } catch (Exception e) {
                hosts = new Hashtable(1001);
            } finally {
                try {
                    in.close();
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            hosts = new Hashtable(1001);
        } finally {
            try {
                fileIn.close();
            } catch (Exception e) {
            }
        }
        threadCache = new ThreadCache("resolver");
        threadCache.setThreadPriority(5);
        threadCache.setCachesize(cacheSize);
        threadCache.initialize();
    }

    public DNSResolver(BufferedReader bf, int cacheSize) {
        this(bf, cacheSize, false, true);
    }

    public DNSResolver(BufferedReader bf, boolean timestamp, boolean resolve) {
        this(bf, 50, timestamp, resolve);
    }

    public DNSResolver(BufferedReader bf) {
        this(bf, 50, false, true);
    }
}
