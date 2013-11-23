package org.w3c.www.mime;

import java.util.Vector;
import java.io.PrintStream;
import java.io.Serializable;

/**
 * This class is used to represent parsed MIME types. 
 * It creates this representation from a string based representation of
 * the MIME type, as defined in the RFC 1345.
 */
public class MimeType implements Serializable, Cloneable {

    /** 
     * List of well known MIME types:
     */
    public static MimeType TEXT_HTML = null;

    public static MimeType APPLICATION_POSTSCRIPT = null;

    public static MimeType TEXT_PLAIN = null;

    public static MimeType APPLICATION_X_WWW_FORM_URLENCODED = null;

    public static MimeType APPLICATION_OCTET_STREAM = null;

    public static MimeType MULTIPART_FORM_DATA = null;

    public static MimeType APPLICATION_X_JAVA_AGENT = null;

    public static MimeType MESSAGE_HTTP = null;

    public static MimeType TEXT_CSS = null;

    public static MimeType TEXT_XML = null;

    public static MimeType APPLICATION_XML = null;

    public static MimeType TEXT = null;

    public static MimeType APPLICATION_RDF_XML = null;

    public static MimeType APPLICATION_XHTML_XML = null;

    public static String star = "*".intern();

    static {
        try {
            TEXT_HTML = new MimeType("text/html");
            APPLICATION_POSTSCRIPT = new MimeType("application/postscript");
            TEXT_PLAIN = new MimeType("text/plain");
            APPLICATION_X_WWW_FORM_URLENCODED = new MimeType("application/x-www-form-urlencoded");
            APPLICATION_OCTET_STREAM = new MimeType("application/octet-stream");
            MULTIPART_FORM_DATA = new MimeType("multipart/form-data");
            APPLICATION_X_JAVA_AGENT = new MimeType("application/x-java-agent");
            MESSAGE_HTTP = new MimeType("message/http");
            TEXT_CSS = new MimeType("text/css");
            TEXT_XML = new MimeType("text/xml");
            TEXT = new MimeType("text/*");
            APPLICATION_RDF_XML = new MimeType("application/rdf+xml");
            APPLICATION_XHTML_XML = new MimeType("application/xhtml+xml");
            APPLICATION_XML = new MimeType("application/xml");
        } catch (MimeTypeFormatException e) {
            System.out.println("httpd.MimeType: invalid static init.");
            System.exit(1);
        }
    }

    public static final int NO_MATCH = -1;

    public static final int MATCH_TYPE = 1;

    public static final int MATCH_SPECIFIC_TYPE = 2;

    public static final int MATCH_SUBTYPE = 3;

    public static final int MATCH_SPECIFIC_SUBTYPE = 4;

    /**
     * String representation of type
     *
     * @serial
     */
    protected String type = null;

    /**
     * String representation of subtype
     *
     * @serial
     */
    protected String subtype = null;

    /**
     * parameter names
     *
     * @serial
     */
    protected String pnames[] = null;

    /**
     * parameter values
     *
     * @serial
     */
    protected String pvalues[] = null;

    /**
     * external form of this mime type
     *
     * @serial
     */
    protected String external = null;

    /**
     * How good the given MimeType matches the receiver of the method ?
     *  This method returns a matching level among:
     * <dl>
     * <dt>NO_MATCH<dd>Types not matching,</dd>
     * <dt>MATCH_TYPE<dd>Types match,</dd>
     * <dt>MATCH_SPECIFIC_TYPE<dd>Types match exactly,</dd>
     * <dt>MATCH_SUBTYPE<dd>Types match, subtypes matches too</dd>
     * <dt>MATCH_SPECIFIC_SUBTYPE<dd>Types match, subtypes matches exactly</dd>
     * </dl>
     * The matches are ranked from worst match to best match, a simple
     * Max ( match[i], matched) will give the best match. 
     * @param other The other MimeType to match against ourself.
     */
    public int match(MimeType other) {
        int match = NO_MATCH;
        if ((type == star) || (other.type == star)) {
            return MATCH_TYPE;
        } else if (type != other.type) {
            return NO_MATCH;
        } else {
            match = MATCH_SPECIFIC_TYPE;
        }
        if ((subtype == star) || (other.subtype == star)) {
            match = MATCH_SUBTYPE;
        } else if (subtype != other.subtype) {
            return NO_MATCH;
        } else {
            match = MATCH_SPECIFIC_SUBTYPE;
        }
        return match;
    }

    /**
     * Find out if mime types are equivalent, based on heuristics
     * like text/xml <=> application/xml and other problems related
     * to format that may have multiple mime types.
     * Note that text/html and application/xhtml+xml are not exactly
     * the same
     * @param mtype, a MimeType
     * @return a boolean, true if the two mime types are equivalent
     */
    public boolean equiv(MimeType mtype) {
        if (match(mtype) == MATCH_SPECIFIC_SUBTYPE) {
            return true;
        }
        if ((match(TEXT_XML) == MATCH_SPECIFIC_SUBTYPE) || (match(APPLICATION_XML) == MATCH_SPECIFIC_SUBTYPE)) {
            if ((mtype.match(TEXT_XML) == MATCH_SPECIFIC_SUBTYPE) || (mtype.match(APPLICATION_XML) == MATCH_SPECIFIC_SUBTYPE)) {
                return true;
            }
        }
        return false;
    }

    /**
     * A printable representation of this MimeType. 
     * The printed representation is guaranteed to be parseable by the
     * String constructor.
     */
    public String toString() {
        if (external == null) {
            StringBuffer sb = new StringBuffer(type);
            sb.append((char) '/');
            sb.append(subtype);
            if (pnames != null) {
                edu.hkust.clap.monitor.Monitor.loopBegin(419);
for (int i = 0; i < pnames.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(419);
{
                    sb.append(';');
                    sb.append(pnames[i]);
                    if (pvalues[i] != null) {
                        sb.append('=');
                        sb.append(pvalues[i]);
                    }
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(419);

            }
            external = sb.toString().intern();
        }
        return external;
    }

    /**
     * Does this MIME type has some value for the given parameter ?
     * @param name The parameter to check.
     * @return <strong>True</strong> if this parameter has a value, false
     *    otherwise.
     */
    public boolean hasParameter(String name) {
        if (name != null) {
            if (pnames != null) {
                String lname = name.toLowerCase();
                edu.hkust.clap.monitor.Monitor.loopBegin(420);
for (int i = 0; i < pnames.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(420);
{
                    if (pnames[i].equals(name)) return true;
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(420);

            }
        }
        return false;
    }

    /**
     * Get the major type of this mime type.
     * @return The major type, encoded as a String.
     */
    public String getType() {
        return type;
    }

    /**
     * Get the minor type (subtype) of this mime type.
     * @return The minor or subtype encoded as a String.
     */
    public String getSubtype() {
        return subtype;
    }

    /**
     * Get a mime type parameter value.
     * @param name The parameter whose value is to be returned.
     * @return The parameter value, or <b>null</b> if not found.
     */
    public String getParameterValue(String name) {
        if (name != null) {
            if (pnames != null) {
                String lname = name.toLowerCase();
                edu.hkust.clap.monitor.Monitor.loopBegin(421);
for (int i = 0; i < pnames.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(421);
{
                    if (pnames[i].equals(lname)) return pvalues[i];
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(421);

            }
        }
        return null;
    }

    /**
     * adds some parameters to a MimeType
     * @param param a String array of parameter names
     * @param values the corresponding String array of values
     */
    public void addParameters(String[] param, String[] values) {
        if ((param == null) || (values == null) || (values.length != param.length)) return;
        if (pnames == null) {
            pnames = param;
            pvalues = values;
        } else {
            String[] nparam = new String[pnames.length + param.length];
            String[] nvalues = new String[pvalues.length + values.length];
            System.arraycopy(pnames, 0, nparam, 0, pnames.length);
            System.arraycopy(param, 0, nparam, pnames.length, param.length);
            System.arraycopy(pvalues, 0, nvalues, 0, pvalues.length);
            System.arraycopy(values, 0, nvalues, pvalues.length, values.length);
            pnames = nparam;
            pvalues = nvalues;
        }
        edu.hkust.clap.monitor.Monitor.loopBegin(422);
for (int i = 0; i < pnames.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(422);
{
            pnames[i] = pnames[i].toLowerCase();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(422);

        external = null;
    }

    /**
     * get a clone of this object
     * @return another cloned instance of MimeType
     */
    public MimeType getClone() {
        try {
            return (MimeType) clone();
        } catch (CloneNotSupportedException ex) {
        }
        return null;
    }

    /**
     * adds a parameterto a MimeType
     * @param param the parameter name, as a String
     * @param value  the parameter value, as a Sting
     */
    public void addParameter(String param, String value) {
        String[] p = new String[1];
        String[] v = new String[1];
        p[0] = param;
        v[0] = value;
        addParameters(p, v);
    }

    /**
     * Set the parameter to a MimeType (replace old value if any).
     * @param param the parameter name, as a String
     * @param value  the parameter value, as a Sting
     */
    public void setParameter(String param, String value) {
        if (pnames == null) {
            addParameter(param, value);
        } else {
            String lparam = param.toLowerCase();
            edu.hkust.clap.monitor.Monitor.loopBegin(423);
for (int i = 0; i < pnames.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(423);
{
                if (pnames[i].equals(lparam)) {
                    pvalues[i] = value;
                    return;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(423);

            addParameter(lparam, value);
        }
    }

    /**
     * Construct  MimeType object for the given string.
     * The string should be the representation of the type. This methods
     * tries to be compliant with HTTP1.1, p 15, although it is not
     * (because of quoted-text not being accepted).
     * FIXME
     * @parameter spec A string representing a MimeType
     * @return A MimeType object
     * @exception MimeTypeFormatException if the string couldn't be parsed.
     */
    public MimeType(String spec) throws MimeTypeFormatException {
        int strl = spec.length();
        int start = 0, look = -1;
        edu.hkust.clap.monitor.Monitor.loopBegin(424);
while ((start < strl) && (spec.charAt(start)) <= ' ') { 
edu.hkust.clap.monitor.Monitor.loopInc(424);
start++;} 
edu.hkust.clap.monitor.Monitor.loopEnd(424);

        edu.hkust.clap.monitor.Monitor.loopBegin(425);
while ((strl > start) && (spec.charAt(strl - 1) <= ' ')) { 
edu.hkust.clap.monitor.Monitor.loopInc(425);
strl--;} 
edu.hkust.clap.monitor.Monitor.loopEnd(425);

        StringBuffer sb = new StringBuffer();
        edu.hkust.clap.monitor.Monitor.loopBegin(426);
while ((start < strl) && ((look = spec.charAt(start)) != '/')) { 
edu.hkust.clap.monitor.Monitor.loopInc(426);
{
            sb.append(Character.toLowerCase((char) look));
            start++;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(426);

        if (look != '/') throw new MimeTypeFormatException(spec);
        this.type = sb.toString().intern();
        start++;
        sb.setLength(0);
        edu.hkust.clap.monitor.Monitor.loopBegin(427);
while ((start < strl) && ((look = spec.charAt(start)) > ' ') && (look != ';')) { 
edu.hkust.clap.monitor.Monitor.loopInc(427);
{
            sb.append(Character.toLowerCase((char) look));
            start++;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(427);

        this.subtype = sb.toString().intern();
        edu.hkust.clap.monitor.Monitor.loopBegin(428);
while ((start < strl) && ((look = spec.charAt(start)) <= ' ')) { 
edu.hkust.clap.monitor.Monitor.loopInc(428);
start++;} 
edu.hkust.clap.monitor.Monitor.loopEnd(428);

        if (start < strl) {
            if (spec.charAt(start) != ';') throw new MimeTypeFormatException(spec);
            start++;
            Vector vp = new Vector(4);
            Vector vv = new Vector(4);
            edu.hkust.clap.monitor.Monitor.loopBegin(429);
while (start < strl) { 
edu.hkust.clap.monitor.Monitor.loopInc(429);
{
                edu.hkust.clap.monitor.Monitor.loopBegin(430);
while ((start < strl) && (spec.charAt(start) <= ' ')) { 
edu.hkust.clap.monitor.Monitor.loopInc(430);
start++;} 
edu.hkust.clap.monitor.Monitor.loopEnd(430);

                sb.setLength(0);
                edu.hkust.clap.monitor.Monitor.loopBegin(431);
while ((start < strl) && ((look = spec.charAt(start)) > ' ') && (look != '=')) { 
edu.hkust.clap.monitor.Monitor.loopInc(431);
{
                    sb.append(Character.toLowerCase((char) look));
                    start++;
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(431);

                String name = sb.toString();
                edu.hkust.clap.monitor.Monitor.loopBegin(430);
while ((start < strl) && (spec.charAt(start) <= ' ')) { 
edu.hkust.clap.monitor.Monitor.loopInc(430);
start++;} 
edu.hkust.clap.monitor.Monitor.loopEnd(430);

                if (spec.charAt(start) != '=') throw new MimeTypeFormatException(spec);
                start++;
                edu.hkust.clap.monitor.Monitor.loopBegin(432);
while ((start < strl) && ((spec.charAt(start) == '"') || (spec.charAt(start) <= ' '))) { 
edu.hkust.clap.monitor.Monitor.loopInc(432);
start++;} 
edu.hkust.clap.monitor.Monitor.loopEnd(432);

                sb.setLength(0);
                edu.hkust.clap.monitor.Monitor.loopBegin(433);
while ((start < strl) && ((look = spec.charAt(start)) > ' ') && (look != ';') && (look != '"')) { 
edu.hkust.clap.monitor.Monitor.loopInc(433);
{
                    sb.append((char) look);
                    start++;
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(433);

                edu.hkust.clap.monitor.Monitor.loopBegin(434);
while ((start < strl) && (spec.charAt(start) != ';')) { 
edu.hkust.clap.monitor.Monitor.loopInc(434);
start++;} 
edu.hkust.clap.monitor.Monitor.loopEnd(434);

                start++;
                String value = sb.toString();
                vp.addElement(name);
                vv.addElement(value);
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(429);

            this.pnames = new String[vp.size()];
            vp.copyInto(pnames);
            this.pvalues = new String[vv.size()];
            vv.copyInto(pvalues);
        }
    }

    public MimeType(String type, String subtype, String pnames[], String pvalues[]) {
        this.type = type.toLowerCase().intern();
        this.subtype = subtype.toLowerCase().intern();
        this.pnames = pnames;
        this.pvalues = pvalues;
    }

    public MimeType(String type, String subtype) {
        this.type = type.toLowerCase().intern();
        this.subtype = subtype.toLowerCase().intern();
    }

    public static void main(String args[]) {
        if (args.length == 1) {
            MimeType type = null;
            try {
                type = new MimeType(args[0]);
            } catch (MimeTypeFormatException e) {
            }
            if (type != null) {
                System.out.println(type);
                if (type.getClone().match(type) == MATCH_SPECIFIC_SUBTYPE) {
                    System.out.println("Clone OK");
                } else {
                    System.out.println("Cloning failed");
                }
            } else {
                System.out.println("Invalid mime type specification.");
            }
        } else {
            System.out.println("Usage: java MimeType <type-to-parse>");
        }
    }
}
