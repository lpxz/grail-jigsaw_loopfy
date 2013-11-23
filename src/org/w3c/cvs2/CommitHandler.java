package org.w3c.cvs2;

abstract class CommitHandler {

    abstract void notifyEntry(String filename, int status);
}
