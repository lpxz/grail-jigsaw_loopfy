package org.w3c.jigsaw.ssi.commands;

import java.util.Date;
import java.util.Dictionary;
import org.w3c.www.http.HTTP;
import org.w3c.util.ArrayDictionary;
import org.w3c.util.TimeFormatter;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.tools.resources.FileResource;
import org.w3c.jigsaw.ssi.SSIFrame;

/**
 * Implementation of the standard <code>flastmod</code> SSI command.
 * @author Antonio Ramirez <anto@mit.edu>
 */
public class FLastModCommand extends BasicCommand {

    private static final String NAME = "flastmod";

    public Reply execute(SSIFrame ssiframe, Request request, ArrayDictionary parameters, Dictionary variables) {
        Reply reply = ssiframe.createCommandReply(request, HTTP.OK);
        reply.setContent(TimeFormatter.format(new Date(ssiframe.getFileResource().getFile().lastModified()), (String) variables.get("datefmt")));
        handleSimpleIMS(request, reply);
        return reply;
    }

    public String getName() {
        return NAME;
    }

    public String getValue(Dictionary variables, String variable, Request request) {
        return "null";
    }
}
