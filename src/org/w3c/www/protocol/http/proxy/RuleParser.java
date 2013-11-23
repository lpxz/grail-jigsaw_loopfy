package org.w3c.www.protocol.http.proxy;

import java.util.StringTokenizer;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;

/**
 * A simple Rule parser.
 */
public class RuleParser {

    InputStream in = null;

    RuleNode root = null;

    /**
     * Add a mapping for the given rule in our rule node.
     * @param lhs The rule left hand side, as a parsed String array.
     * @param rule The mapped rule instance.
     */
    protected void addRule(String lhs[], Rule rule) {
        RuleNode node = root;
        int lhslen = lhs.length;
        if (!lhs[0].equals("default")) {
            edu.hkust.clap.monitor.Monitor.loopBegin(363);
for (int i = lhslen; --i >= 0; ) { 
edu.hkust.clap.monitor.Monitor.loopInc(363);
{
                RuleNode child = node.lookup(lhs[i]);
                if (child == null) child = node.addChild(lhs[i]);
                node = child;
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(363);

        }
        node.setRule(rule);
    }

    /**
     * Create a suitable rule mapping for the tokenized rule.
     * @param tokens The rule tokens, as a String array.
     * @param toklen Number of tokens in above array.
     * @exception RuleParserException if parsing failed.
     */
    protected void parseRule(String tokens[], int toklen) throws RuleParserException {
        StringTokenizer st = new StringTokenizer((String) tokens[0], ".");
        Vector vlhs = new Vector();
        String vls;
        boolean isnum = false;
        edu.hkust.clap.monitor.Monitor.loopBegin(364);
while (st.hasMoreTokens()) { 
edu.hkust.clap.monitor.Monitor.loopInc(364);
{
            isnum = true;
            vls = st.nextToken();
            edu.hkust.clap.monitor.Monitor.loopBegin(365);
for (int i = 0; isnum && (i < vls.length()); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(365);
isnum = (vls.charAt(i) >= '0') && (vls.charAt(i) <= '9');} 
edu.hkust.clap.monitor.Monitor.loopEnd(365);

            vlhs.addElement(vls);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(364);

        if (isnum) {
            int vs = vlhs.size();
            edu.hkust.clap.monitor.Monitor.loopBegin(366);
for (int i = 0; i < vs; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(366);
{
                vlhs.addElement(vlhs.elementAt(vs - i - 1));
                vlhs.removeElementAt(vs - i - 1);
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(366);

        }
        String slhs[] = new String[vlhs.size()];
        vlhs.copyInto(slhs);
        Rule rule = Rule.createRule(tokens, 1, toklen);
        addRule(slhs, rule);
    }

    /**
     * Parse the our input stream into a RuleNode instance.
     * @exception IOException If reading the rule input stream failed.
     * @exception RuleParserException If some invalid rule syntax was
     * detected.
     */
    public RuleNode parse() throws RuleParserException, IOException {
        boolean eof = false;
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StreamTokenizer st = new StreamTokenizer(br);
        st.resetSyntax();
        st.wordChars('a', 'z');
        st.wordChars('A', 'Z');
        st.wordChars('0', '9');
        st.wordChars(128 + 32, 255);
        st.whitespaceChars(0, ' ');
        st.wordChars(33, 128);
        st.commentChar('#');
        st.eolIsSignificant(true);
        st.lowerCaseMode(true);
        root = new RuleNode();
        String tokens[] = new String[32];
        int toklen = 0;
        edu.hkust.clap.monitor.Monitor.loopBegin(367);
while (!eof) { 
edu.hkust.clap.monitor.Monitor.loopInc(367);
{
            edu.hkust.clap.monitor.Monitor.loopBegin(368);
while (!eof) { 
edu.hkust.clap.monitor.Monitor.loopInc(368);
{
                int tt = -1;
                switch(tt = st.nextToken()) {
                    case StreamTokenizer.TT_EOF:
                        eof = true;
                        if (toklen > 0) {
                            try {
                                parseRule(tokens, toklen);
                            } catch (RuleParserException ex) {
                                String msg = ("Error while parsing rule file, " + "line " + st.lineno() + ": " + ex.getMessage());
                                throw new RuleParserException(msg);
                            }
                            toklen = 0;
                        }
                        break;
                    case StreamTokenizer.TT_EOL:
                        if (toklen > 0) {
                            try {
                                parseRule(tokens, toklen);
                            } catch (RuleParserException ex) {
                                String msg = ("Error while parsing rule file, " + "line " + st.lineno() + ": " + ex.getMessage());
                                throw new RuleParserException(msg);
                            }
                            toklen = 0;
                        }
                        break;
                    case StreamTokenizer.TT_WORD:
                        if (toklen + 1 >= tokens.length) {
                            String newtok[] = new String[tokens.length + 8];
                            System.arraycopy(tokens, 0, newtok, 0, toklen);
                            tokens = newtok;
                        }
                        tokens[toklen++] = st.sval;
                        break;
                    default:
                        throw new RuleParserException("Invalid syntax, line " + st.lineno() + ".");
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(368);

        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(367);

        return root;
    }

    /**
     * Create a rule parser to parse the given input stream.
     */
    public RuleParser(InputStream in) {
        this.in = in;
    }
}
