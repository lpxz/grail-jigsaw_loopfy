package org.w3c.www.http;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;

class headerEnumerator implements Enumeration {

    HttpMessage message = null;

    int index = 0;

    Enumeration extras = null;

    boolean all = false;

    /**
     * Are there more available header descriptions ?
     * @return A boolean, <strong>true</strong> if more header descriptions
     * are available.
     */
    public boolean hasMoreElements() {
        edu.hkust.clap.monitor.Monitor.loopBegin(56);
while (index < HttpMessage.MAX_HEADERS) { 
edu.hkust.clap.monitor.Monitor.loopInc(56);
{
            if (all || message.values[index] != null) return true;
            index++;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(56);

        if (extras == null) {
            if (message.headers == null) return false;
            if (all) extras = message.factory.keys(); else extras = message.headers.keys();
        }
        return extras.hasMoreElements();
    }

    /**
     * Get the next header description out of this enumeration.
     * @return A HeaderDescription.
     * @exception NoSuchElement If no more header descriptions are available.
     */
    public Object nextElement() {
        if (index < HttpMessage.MAX_HEADERS) return message.descriptors[index++];
        if (extras == null) throw new NoSuchElementException("Enumeration exhausted.");
        String key = (String) extras.nextElement();
        return message.factory.get(key);
    }

    headerEnumerator(HttpMessage message, boolean all) {
        this.message = message;
        this.index = 0;
        this.all = all;
    }
}
