package org.w3c.jigsaw.ssi;

import java.util.Dictionary;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.w3c.util.ArrayDictionary;
import org.w3c.tools.resources.FileResource;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.ssi.commands.Command;
import org.w3c.jigsaw.ssi.commands.CommandRegistry;
import org.w3c.jigsaw.ssi.commands.ControlCommand;
import org.w3c.jigsaw.ssi.commands.ControlCommandException;

public class Segment {

    protected static final int UNPARSED = 0;

    protected static final int COMMAND = 1;

    protected static final String SEPARATOR = "|";

    protected boolean control = false;

    protected SSIFrame ssiframe = null;

    protected Request request = null;

    public boolean isControl() {
        return control;
    }

    /**
     * Where this segment starts in the input file (inclusive)
     */
    int start = 0;

    /**
     * Where it ends (exclusive)
     */
    int end = 0;

    /** The name of the command that this segment runs, if any  */
    String commandName = null;

    /** The command that it's linked to, if any. */
    private Command command = null;

    /** The last registry that this command was executed from, if any */
    private CommandRegistry registry = null;

    /** Its list of parameters */
    ArrayDictionary parameters = null;

    public Segment(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public Segment(SSIFrame ssiframe, String commandName, ArrayDictionary parameters, int start, int end) {
        this.ssiframe = ssiframe;
        this.commandName = commandName;
        this.parameters = parameters;
        this.start = start;
        this.end = end;
    }

    protected Segment() {
        start = end = -1;
    }

    public String pickle() {
        StringBuffer buffer = new StringBuffer(128);
        if (commandName != null) {
            buffer.append(COMMAND);
            buffer.append(SEPARATOR);
            buffer.append(start);
            buffer.append(SEPARATOR);
            buffer.append(end);
            buffer.append(SEPARATOR);
            buffer.append(parameters.size());
            edu.hkust.clap.monitor.Monitor.loopBegin(839);
for (int i = 0; i < parameters.capacity() && parameters.keyAt(i) != null; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(839);
{
                buffer.append(SEPARATOR);
                buffer.append((String) parameters.keyAt(i));
                buffer.append(SEPARATOR);
                buffer.append((String) parameters.elementAt(i));
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(839);

        } else {
            buffer.append(UNPARSED);
            buffer.append(SEPARATOR);
            buffer.append(start);
            buffer.append(SEPARATOR);
            buffer.append(end);
        }
        return buffer.toString();
    }

    public static Segment unpickle(String value) {
        Segment seg = new Segment();
        StringTokenizer st = new StringTokenizer(value, SEPARATOR);
        try {
            int type = Integer.parseInt(st.nextToken());
            seg.start = Integer.parseInt(st.nextToken());
            seg.end = Integer.parseInt(st.nextToken());
            if (type == COMMAND) {
                int n = Integer.parseInt(st.nextToken());
                String parNames[] = new String[n];
                String parValues[] = new String[n];
                edu.hkust.clap.monitor.Monitor.loopBegin(840);
for (int i = 0; i < n; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(840);
{
                    parNames[i] = st.nextToken();
                    parValues[i] = st.nextToken();
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(840);

                seg.parameters = new ArrayDictionary(parNames, parValues);
            } else {
                seg.commandName = null;
                seg.parameters = null;
            }
        } catch (NoSuchElementException ex) {
            ex.printStackTrace();
        }
        return seg;
    }

    public void pickle(DataOutputStream out) throws IOException {
        if (commandName != null) {
            out.writeInt(COMMAND);
            out.writeInt(start);
            out.writeInt(end);
            out.writeUTF(commandName);
            out.writeInt(parameters.size());
            edu.hkust.clap.monitor.Monitor.loopBegin(841);
for (int i = 0; i < parameters.capacity() && parameters.keyAt(i) != null; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(841);
{
                out.writeUTF((String) parameters.keyAt(i));
                out.writeUTF((String) parameters.elementAt(i));
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(841);

        } else {
            out.writeInt(UNPARSED);
            out.writeInt(start);
            out.writeInt(end);
        }
    }

    public static Segment unpickle(DataInputStream in) throws IOException {
        Segment seg = new Segment();
        int type = in.readInt();
        seg.start = in.readInt();
        seg.end = in.readInt();
        if (type == COMMAND) {
            seg.commandName = in.readUTF();
            int n = in.readInt();
            String parNames[] = new String[n];
            String parValues[] = new String[n];
            edu.hkust.clap.monitor.Monitor.loopBegin(842);
for (int i = 0; i < n; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(842);
{
                parNames[i] = in.readUTF();
                parValues[i] = in.readUTF();
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(842);

            seg.parameters = new ArrayDictionary(parNames, parValues);
        } else {
            seg.commandName = null;
            seg.parameters = null;
        }
        return seg;
    }

    public final boolean isUnparsed() {
        return commandName == null;
    }

    public final String toString() {
        return commandName != null ? "<" + start + ',' + end + ": " + commandName + " " + parameters + '>' : "<" + start + ',' + end + '>';
    }

    public int jumpTo() throws ControlCommandException {
        if ((!control) || (command == null)) throw new ControlCommandException("SEGMENT", "Internal Error");
        ControlCommand cc = (ControlCommand) command;
        int jump = 0;
        jump = cc.jumpTo(ssiframe, request, registry, parameters, ssiframe.vars);
        return jump;
    }

    public final Reply get() {
        if (command == null) return null;
        return command.execute(ssiframe, request, parameters, ssiframe.vars);
    }

    public boolean needsRevalidate() {
        if (command == null) return false; else return (!command.acceptCaching());
    }

    public final Reply init(SSIFrame ssiframe, Request request, Dictionary variables, CommandRegistry registry, int position) {
        if (commandName == null) return null;
        this.ssiframe = ssiframe;
        this.request = request;
        if (command == null || registry != this.registry) {
            this.registry = registry;
            this.command = registry.lookupCommand(commandName);
            this.control = (command instanceof ControlCommand);
        }
        if (control) {
            ControlCommand cc = (ControlCommand) command;
            cc.setPosition(ssiframe, request, registry, parameters, variables, position);
            ssiframe.doNotCacheReply();
            return null;
        }
        if ((ssiframe.cacheReplies()) && command.acceptCaching()) return get(); else return null;
    }
}
