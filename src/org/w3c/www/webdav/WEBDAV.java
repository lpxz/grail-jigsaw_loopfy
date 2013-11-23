package org.w3c.www.webdav;

import org.w3c.www.http.HTTP;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public interface WEBDAV {

    public static final String NAMESPACE_URI = "DAV:";

    public static final String NAMESPACE_PREFIX = "D";

    public static final String ENCODING = "utf-8";

    public static final String DAV_HEADER = "DAV";

    public static final String DEPTH_HEADER = "Depth";

    public static final String DESTINATION_HEADER = "Destination";

    public static final String IF_HEADER = "If";

    public static final String LOCK_TOKEN_HEADER = "Lock-Token";

    public static final String OVERWRITE_HEADER = "Overwrite";

    public static final String STATUS_URI_HEADER = "Status-URI";

    public static final String TIMEOUT_HEADER = "Timeout";

    public static final String CLASS_1_COMPLIANT = "1";

    public static final String CLASS_2_COMPLIANT = "1,2";

    public static final int DEPTH_0 = 0;

    public static final int DEPTH_1 = 1;

    public static final int DEPTH_INFINITY = -1;

    public static final String dav_msg_100[] = { "Continue", "Switching Protocols", "Processing" };

    public static final String dav_msg_200[] = { "OK", "Created", "Accepted", "Non-Authoritative information", "No Content", "Reset Content", "Partial Content", "Multi-Status" };

    public static final String dav_msg_300[] = HTTP.msg_300;

    public static final String dav_msg_400[] = { "Bad Request", "Unauthorized", "Payment Required", "Forbidden", "Not Found", "Method Not Allowed", "Not Acceptable", "Proxy Authentication Required", "Request Timeout", "Conflict", "Gone", "Length Required", "Precondition Failed", "Request Entity Too Large", "Request-URI Too Long", "Unsupported Media Type", "Requested Range Not Satisfiable", "Expectation Failed", "", "", "", "", "Unprocessable Entity", "Locked", "Failed Dependency" };

    public static final String dav_msg_500[] = { "Internal Server Error", "Not Implemented", "Bad Gateway", "Service Unavailable", "Gateway Timeout", "HTTP Version Not Supported", "", "Insufficient Storage", "", "", "Not Extended" };

    public static final int PROCESSING = 102;

    public static final int MULTI_STATUS = 207;

    public static final int UNPROCESSABLE_ENTITY = 422;

    public static final int LOCKED = 423;

    public static final int FAILED_DEPENDENCY = 424;

    public static final int INSUFFICIENT_STORAGE = 507;
}
