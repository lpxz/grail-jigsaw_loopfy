package org.w3c.jigsaw.ssi.commands;

import java.util.Dictionary;
import org.w3c.www.http.HTTP;
import org.w3c.util.ArrayDictionary;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.ssi.SSIFrame;

public class SampleCommand extends BasicCommand {

    protected static final String NAME = "params";

    public Reply execute(SSIFrame ssiframe, Request request, ArrayDictionary parameters, Dictionary variables) {
        Reply reply = ssiframe.createCommandReply(request, HTTP.OK);
        StringBuffer sb = new StringBuffer();
        sb.append("<ul>");
        edu.hkust.clap.monitor.Monitor.loopBegin(959);
for (int i = 0; i < parameters.capacity() && parameters.keyAt(i) != null; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(959);
sb.append("<li>" + parameters.keyAt(i) + " = \"" + parameters.elementAt(i) + "\"</li>");} 
edu.hkust.clap.monitor.Monitor.loopEnd(959);

        sb.append("</ul>");
        reply.setContent(sb.toString());
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
