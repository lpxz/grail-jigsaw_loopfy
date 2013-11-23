package org.w3c.jigsaw.https.socket;

import java.security.KeyStore;
import org.w3c.jigsaw.config.PropertySet;
import org.w3c.jigsaw.http.httpd;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.FileAttribute;
import org.w3c.tools.resources.StringAttribute;

/**
 * @author Thomas Kopp, Dialogika GmbH
 * @version 1.1, 27 December 2000, 6 February 2004
 * 
 * This class supplies a management resource for SSL properties
 */
public class SSLProperties extends PropertySet {

    /**
     * property key for specifying the TLS security provider
     */
    public static final String SECURITY_PROVIDER_P = "org.w3c.jigsaw.ssl.security.provider";

    /**
     * property key for specifying the TLS protocol handler
     */
    public static final String PROTOCOL_HANDLER_P = "org.w3c.jigsaw.ssl.protocol.handler";

    /**
     * property key for specifying the TLS protocol name
     */
    public static final String PROTOCOL_NAME_P = "org.w3c.jigsaw.ssl.protocol.name";

    /**
     * property key for specifying the keymanager type
     */
    public static final String KEYMANAGER_TYPE_P = "org.w3c.jigsaw.ssl.keymanager.type";

    /**
     * property key for indicating a generic keystore, i.e.
     * a class implementing the key manager factory parameters interface
     */
    public static final String KEYSTORE_GENERIC_P = "org.w3c.jigsaw.ssl.keystore.generic";

    /**
     * property key for specifying the keystore type
     */
    public static final String KEYSTORE_TYPE_P = "org.w3c.jigsaw.ssl.keystore.type";

    /**
     * property key for specifying the keystore path
     */
    public static final String KEYSTORE_PATH_P = "org.w3c.jigsaw.ssl.keystore.path";

    /**
     * property key for specifying the keystore password
     */
    public static final String KEYSTORE_PASSWORD_P = "org.w3c.jigsaw.ssl.keystore.password";

    /**
     * property key for specifying the trustmanager type
     */
    public static final String TRUSTMANAGER_TYPE_P = "org.w3c.jigsaw.ssl.trustmanager.type";

    /**
     * property key for indicating a generic truststore, i.e.
     * a class implementing the key manager factory parameters interface
     */
    public static final String TRUSTSTORE_GENERIC_P = "org.w3c.jigsaw.ssl.truststore.generic";

    /**
     * property key for specifying the truststore type
     */
    public static final String TRUSTSTORE_TYPE_P = "org.w3c.jigsaw.ssl.truststore.type";

    /**
     * property key for specifying the truststore path 
     */
    public static final String TRUSTSTORE_PATH_P = "org.w3c.jigsaw.ssl.truststore.path";

    /**
     * property key for specifying the truststore password 
     */
    public static final String TRUSTSTORE_PASSWORD_P = "org.w3c.jigsaw.ssl.truststore.password";

    /**
     * property key for enabling mandatory TLS client authentication
     */
    public static final String MUST_AUTHENTICATE_P = "org.w3c.jigsaw.ssl.must.authenticate";

    /**
     * property key for enabling TLS support (attribute implicit via
     *                                        keystore configuration)
     */
    public static final String SSL_ENABLED_P = "org.w3c.jigsaw.ssl.enabled";

    /**
     * default security provider for TLS support 
     * (concerning pre JDK 1.4 api only)
     */
    public static final String DEFAULT_SECURITY_PROVIDER = "com.sun.net.ssl.internal.ssl.Provider";

    /**
     * default TLS protocol handler (concerning pre JDK 1.4 api only)
     */
    public static final String DEFAULT_PROTOCOL_HANDLER = "com.sun.net.ssl.internal.www.protocol";

    /**
     * default TLS protocol
     */
    public static final String DEFAULT_PROTOCOL_NAME = "TLS";

    /**
     * default TLS generic keystore default
     */
    public static final boolean DEFAULT_KEYSTORE_GENERIC = false;

    /**
     * default TLS generic truststore default
     */
    public static final boolean DEFAULT_TRUSTSTORE_GENERIC = false;

    /**
     * default TLS client mandatory authentication
     */
    public static final boolean DEFAULT_MUST_AUTHENTICATE = false;

    /**
     * default TLS support
     */
    public static final boolean DEFAULT_SSL_ENABLED = true;

    /**
     * default properties name
     */
    private static final String DEFAULT_NAME = "SSL";

    /**
     * protocol name attribute index
     */
    protected static int ATTR_SSL_PROTOCOL_NAME = -1;

    /**
     * keymanager type attribute index
     */
    protected static int ATTR_SSL_KEYMANAGER_TYPE = -1;

    /**
     * keystore generic attribute index
     */
    protected static int ATTR_SSL_KEYSTORE_GENERIC = -1;

    /**
     * keystore type attribute index
     */
    protected static int ATTR_SSL_KEYSTORE_TYPE = -1;

    /**
     * keystore path attribute index
     */
    protected static int ATTR_SSL_KEYSTORE_PATH = -1;

    /**
     * keystore password attribute index
     */
    protected static int ATTR_SSL_KEYSTORE_PASSWORD = -1;

    /**
     * trustmanager type attribute index
     */
    protected static int ATTR_SSL_TRUSTMANAGER_TYPE = -1;

    /**
     * truststore generic attribute index
     */
    protected static int ATTR_SSL_TRUSTSTORE_GENERIC = -1;

    /**
     * truststore type attribute index (for client authentication)
     */
    protected static int ATTR_SSL_TRUSTSTORE_TYPE = -1;

    /**
     * truststore path attribute index (for client authentication)
     */
    protected static int ATTR_SSL_TRUSTSTORE_PATH = -1;

    /**
     * truststore password attribute index (for client authentication)
     */
    protected static int ATTR_SSL_TRUSTSTORE_PASSWORD = -1;

    /**
     * ssl authenticate status attribute index (for mandatory client
     *                                          authentication)
     */
    protected static int ATTR_SSL_AUTHENTICATE_REQUIRED = -1;

    /**
     * static initializer for TLS properties
     */
    static {
        Class cls = null;
        Attribute a = null;
        try {
            cls = Class.forName("org.w3c.jigsaw.https.socket.SSLProperties");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        a = new StringAttribute(PROTOCOL_NAME_P, DEFAULT_PROTOCOL_NAME, Attribute.EDITABLE);
        ATTR_SSL_PROTOCOL_NAME = AttributeRegistry.registerAttribute(cls, a);
        a = new BooleanAttribute(KEYSTORE_GENERIC_P, new Boolean(DEFAULT_KEYSTORE_GENERIC), Attribute.EDITABLE);
        ATTR_SSL_KEYSTORE_GENERIC = AttributeRegistry.registerAttribute(cls, a);
        a = new StringAttribute(KEYSTORE_TYPE_P, null, Attribute.EDITABLE);
        ATTR_SSL_KEYSTORE_TYPE = AttributeRegistry.registerAttribute(cls, a);
        a = new FileAttribute(KEYSTORE_PATH_P, null, Attribute.EDITABLE);
        ATTR_SSL_KEYSTORE_PATH = AttributeRegistry.registerAttribute(cls, a);
        a = new StringAttribute(KEYSTORE_PASSWORD_P, null, Attribute.EDITABLE);
        ATTR_SSL_KEYSTORE_PASSWORD = AttributeRegistry.registerAttribute(cls, a);
        a = new BooleanAttribute(TRUSTSTORE_GENERIC_P, new Boolean(DEFAULT_TRUSTSTORE_GENERIC), Attribute.EDITABLE);
        ATTR_SSL_TRUSTSTORE_GENERIC = AttributeRegistry.registerAttribute(cls, a);
        a = new StringAttribute(TRUSTSTORE_TYPE_P, null, Attribute.EDITABLE);
        ATTR_SSL_TRUSTSTORE_TYPE = AttributeRegistry.registerAttribute(cls, a);
        a = new FileAttribute(TRUSTSTORE_PATH_P, null, Attribute.EDITABLE);
        ATTR_SSL_TRUSTSTORE_PATH = AttributeRegistry.registerAttribute(cls, a);
        a = new StringAttribute(TRUSTSTORE_PASSWORD_P, null, Attribute.EDITABLE);
        ATTR_SSL_TRUSTSTORE_PASSWORD = AttributeRegistry.registerAttribute(cls, a);
        a = new BooleanAttribute(MUST_AUTHENTICATE_P, new Boolean(DEFAULT_MUST_AUTHENTICATE), Attribute.EDITABLE);
        ATTR_SSL_AUTHENTICATE_REQUIRED = AttributeRegistry.registerAttribute(cls, a);
    }

    /**
     * nameless constructor for SSL properties
     * @param server reference to the current daemon
     */
    public SSLProperties(httpd server) {
        super(DEFAULT_NAME, server);
    }

    /**
     * named constructor for SSL properties
     * @param name the name for the daemon in question
     * @param server reference to the current daemon
     */
    public SSLProperties(String name, httpd server) {
        super(name, server);
    }
}
