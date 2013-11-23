package org.w3c.tools.forms;

public interface FormHandlerInterface {

    /**
     * The field whose name is given has changed.
     */
    public void fieldChanged(String name);

    /**
     * Is the given value appropriate for the given field ?
     */
    public boolean acceptFieldValue(String name, Object value);

    /**
     * The whole form has been validated.
     */
    public void validate();
}
