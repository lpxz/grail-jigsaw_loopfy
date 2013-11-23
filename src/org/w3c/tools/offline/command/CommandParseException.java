package org.w3c.tools.offline.command;

/**
 * The command line is not a valid jigshell command
 */
public class CommandParseException extends Exception {

    public CommandParseException() {
        super("ParseError");
    }
}
