package driver;

import java.util.concurrent.Semaphore;

public class JigsawDriver {

    public static void main(final String[] args) {
        final String[] arg = args;
        try {
            Thread t1 = new Thread() {

                public void run() {
                    org.w3c.jigsaw.Main.main(args);
                }
            };
            Thread t2 = new Thread() {

                public void run() {
                    JigsawHarnessPretex.main(args);
                }
            };
            t1.start();
            t2.start();
            Thread.sleep(10000);
        } catch (Exception e) {
        }
    }
}
