package org.w3c.www.protocol.http.micp;

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.EventObject;

/**
 * A real piece of fun, try it !
 */
class Stats implements MICP {

    int queries = 0;

    int hits = 0;

    String lasturl = null;

    final synchronized void handle(int op, String url) {
        if (op == MICP_OP_QUERY) queries++; else hits++;
        lasturl = url;
    }

    final synchronized int getQueries() {
        return queries;
    }

    final synchronized int getHits() {
        return hits;
    }

    final synchronized String getLastURL() {
        return lasturl;
    }
}

class MICPReader extends Thread {

    InetAddress addr = null;

    int port = -1;

    Stats stats = null;

    MulticastSocket socket = null;

    MICPReadWrite micprw = null;

    public void run() {
        byte buffer[] = new byte[4096];
        MICPMessage msg = new MICPMessage();
while (true) { 
{
            try {
                DatagramPacket p = new DatagramPacket(buffer, buffer.length);
                socket.receive(p);
                micprw.decode(p.getData(), p.getLength(), msg);
                stats.handle(msg.op, msg.url);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }} 

    }

    MICPReader(InetAddress a, int port, Stats stats) throws UnknownHostException, IOException {
        this.micprw = new MICPReadWrite();
        this.addr = a;
        this.port = port;
        this.stats = stats;
        this.socket = new MulticastSocket(port);
        this.socket.joinGroup(a);
        setName("mICP listener");
        start();
    }
}

public class Probe extends Panel implements Runnable, ActionListener {

    MICPReader reader = null;

    Stats stats = null;

    long refresh = 500;

    Label hits = null;

    Label queries = null;

    Label url = null;

    Button exit = null;

    /**
     * ActionListener implementation - exit on exit button.
     * @param e The event.
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == exit) {
            System.out.println("Bye !");
            System.exit(0);
        }
    }

    protected synchronized void tick() {
        try {
            wait(refresh);
        } catch (InterruptedException ex) {
        }
    }

    public void run() {
while (true) { 
{
            hits.setText(Integer.toString(stats.getHits()));
            queries.setText(Integer.toString(stats.getQueries()));
            url.setText(stats.getLastURL());
            tick();
        }} 

    }

    public Probe(InetAddress addr, int port, long refresh) throws UnknownHostException, IOException {
        this.refresh = refresh;
        this.stats = new Stats();
        this.reader = new MICPReader(addr, port, stats);
        GridBagLayout gb = new GridBagLayout();
        setLayout(gb);
        GridBagConstraints ct = new GridBagConstraints();
        GridBagConstraints cv = new GridBagConstraints();
        ct = new GridBagConstraints();
        ct.gridx = GridBagConstraints.RELATIVE;
        ct.anchor = GridBagConstraints.EAST;
        ct.weighty = 1.0;
        cv = new GridBagConstraints();
        cv.gridx = GridBagConstraints.RELATIVE;
        cv.gridwidth = GridBagConstraints.REMAINDER;
        cv.fill = GridBagConstraints.HORIZONTAL;
        cv.anchor = GridBagConstraints.WEST;
        cv.weightx = 1.0;
        cv.weighty = 1.0;
        Label title = new Label("queries");
        gb.setConstraints(title, ct);
        add(title);
        queries = new Label("0");
        queries.setBackground(Color.white);
        gb.setConstraints(queries, cv);
        add(queries);
        title = new Label("hits");
        gb.setConstraints(title, ct);
        add(title);
        hits = new Label("0");
        hits.setBackground(Color.white);
        gb.setConstraints(hits, cv);
        add(hits);
        title = new Label("url");
        gb.setConstraints(title, ct);
        add(title);
        url = new Label("0");
        url.setBackground(Color.white);
        gb.setConstraints(url, cv);
        add(url);
        exit = new Button("Exit");
        exit.addActionListener(this);
        gb.setConstraints(exit, ct);
        add(exit);
    }

    public static void usage() {
        PrintStream p = System.out;
        p.println("Probe -a <addr> -p <port> -r <refresh>" + " -w <width> -h <height>");
        p.println("\taddr: multicast group address");
        p.println("\tport: multicast port");
        p.println("\trefresh: refresh interval in ms");
        p.println("\twidth: width at startup (pixels)");
        p.println("\theight: height at startup (pixels)");
        System.exit(1);
    }

    public static void main(String args[]) {
        InetAddress addr = null;
        int port = -1;
        long refresh = 500;
        int width = 330;
        int height = 130;
        try {
            edu.hkust.clap.monitor.Monitor.loopBegin(397);
for (int i = 0; i < args.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(397);
{
                if (args[i].equals("-a") && (i + 1 < args.length)) {
                    addr = InetAddress.getByName(args[++i]);
                } else if (args[i].equals("-p") && (i + 1 < args.length)) {
                    port = Integer.parseInt(args[++i]);
                } else if (args[i].equals("-r") && (i + 1 < args.length)) {
                    refresh = Long.parseLong(args[++i]);
                } else if (args[i].equals("-w") && (i + 1 < args.length)) {
                    width = Integer.parseInt(args[++i]);
                } else if (args[i].equals("-h") && (i + 1 < args.length)) {
                    height = Integer.parseInt(args[++i]);
                } else {
                    usage();
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(397);

        } catch (Exception ex) {
            usage();
        }
        if ((addr == null) || (port == -1)) usage();
        Probe probe = null;
        try {
            probe = new Probe(addr, port, refresh);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        Frame toplevel = new Frame("mICP-Probe");
        toplevel.add("Center", probe);
        toplevel.setSize(new Dimension(width, height));
        toplevel.show();
        new Thread(probe).start();
    }
}
