package org.w3c.jigsaw.ssi.commands;

public class ControlCommandException extends Exception {

    public ControlCommandException(String msg) {
        super(msg);
    }

    public ControlCommandException(String name, String msg) {
        super("[" + name + "] : " + msg);
    }
}
