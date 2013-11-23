package org.w3c.cvs2;

abstract class UpdateHandler implements CVS {

    abstract void notifyEntry(String filename, int status);
}
