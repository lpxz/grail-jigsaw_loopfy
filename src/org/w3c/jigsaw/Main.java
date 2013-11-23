package org.w3c.jigsaw;

/**
 * A place holder for running Jigsaw.
 */
public class Main {

    public static void main(String args[]) {
// arg1: thread arg2: iteration count
        String[] newArgs = new String[args.length - 2];
        edu.hkust.clap.monitor.Monitor.loopBegin(979);
for (int i = 0; i < newArgs.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(979);
{
            newArgs[i] = args[i + 2];
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(979);

        org.w3c.jigsaw.daemon.ServerHandlerManager.main(newArgs);
    }
}
