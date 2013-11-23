package org.w3c.tools.offline.command;

import java.io.StringReader;
import java.io.StreamTokenizer;
import java.io.IOException;
import java.util.Vector;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.MalformedPatternException;

/**
 * <p>The jigshell command line class.
 */
public class CommandLine {

    /** static members: jigshell commands syntax */
    public static String WHERE = "where";

    public static String LIST = "list";

    public static String GO = "go";

    public static String REC = "r";

    public static String ATTR = "a";

    public static String UP = "..";

    public static String NO_OPT = "none";

    private String cmd = null;

    private String action = null;

    private String target = null;

    private String option = NO_OPT;

    /** 
	 * a Vector to handle elements of a parsed command
	 */
    protected Vector parsedCmd;

    protected static Perl5Matcher pmatcher = new Perl5Matcher();

    protected static Perl5Compiler pcompiler = new Perl5Compiler();

    protected static Pattern srPattern;

    /** 
	*  Initialize a CommandLine instance.  
	* @param s the command line.
	*/
    public CommandLine(String s) {
        try {
            cmd = s;
            parsedCmd = new Vector();
            srPattern = pcompiler.compile("^s/[\\w|=|\\*|\\-|\\\\/]+?/[\\w|\\-|\\\\/]+/$", Perl5Compiler.DEFAULT_MASK);
        } catch (org.apache.oro.text.regex.MalformedPatternException ex) {
            ex.printStackTrace();
        }
    }

    /** 
	* Parse a CommandLine instance.  
	*/
    public void parse() throws CommandParseException {
        StringReader r = new StringReader(cmd);
        StreamTokenizer st = new StreamTokenizer(r);
        st.ordinaryChar('.');
        st.wordChars('!', ',');
        st.wordChars('.', '/');
        st.wordChars('=', '=');
        st.wordChars('?', '@');
        st.wordChars('[', '`');
        try {
            edu.hkust.clap.monitor.Monitor.loopBegin(5);
while (st.nextToken() != st.TT_EOF) { 
edu.hkust.clap.monitor.Monitor.loopInc(5);
{
                if (st.ttype == st.TT_WORD) {
                    parsedCmd.addElement(new String(st.sval));
                }
                if (st.ttype == '-') {
                    parsedCmd.addElement(new String("-"));
                }
                if (st.ttype == st.TT_NUMBER) {
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(5);

        } catch (IOException e) {
            throw new CommandParseException();
        }
        switch(parsedCmd.size()) {
            case 0:
                break;
            case 1:
                action = (String) parsedCmd.elementAt(0);
                if (action.compareTo(LIST) == 0 || action.compareTo(WHERE) == 0) {
                    target = ".*";
                } else {
                    throw new CommandParseException();
                }
                break;
            default:
                action = (String) parsedCmd.elementAt(0);
                if (isaReplaceAction(action) == true || action.compareTo(LIST) == 0 || action.compareTo(GO) == 0) {
                    boolean isOption = false;
                    edu.hkust.clap.monitor.Monitor.loopBegin(6);
for (int i = 1; i < parsedCmd.size(); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(6);
{
                        String curWord = (String) parsedCmd.elementAt(i);
                        if (isOption) {
                            isOption = false;
                            if (curWord.compareTo(REC) == 0 || curWord.compareTo(ATTR) == 0 || curWord.compareTo(REC + ATTR) == 0 || curWord.compareTo(ATTR + REC) == 0) {
                                option = curWord;
                            } else {
                                System.out.println("option discarded " + curWord);
                            }
                        } else {
                            if (curWord.compareTo("-") == 0) {
                                isOption = true;
                            } else {
                                target = curWord;
                                break;
                            }
                        }
                    }} 
edu.hkust.clap.monitor.Monitor.loopEnd(6);

                } else {
                    throw new CommandParseException();
                }
                if (target == null) {
                    throw new CommandParseException();
                }
        }
    }

    /**
	 * Get the command line action
	 * @return the string action (should be a jigshell action).
	 */
    public String getAction() {
        return (action);
    }

    /**
	 * Get the command target 
	 * @return the string target (should be a name or regexp).
	 */
    public String getTarget() {
        return (target);
    }

    /**
	 * Get the command option 
	 * @return the command option ("none" if no option specified in
	 * the command line).
	 */
    public String getOption() {
        return (option);
    }

    private boolean isaReplaceAction(String s) {
        if (pmatcher.matches(s, srPattern)) {
            return true;
        }
        return false;
    }
}
