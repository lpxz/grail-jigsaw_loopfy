package org.w3c.tools.resources;

/**
 * The resource store is unable to restore a resource.
 */
public class MultipleLockException extends Exception {

    int nb = 0;

    public int getNbLock() {
        return nb;
    }

    public MultipleLockException(int nb, Resource resource, String msg) {
        super(nb + " locks on " + resource.getIdentifier() + " " + msg);
        this.nb = nb;
    }
}
