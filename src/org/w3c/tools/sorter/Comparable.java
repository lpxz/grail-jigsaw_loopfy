package org.w3c.tools.sorter;

/**
 * used for string comparison.
 */
public interface Comparable {

    public boolean greaterThan(Comparable comp);

    public String getStringValue();
}
