package org.w3c.util;

public interface PropertyMonitoring {

    /**
     * The callback method, invoked when any property change occurs.
     * @param name The name of the property that changed.
     * @return A boolean, if <strong>true</strong>, accept the new property
     *    value, otherwise, reject it and reset the property to its old
     *    value.
     */
    public boolean propertyChanged(String name);
}
