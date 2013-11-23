package org.w3c.www.mux.tests;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.util.Vector;
import org.w3c.www.mux.MuxSession;
import org.w3c.www.mux.MuxStream;

class MuxFetcher extends Thread {

    MuxSession ses = null;

    DataOutputStream out = null;

    InputStream in = null;

    URL u = null;

    OutputStream dst = null;

    public void request(URL u) throws IOException {
        out.writeBytes("GET " + u + " HTTP/1.1\r\n");
        out.writeBytes("Connection: close\r\n\r\n");
        out.flush();
    }

    public void copy(URL u, OutputStream dst) throws IOException {
        request(u);
        byte buffer[] = new byte[1024];
        int got = -1;
        edu.hkust.clap.monitor.Monitor.loopBegin(664);
while ((got = in.read(buffer, 0, buffer.length)) > 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(664);
dst.write(buffer, 0, got);} 
edu.hkust.clap.monitor.Monitor.loopEnd(664);

    }

    public void dump(URL u) throws IOException {
        request(u);
        byte buffer[] = new byte[1024];
        int got = -1;
        edu.hkust.clap.monitor.Monitor.loopBegin(665);
while ((got = in.read(buffer, 0, buffer.length)) > 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(665);
System.out.print(new String(buffer, 0, 0, got));} 
edu.hkust.clap.monitor.Monitor.loopEnd(665);

        System.out.println("Dumped all available data !");
    }

    public void run() {
        try {
            out = (new DataOutputStream(new BufferedOutputStream(ses.getOutputStream())));
            in = ses.getInputStream();
            if (dst != null) copy(u, dst); else dump(u);
            ses.shutdown();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    MuxFetcher(MuxStream stream, URL u) throws IOException {
        this(stream, u, null);
    }

    MuxFetcher(MuxStream stream, URL u, OutputStream dst) throws IOException {
        this.ses = stream.connect(80);
        this.dst = dst;
        this.u = u;
        start();
    }
}

public class HttpClient {

    public static MuxStream stream = null;

    public static void main(String args[]) {
        String host = "www43.inria.fr";
        int port = 8007;
        Vector urls = new Vector();
        edu.hkust.clap.monitor.Monitor.loopBegin(666);
for (int i = 0; i < args.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(666);
{
            if (args[i].equals("-h")) {
                host = args[++i];
            } else if (args[i].equals("-p")) {
                try {
                    port = Integer.parseInt(args[++i]);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                urls.addElement(args[i]);
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(666);

        try {
            Socket socket = new Socket(host, port);
            MuxStream stream = new MuxStream(false, null, socket);
            MuxFetcher fetchers[] = new MuxFetcher[urls.size()];
            edu.hkust.clap.monitor.Monitor.loopBegin(667);
for (int i = 0; i < urls.size(); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(667);
{
                URL u = new URL((String) urls.elementAt(i));
                fetchers[i] = new MuxFetcher(stream, u);
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(667);

            edu.hkust.clap.monitor.Monitor.loopBegin(668);
for (int i = 0; i < fetchers.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(668);
{
                fetchers[i].join();
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(668);

            stream.shutdown(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
