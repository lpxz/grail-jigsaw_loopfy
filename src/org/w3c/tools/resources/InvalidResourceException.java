package org.w3c.tools.resources;

/**
 * The resource is no more a valide resource.
 */
public class InvalidResourceException extends Exception {

    public InvalidResourceException(String id, String msg) {
        super("[" + id + "] loadResource failed: " + msg);
    }

    public InvalidResourceException(String parent, String child, String msg) {
        super("[" + parent + " , " + child + "] registerResource failed: " + msg);
    }
}
