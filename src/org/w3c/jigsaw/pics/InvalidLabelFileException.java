package org.w3c.jigsaw.pics;

import java.io.File;

public class InvalidLabelFileException extends InvalidLabelException {

    public InvalidLabelFileException(String msg) {
        super(msg);
    }

    public InvalidLabelFileException(File file, int lineno, String msg) {
        this(file.getAbsolutePath() + "[" + lineno + "]" + ": " + msg);
    }
}
