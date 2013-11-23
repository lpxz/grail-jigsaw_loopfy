package org.w3c.tools.resources;

public class SpaceEntryImpl implements SpaceEntry {

    Integer key = null;

    /**
     * Get the Key. This key must be unique and unchanged
     * during the all life.
     * @return an int.
     */
    public Integer getEntryKey() {
        return key;
    }

    public String toString() {
        return String.valueOf(key);
    }

    public SpaceEntryImpl(ContainerResource cont) {
        this.key = cont.getKey();
    }
}
