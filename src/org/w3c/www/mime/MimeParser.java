package org.w3c.www.mime;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import org.w3c.www.http.HttpAccept;
import org.w3c.www.http.HttpAcceptCharset;
import org.w3c.www.http.HttpAcceptLanguage;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpRequestMessage;

/**
 * The MimeParser class parses an input MIME stream. 
 */
public class MimeParser {

    protected int ch = -1;

    protected InputStream input = null;

    protected byte buffer[] = new byte[128];

    protected int bsize = 0;

    /**
     * The factory used to create new MIME header holders.
     */
    protected MimeParserFactory factory = null;

    protected void expect(int car) throws MimeParserException, IOException {
        if (car != ch) {
            String sc = (new Character((char) car)).toString();
            String se = (new Character((char) ch)).toString();
            throw new MimeParserException("expecting " + sc + "(" + car + ")" + " got " + se + "(" + ch + ")\n" + "context: " + new String(buffer, 0, 0, bsize) + "\n");
        }
        ch = input.read();
    }

    protected void skipSpaces() throws MimeParserException, IOException {
        edu.hkust.clap.monitor.Monitor.loopBegin(1121);
while ((ch == ' ') || (ch == '\t')) { 
edu.hkust.clap.monitor.Monitor.loopInc(1121);
ch = input.read();} 
edu.hkust.clap.monitor.Monitor.loopEnd(1121);

    }

    protected final void append(int c) {
        if (bsize + 1 >= buffer.length) {
            byte nb[] = new byte[buffer.length * 2];
            System.arraycopy(buffer, 0, nb, 0, buffer.length);
            buffer = nb;
        }
        buffer[bsize++] = (byte) c;
    }

    protected String parse822HeaderName() throws MimeParserException, IOException {
        bsize = 0;
        edu.hkust.clap.monitor.Monitor.loopBegin(1122);
while ((ch >= 32) && (ch != ':')) { 
edu.hkust.clap.monitor.Monitor.loopInc(1122);
{
            append((char) ch);
            ch = input.read();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1122);

        skipSpaces();
        expect(':');
        if (bsize <= 0) throw new MimeParserException("expected a header name.");
        return new String(buffer, 0, 0, bsize);
    }

    protected void parse822HeaderBody() throws MimeParserException, IOException {
        parse822HeaderBody(true);
    }

    protected void parse822HeaderBodyLenient() throws MimeParserException, IOException {
        bsize = 0;
        skipSpaces();
        boolean quoted = false;
        edu.hkust.clap.monitor.Monitor.loopBegin(1123);
        loop:
while (true) { 
edu.hkust.clap.monitor.Monitor.loopInc(1123);
{
            switch(ch) {
                case -1:
                    break loop;
                case '\r':
                    if ((ch = input.read()) != '\n') {
                        append('\r');
                        continue;
                    }
                case '\n':
                    switch(ch = input.read()) {
                        case ' ':
                        case '\t':
                            do {
                                ch = input.read();
                            } while ((ch == ' ') || (ch == '\t'));
                            if ((ch == '\r') || (ch == '\n')) {
                                continue;
                            }
                            append(' ');
                            append(ch);
                            break;
                        default:
                            break loop;
                    }
                    break;
                case '\\':
                    append((char) ch);
                    if (quoted) {
                        ch = input.read();
                        append((char) ch);
                    }
                    break;
                case '\"':
                    quoted = !quoted;
                default:
                    append((char) ch);
                    break;
            }
            ch = input.read();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1123);

        return;
    }

    protected void parse822HeaderBody(boolean lenient) throws MimeParserException, IOException {
        if (lenient) {
            parse822HeaderBodyLenient();
        } else {
            parse822HeaderBodyStrict();
        }
    }

    protected void parse822HeaderBodyStrict() throws MimeParserException, IOException {
        bsize = 0;
        skipSpaces();
        boolean quoted = false;
        boolean gotr = false;
        edu.hkust.clap.monitor.Monitor.loopBegin(1124);
        loop: 
while (true) { 
edu.hkust.clap.monitor.Monitor.loopInc(1124);
{
            switch(ch) {
                case -1:
                    break loop;
                case '\r':
                    if ((ch = input.read()) != '\n') {
                        append('\r');
                        continue;
                    }
                    gotr = true;
                    continue;
                case '\n':
                    if (quoted) {
                        if (gotr) {
                            append('\r');
                            append('\n');
                            break;
                        }
                        throw new MimeParserException("MimeParser: " + "\\n not allowed in " + "quoted string");
                    }
                    if (gotr) {
                        switch(ch = input.read()) {
                            case ' ':
                            case '\t':
                                do {
                                    ch = input.read();
                                } while ((ch == ' ') || (ch == '\t'));
                                if (ch == '\r') {
                                    continue;
                                }
                                append(' ');
                                append(ch);
                                gotr = false;
                                break;
                            default:
                                break loop;
                        }
                    } else {
                        append('\n');
                    }
                    break;
                case '\\':
                    gotr = false;
                    append((char) ch);
                    if (quoted) {
                        ch = input.read();
                        append((char) ch);
                    }
                    break;
                case '\"':
                    gotr = false;
                    quoted = !quoted;
                default:
                    if (quoted) {
                        if ((ch < 32) && (ch != '\t')) {
                            throw new MimeParserException("MimeParser: " + "CTRL not allowed in " + "quoted string");
                        }
                    }
                    gotr = false;
                    append((char) ch);
                    break;
            }
            ch = input.read();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1124);

        return;
    }

    protected String parseToken(boolean lower) throws MimeParserException, IOException {
        bsize = 0;
while (true) { 
{
            switch(ch) {
                case -1:
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                case '(':
                case ')':
                case '<':
                case '>':
                case '@':
                case ',':
                case ';':
                case ':':
                case '\\':
                case '\"':
                case '/':
                case '[':
                case ']':
                case '?':
                case '=':
                case '{':
                case '}':
                case ' ':
                    return new String(buffer, 0, 0, bsize);
                default:
                    append((char) (lower ? Character.toLowerCase((char) ch) : ch));
            }
            ch = input.read();
        }} 

    }

    protected void parse822Headers(MimeHeaderHolder msg, boolean lenient) throws MimeParserException, IOException {
while (true) { 
{
            if (ch == '\r') {
                if ((ch = input.read()) == '\n') return;
            } else if (lenient && (ch == '\n')) {
                return;
            }
            String name = parse822HeaderName();
            skipSpaces();
            parse822HeaderBody(lenient);
            msg.notifyHeader(name, buffer, 0, bsize);
        }} 

    }

    protected void parse822Headers(MimeHeaderHolder msg) throws MimeParserException, IOException {
        parse822Headers(msg, true);
    }

    /**
     * parse the stream, and create a MimeHeaderHolder containing all
     * the parsed headers, note that invalid headers will trigger an exception
     * in stirct mode, and will just be removed in lenient mode
     * @param lenient, a boolean, true if we want to be kind with bad people
     * @return a MimeHeaderHolder instance containing the aprsed headers
     */
    public MimeHeaderHolder parse(boolean lenient) throws MimeParserException, IOException {
        MimeHeaderHolder msg = factory.createHeaderHolder(this);
        ch = input.read();
        cached = true;
        if (!msg.notifyBeginParsing(this)) {
            if (!cached) ch = input.read();
            if (lenient) {
                try {
                    parse822Headers(msg, lenient);
                } catch (MimeParserException ex) {
                }
            } else {
                parse822Headers(msg, lenient);
            }
        }
        msg.notifyEndParsing(this);
        return msg;
    }

    /**
     * parse the stream, and create a MimeHeaderHolder containing all
     * the parsed headers, in lenient mode
     * Always be lenient by default (general rule is: be lenient in what you
     * accept conservative with what you generate).
     */
    public MimeHeaderHolder parse() throws MimeParserException, IOException {
        return parse(true);
    }

    boolean cached = false;

    public int read() throws IOException {
        if (cached) cached = false; else ch = input.read();
        return ch;
    }

    public void unread(int ch) {
        if (cached) throw new RuntimeException("cannot unread more then once !");
        this.ch = ch;
        cached = true;
    }

    /**
     * Get the message body, as an input stream.
     * @return The input stream used by the parser to get data, after 
     * a call to <code>parse</code>, this input stream contains exactly
     * the body of the message.
     */
    public InputStream getInputStream() {
        return input;
    }

    /**
     * Create an instance of the MIMEParser class.
     * @param in The input stream to be parsed as a MIME stream.
     * @param factory The factory used to create MIME header holders.
     */
    public MimeParser(InputStream input, MimeParserFactory factory) {
        this.input = input;
        this.factory = factory;
    }

    /**
     * Debuging
     */
    public static void main(String args[]) {
        try {
            String factoryname = args[0];
            String filename = args[1];
            MimeParserFactory f = null;
            f = (MimeParserFactory) Class.forName(factoryname).newInstance();
            InputStream in = (new BufferedInputStream(new FileInputStream(filename)));
            MimeParser p = new MimeParser(in, f);
            HttpRequestMessage m = (HttpRequestMessage) p.parse();
            HttpAccept a[] = m.getAccept();
            edu.hkust.clap.monitor.Monitor.loopBegin(1127);
for (int i = 0; i < a.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1127);
{
                System.out.println("accept: " + a[i].getMimeType());
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1127);

            HttpAcceptLanguage l[] = m.getAcceptLanguage();
            edu.hkust.clap.monitor.Monitor.loopBegin(1128);
for (int i = 0; i < l.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1128);
{
                System.out.println("accept-lang: " + l[i].getLanguage());
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1128);

            HttpAcceptCharset c[] = m.getAcceptCharset();
            edu.hkust.clap.monitor.Monitor.loopBegin(1129);
for (int i = 0; i < c.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1129);
{
                System.out.println("accept-charset: " + c[i].getCharset());
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1129);

            m.emit(System.out);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("MimeParser <factory> <file>");
        }
    }
}
