package org.w3c.jigadmin;

/**
 * a place holder for running the administration tool
 */
public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            String[] arg = { "http://localhost:8009/" };
            org.w3c.jigadmin.gui.ServerBrowser.main(arg);
        } else {
            org.w3c.jigadmin.gui.ServerBrowser.main(args);
        }
    }
}
