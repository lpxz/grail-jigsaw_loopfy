package org.w3c.www.http;

public interface HTTP {

    /**
     * The major version of HTTP handled by that package.
     */
    public static final int major_number = 1;

    /**
     * The miniro version of HTTP handled by that package.
     */
    public static final int minor_number = 1;

    /**
     * Some well know methods rfc2616 methods
     */
    public static final String GET = "GET".intern();

    public static final String HEAD = "HEAD".intern();

    public static final String POST = "POST".intern();

    public static final String PUT = "PUT".intern();

    public static final String DELETE = "DELETE".intern();

    public static final String OPTIONS = "OPTIONS".intern();

    public static final String TRACE = "TRACE".intern();

    public static final String CONNECT = "CONNECT".intern();

    public static final String HTTP_100_CONTINUE = "100-continue";

    /**
     * The version we emit with all replies. 
     * This version matches the version understood by the API, which does
     * not necessarily reflect what is returned by <code>getMajorVersion
     * </code> and <code>getMinorVersion</code>.
     */
    public static final byte byteArrayVersion[] = { (byte) 'H', (byte) 'T', (byte) 'T', (byte) 'P', (byte) '/', (byte) '1', (byte) '.', (byte) '1' };

    public static final String msg_100[] = { "Continue", "Switching Protocols" };

    public static final String msg_200[] = { "OK", "Created", "Accepted", "Non-Authoritative information", "No Content", "Reset Content", "Partial Content" };

    public static final String msg_300[] = { "Multiple Choices", "Moved Permanently", "Found", "See Other", "Not Modified", "Use Proxy", "", "Temporary Redirect" };

    public static final String msg_400[] = { "Bad Request", "Unauthorized", "Payment Required", "Forbidden", "Not Found", "Method Not Allowed", "Not Acceptable", "Proxy Authentication Required", "Request Timeout", "Conflict", "Gone", "Length Required", "Precondition Failed", "Request Entity Too Large", "Request-URI Too Long", "Unsupported Media Type", "Requested Range Not Satisfiable", "Expectation Failed" };

    public static final String msg_500[] = { "Internal Server Error", "Not Implemented", "Bad Gateway", "Service Unavailable", "Gateway Timeout", "HTTP Version Not Supported", "", "", "", "", "Not Extended" };

    public static final int CONTINUE = 100;

    public static final int SWITCHING = 101;

    public static final int OK = 200;

    public static final int CREATED = 201;

    public static final int ACCEPTED = 202;

    public static final int NON_AUTHORITATIVE_INFORMATION = 203;

    public static final int NO_CONTENT = 204;

    public static final int RESET_CONTENT = 205;

    public static final int PARTIAL_CONTENT = 206;

    public static final int MULTIPLE_CHOICE = 300;

    public static final int MOVED_PERMANENTLY = 301;

    public static final int FOUND = 302;

    public static final int SEE_OTHER = 303;

    public static final int NOT_MODIFIED = 304;

    public static final int USE_PROXY = 305;

    public static final int TEMPORARY_REDIRECT = 307;

    public static final int BAD_REQUEST = 400;

    public static final int UNAUTHORIZED = 401;

    public static final int PAYMENT_REQUIRED = 402;

    public static final int FORBIDDEN = 403;

    public static final int NOT_FOUND = 404;

    public static final int NOT_ALLOWED = 405;

    public static final int NOT_ACCEPTABLE = 406;

    public static final int PROXY_AUTH_REQUIRED = 407;

    public static final int REQUEST_TIMEOUT = 408;

    public static final int CONFLICT = 409;

    public static final int GONE = 410;

    public static final int LENGTH_REQUIRED = 411;

    public static final int PRECONDITION_FAILED = 412;

    public static final int REQUEST_ENTITY_TOO_LARGE = 413;

    public static final int REQUEST_URI_TOO_LONG = 414;

    public static final int UNSUPPORTED_MEDIA_TYPE = 415;

    public static final int REQUESTED_RANGE_NOT_SATISFIABLE = 416;

    public static final int EXPECTATION_FAILED = 417;

    public static final int INTERNAL_SERVER_ERROR = 500;

    public static final int NOT_IMPLEMENTED = 501;

    public static final int BAD_GATEWAY = 502;

    public static final int SERVICE_UNAVAILABLE = 503;

    public static final int GATEWAY_TIMEOUT = 504;

    public static final int HTTP_VERSION_NOT_SUPPORTED = 505;

    public static final int NOT_EXTENDED = 510;

    public static final int NOHEADER = 1000;

    public static final int DONE = 1001;
}
