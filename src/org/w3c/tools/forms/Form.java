package org.w3c.tools.forms;

public class Form {

    /**
     * The handler of the form.
     */
    protected FormHandlerInterface handler = null;

    /**
     * Register a new form field.
     * @param name The name of the field to be defined.
     * @param title Title of the field to be defined.
     * @param field The field editor.
     */
    public void addField(String name, String title, FormField field) {
    }

    public Form(FormHandlerInterface handler) {
        this.handler = handler;
    }
}
