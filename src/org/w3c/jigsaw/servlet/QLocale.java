package org.w3c.jigsaw.servlet;

import java.util.Locale;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class QLocale {

    protected double quality;

    protected Locale locale;

    public double getLanguageQuality() {
        return quality;
    }

    public Locale getLocale() {
        return locale;
    }

    /**
     * Construct a locale from language, country and quality.
     */
    public QLocale(String language, String country, double quality) {
        this.locale = new Locale(language, country);
        this.quality = quality;
    }
}
