package org.w3c.util;

/**
 * Native methods to do some UNIX specific system calls.
 * This class can be used on UNIX variants to access some specific system
 * calls.
 */
public class Unix {

    private static final String NATIVE_LIBRARY = "Unix";

    /**
     * Are the calls we support really availables ?
     */
    private static boolean haslibrary = false;

    private static Unix that = null;

    private native int libunix_getUID(String user);

    private native int libunix_getGID(String group);

    private native boolean libunix_setUID(int uid);

    private native boolean libunix_setGID(int gid);

    private native boolean libunix_chRoot(String root);

    /**
     * Get the UNIX system call manger.
     * @return An instance of this class, suitable to call UNIX system
     * calls.
     */
    public static synchronized Unix getUnix() {
        if (that == null) {
            try {
                System.loadLibrary(NATIVE_LIBRARY);
                haslibrary = true;
            } catch (UnsatisfiedLinkError ex) {
                haslibrary = false;
            } catch (RuntimeException ex) {
                haslibrary = false;
            }
            that = new Unix();
        }
        return (that);
    }

    /**
     * Can I perform UNIX system calls through that instance ?
     * @return A boolean, <strong>true</strong> if these system calls are
     * allowed, <strong>false</strong> otherwise.
     */
    public boolean isResolved() {
        return (haslibrary);
    }

    /**
     * Get the user identifier for that user.
     * @return The user's identifier, or <strong>-1</strong> if user was not
     * found.
     */
    public int getUID(String uname) {
        if (uname == null) return (-1);
        return (libunix_getUID(uname));
    }

    /**
     * Get the group identifier for that group.
     * @return The group identifier, or <strong>-1</strong> if not found.
     */
    public int getGID(String gname) {
        if (gname == null) return (-1);
        return (libunix_getGID(gname));
    }

    /**
     * Set the user id for the running process.
     * @param uid The new user identifier for the process.
     * @exception UnixException If failed.
     */
    public void setUID(int uid) throws UnixException {
        if (!libunix_setUID(uid)) throw new UnixException("setuid failed");
    }

    /**
     * Set the group id for the running process.
     * @param gid The new user identifier for the process.
     * @exception UnixException If failed.
     */
    public void setGID(int gid) throws UnixException {
        if (!libunix_setGID(gid)) throw new UnixException("setgid failed");
    }

    /**
     * Change the process root, using <code>chroot</code> system call.
     * @param root The new root for the process.
     * @exception UnixException If failed.
     */
    public void chroot(String root) throws UnixException {
        if (root == null) throw new NullPointerException("chroot: root == null");
        if (!libunix_chRoot(root)) throw new UnixException("chroot failed");
    }
}
