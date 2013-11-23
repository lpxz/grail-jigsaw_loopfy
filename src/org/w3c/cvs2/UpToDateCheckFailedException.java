package org.w3c.cvs2;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class UpToDateCheckFailedException extends CvsCommitException {

    UpToDateCheckFailedException(String filename) {
        super(filename);
    }

    /**
     * Get the filename.
     * @return a String.
     */
    public String getFilename() {
        return getMessage();
    }
}
