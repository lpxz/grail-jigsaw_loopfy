package org.w3c.jigadm;

/**
 * a place holder for running the administration tool
 */
public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            String[] arg = { "http://localhost:8009/" };
            org.w3c.jigadm.gui.ServerBrowser.main(arg);
        } else {
            org.w3c.jigadm.gui.ServerBrowser.main(args);
        }
    }
}
