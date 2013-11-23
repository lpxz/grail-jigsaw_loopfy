package org.w3c.www.protocol.http.icp;

/**
 * Internet Cache Protocol well known constants.
 * ICP is a light-weight proxy to proxy protocol (FIXME, url)
 */
public interface ICP {

    /**
     * ICP constants - The major version of ICP we speak.
     */
    public static final int ICP_VERSION = 3;

    /**
     * ICP constants - Invalid payload (must not be sent).
     */
    public static final int ICP_OP_INVALID = 0;

    public static final int ICP_OP_QUERY = 1;

    public static final int ICP_OP_HIT = 2;

    public static final int ICP_OP_MISS = 3;

    public static final int ICP_OP_ERR = 4;

    public static final int ICP_OP_SEND = 5;

    public static final int ICP_OP_SENDA = 6;

    public static final int ICP_OP_DATABEG = 7;

    public static final int ICP_OP_DATA = 8;

    public static final int ICP_OP_DATAEND = 9;

    public static final int ICP_OP_SECHO = 10;

    public static final int ICP_OP_DECHO = 11;

    public static final int ICP_OP_RELOADING = 21;

    public static final int ICP_OP_DENIED = 22;

    public static final int ICP_OP_HIT_OBJ = 23;

    public static final int ICP_FLAG_HIT_OBJ = 0x80000000;
}
