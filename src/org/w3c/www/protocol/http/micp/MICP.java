package org.w3c.www.protocol.http.micp;

/**
 * MICP constants.
 */
public interface MICP {

    /**
     * MICP current version.
     */
    public static final byte MICP_VERSION = (byte) 1;

    /**
     * MIC opcodes - query for an URL.
     */
    public static final byte MICP_OP_QUERY = (byte) 1;

    /**
     * MICP opcode - reply to a query.
     */
    public static final byte MICP_OP_REPLY = (byte) 2;
}
