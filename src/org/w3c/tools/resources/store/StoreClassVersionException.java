package org.w3c.tools.resources.store;

/**
 * This is exception gets thrown if an invalid resource store is detected.
 */
public class StoreClassVersionException extends Exception {

    /**
     * Version of the class that wanted to load the store.
     */
    int loader = -1;

    /**
     * Version of the class that saved the store.
     */
    int saver = -1;

    /**
     * Get the loader's class version.
     * @return An integer version number.
     */
    public int getLoaderClassVersion() {
        return loader;
    }

    /**
     * Get the saver's class version.
     * @return An integer version number.
     */
    public int getSaverClassVersion() {
        return saver;
    }

    public StoreClassVersionException(int loader, int saver) {
        super("invalid store class " + loader + " saved by " + saver);
        this.loader = loader;
        this.saver = saver;
    }
}
