package org.w3c.jigsaw.pics;

public class InvalidLabelException extends Exception {

    public InvalidLabelException(String msg) {
        super(msg);
    }

    public InvalidLabelException(int lineno, String msg) {
        this("[" + lineno + "]" + ": " + msg);
    }
}
