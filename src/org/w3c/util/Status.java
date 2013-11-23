package org.w3c.util;

public interface Status {

    /**
     * Give the status of this class as a partial HTML text which will be added
     * into a block level element
     * @return a String, the generated HTML
     */
    public String getHTMLStatus();
}
