package org.w3c.www.mime;

import java.io.Serializable;

/**
 * This class is used to represent parsed Language tags,
 * It creates a representation from a string based representation
 * of the Language tag, as defined in RFC 1766
 * NOTE, we don't check that languages are defined according to ISO 639
 */
public class LanguageTag implements Serializable, Cloneable {

    public static int NO_MATCH = -1;

    public static int MATCH_LANGUAGE = 1;

    public static int MATCH_SPECIFIC_LANGUAGE = 2;

    public static int MATCH_SUBTAG = 3;

    public static int MATCH_SPECIFIC_SUBTAG = 4;

    /**
     * String representation of the language
     *
     * @serial
     */
    protected String language = null;

    /**
     * String representation of subtag
     *
     * @serial
     */
    protected String subtag = null;

    /**
     * external form of this language tag
     *
     * @serial
     */
    protected String external = null;

    /**
     * How good the given LanguageTag matches the receiver of the method ?
     *  This method returns a matching level among:
     * <dl>
     * <dt>NO_MATCH<dd>Language not matching,</dd>
     * <dt>MATCH_LANGUAGE<dd>Languages match roughly (with *),</dd>
     * <dt>MATCH_SPECIFIC_LANGUAGE<dd>Languages match exactly,</dd>
     * <dt>MATCH_SUBTAG<dd>Languages match, subtags matches roughly</dd>
     * <dt>MATCH_SPECIFIC_SUBAG<dd>Languages match, subtag matches exactly</dd>
     * </dl>
     * The matches are ranked from worst match to best match, a simple
     * Max ( match[i], matched) will give the best match. 
     * @param other The other LanguageTag to match against ourself.
     */
    public int match(LanguageTag other) {
        int match = NO_MATCH;
        if (language.equals("*") || other.language.equals("*")) {
            match = MATCH_LANGUAGE;
        } else if (!language.equalsIgnoreCase(other.language)) {
            return NO_MATCH;
        } else {
            match = MATCH_SPECIFIC_LANGUAGE;
        }
        if ((subtag == null) || (other.subtag == null)) return match;
        if (subtag.equals("*") || other.subtag.equals("*")) {
            match = MATCH_SUBTAG;
        } else if (!subtag.equalsIgnoreCase(other.subtag)) {
            return NO_MATCH;
        } else {
            match = MATCH_SPECIFIC_SUBTAG;
        }
        return match;
    }

    /**
     * A printable representation of this LanguageTag. 
     * The printed representation is guaranteed to be parseable by the
     * String constructor.
     */
    public String toString() {
        if (external == null) {
            if (subtag != null) {
                external = language + "-" + subtag;
            } else {
                external = language;
            }
        }
        return external;
    }

    /**
     * Get the language
     * @return The language, encoded as a String.
     */
    public String getLanguage() {
        return language;
    }

    /** 
     * Get the subtag
     * @return The subtag, encoded as a string
     */
    public String getSubtag() {
        return language;
    }

    /**
     * Construct a Language tag from a spec
     * @parameter spec, A string representing a LangateTag
     */
    public LanguageTag(String spec) {
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
        edu.hkust.clap.monitor.Monitor.loopBegin(999);
while ((start < strl) && ((look = spec.charAt(start)) != '-') && ((look = spec.charAt(start)) != ';')) { 
edu.hkust.clap.monitor.Monitor.loopInc(999);
{
            sb.append((char) look);
            start++;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(999);

        this.language = sb.toString();
        if (look == '-') {
            start++;
            sb.setLength(0);
            edu.hkust.clap.monitor.Monitor.loopBegin(1000);
while ((start < strl) && ((look = spec.charAt(start)) > ' ') && (look != ';')) { 
edu.hkust.clap.monitor.Monitor.loopInc(1000);
{
                sb.append((char) look);
                start++;
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1000);

            this.subtag = sb.toString();
        }
    }

    /**
     * construct directly a language tag
     * it NEEDS both language and subtype parameters
     */
    public LanguageTag(String language, String subtag) {
        this.language = language;
        this.subtag = subtag;
    }
}
