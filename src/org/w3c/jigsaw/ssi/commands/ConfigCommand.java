package org.w3c.jigsaw.ssi.commands;

import java.util.Dictionary;
import org.w3c.util.ArrayDictionary;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.ssi.SSIFrame;

/**
 * Implementation of the <code>config</code> SSI command.
 * Used to set the <code>sizefmt</code> and <code>datefmt</code> variables,
 * which control the output of file sizes and dates.
 * @author Antonio Ramirez <anto@mit.edu>
 */
public class ConfigCommand implements Command {

    private static final String NAME = "config";

    public Reply execute(SSIFrame ssiframe, Request request, ArrayDictionary parameters, Dictionary variables) {
        String parName = null, parValue = null;
        edu.hkust.clap.monitor.Monitor.loopBegin(1038);
for (int i = 0; i < parameters.capacity(); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1038);
{
            parName = (String) parameters.keyAt(i);
            if (parName == null) continue;
            parValue = (String) parameters.elementAt(i);
            if (parName.equals("sizefmt")) {
                if (!parValue.equalsIgnoreCase("bytes") && !parValue.equalsIgnoreCase("abbrev")) continue; else variables.put(parName, parValue.toLowerCase());
            } else if (parName.equals("datefmt")) {
                variables.put(parName, parValue);
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1038);

        return null;
    }

    public String getName() {
        return NAME;
    }

    public String getValue(Dictionary variables, String variable, Request request) {
        return "null";
    }

    /**
     * return true if reply can be cached.
     * @return a boolean.
     */
    public boolean acceptCaching() {
        return true;
    }
}
