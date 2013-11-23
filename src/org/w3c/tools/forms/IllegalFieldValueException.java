package org.w3c.tools.forms;

/**
 * This exception is thrown when a field is set to an invalid value.
 */
public class IllegalFieldValueException extends Exception {

    IllegalFieldValueException(Object arg) {
        super("illegal value: " + arg);
    }
}
