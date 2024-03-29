package org.w3c.www.mux.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import org.w3c.www.mux.MuxSession;
import org.w3c.www.mux.MuxStream;
import org.w3c.www.mux.MuxStreamHandler;

class EchoServerStreamHandler implements MuxStreamHandler {

    public boolean acceptSession(MuxStream stream, int sessid, int protid) {
        System.out.println("EchoServerStreamHandler: accept " + sessid + " for protocol " + protid);
        return true;
    }

    public void notifySession(MuxSession session) {
        try {
            new EchoServerHandler(session);
        } catch (Exception ex) {
            System.out.println("EchoServerStreamHandler: error !");
            ex.printStackTrace();
        }
    }
}

class EchoServerHandler extends Thread {

    MuxSession ses = null;

    InputStream in = null;

    OutputStream out = null;

    public void run() {
        byte buffer[] = new byte[1024];
        int got = -1;
        System.out.println(this + ": now running !");
        try {
            edu.hkust.clap.monitor.Monitor.loopBegin(265);
while ((got = in.read(buffer, 0, buffer.length)) >= 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(265);
{
                System.out.println("< " + this + " [" + new String(buffer, 0, 0, got) + "]");
                out.write(buffer, 0, got);
                out.flush();
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(265);

        } catch (Exception ex) {
            System.out.println(this + ": fatal error, aborting.");
            ex.printStackTrace();
        }
    }

    public EchoServerHandler(MuxSession ses) throws IOException {
        this.ses = ses;
        this.in = ses.getInputStream();
        this.out = ses.getOutputStream();
        setName("Echo@" + ses.getIdentifier());
        start();
    }
}

class EchoServer extends Thread {

    ServerSocket socket = null;

    EchoServerStreamHandler handler = null;

    int port = -1;

    public void run() {
        try {
while (true) { 
{
                System.out.println("EchoServer@localhost:" + port + ": accept.");
                Socket client = socket.accept();
                new MuxStream(true, handler, client);
            }} 

        } catch (Exception ex) {
            System.out.println("EchoServer: fatal error !");
            ex.printStackTrace();
        }
    }

    EchoServer(int port) throws IOException {
        this.handler = new EchoServerStreamHandler();
        this.socket = new ServerSocket(port);
        this.port = port;
        this.start();
    }
}

class EchoClient extends Thread {

    static Socket socket = null;

    static MuxStream stream = null;

    String msg = null;

    InputStream in = null;

    OutputStream out = null;

    MuxSession ses = null;

    int id = -1;

    int repeat = -1;

    public void run() {
        byte buffer[] = new byte[1024];
        int msgid = 0;
        try {
            edu.hkust.clap.monitor.Monitor.loopBegin(267);
while ((repeat < 0) || (repeat != 0)) { 
edu.hkust.clap.monitor.Monitor.loopInc(267);
{
                String m = "[" + id + "]" + (msgid++) + "/" + msg;
                m.getBytes(0, m.length(), buffer, 0);
                out.write(buffer, 0, m.length());
                out.flush();
                int got = in.read(buffer, 0, buffer.length);
                System.out.println("> " + this + " [" + new String(buffer, 0, 0, got) + "]");
                if (repeat > 0) repeat--;
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(267);

            out.close();
        } catch (Exception ex) {
            System.out.println(this + ": failed.");
            ex.printStackTrace();
        }
    }

    public static void makeClients(String host, int port, int count, int repeat, String msg) throws IOException {
        if (socket == null) {
            socket = new Socket(host, port);
            stream = new MuxStream(false, null, socket);
        }
        edu.hkust.clap.monitor.Monitor.loopBegin(268);
while (--count >= 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(268);
new EchoClient(stream, count, repeat, msg);} 
edu.hkust.clap.monitor.Monitor.loopEnd(268);

    }

    EchoClient(MuxStream stream, int id, int repeat, String msg) throws IOException {
        this.ses = stream.connect(7);
        this.in = ses.getInputStream();
        this.out = ses.getOutputStream();
        this.msg = msg;
        this.id = id;
        this.repeat = repeat;
        setName("client/" + id);
        start();
    }
}

public class MuxEcho {

    public static void usage() {
        System.out.println("-s (server) -p port");
        System.out.println("-c (client) -m msg -h server -p port -n count -r repeat");
    }

    public static void main(String args[]) {
        boolean server = false;
        String host = null;
        String msg = null;
        int port = 5001;
        int count = 2;
        int repeat = 0;
        edu.hkust.clap.monitor.Monitor.loopBegin(269);
for (int i = 0; i < args.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(269);
{
            if (args[i].equals("-s")) {
                server = true;
            } else if (args[i].equals("-c")) {
                server = false;
            } else if (args[i].equals("-m")) {
                msg = args[++i];
            } else if (args[i].equals("-h")) {
                host = args[++i];
            } else if (args[i].equals("-p")) {
                try {
                    port = Integer.parseInt(args[++i]);
                } catch (Exception ex) {
                    usage();
                }
            } else if (args[i].equals("-n")) {
                try {
                    count = Integer.parseInt(args[++i]);
                } catch (Exception ex) {
                    usage();
                }
            } else if (args[i].equals("-r")) {
                try {
                    repeat = Integer.parseInt(args[++i]);
                } catch (Exception ex) {
                    usage();
                }
            } else {
                usage();
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(269);

        try {
            if (server) {
                new EchoServer(port);
            } else {
                EchoClient.makeClients(host, port, count, repeat, msg);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
