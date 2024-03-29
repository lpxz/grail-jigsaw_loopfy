package org.w3c.jigsaw.pagecompile;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;
import org.w3c.jigsaw.frames.HTTPFrame;
import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.httpd;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.mime.MimeType;
import org.w3c.tools.resources.FileResource;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.ServerInterface;
import org.w3c.util.ObservableProperties;

/**
 * @version $Revision: 1.2 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class PageCompileFrame extends HTTPFrame {

    /**
     * debug flag
     */
    private static final boolean debug = true;

    /**
     * The special interface of Generated Frames.
     */
    static Class generatedClass = null;

    /** Will hold the increments for finding "<java>" */
    private static byte startIncrements[] = new byte[128];

    /** Will hold the increments for finding "</java>" */
    private static byte endIncrements[] = new byte[128];

    /** The start pattern */
    private static byte startPat[] = { (byte) '<', (byte) 'j', (byte) 'a', (byte) 'v', (byte) 'a' };

    /** The end pattern */
    private static byte endPat[] = { (byte) '<', (byte) '/', (byte) 'j', (byte) 'a', (byte) 'v', (byte) 'a', (byte) '>' };

    private byte[] unparsed = null;

    protected static GeneratedClassLoader classloader = null;

    protected static GeneratedClassLoader getClassLoader() {
        return classloader;
    }

    protected static void createClassLoader(File dir) {
        if (classloader == null) {
            classloader = new GeneratedClassLoader(dir);
        }
    }

    protected static GeneratedClassLoader getNewClassLoader() {
        classloader = new GeneratedClassLoader(classloader);
        return classloader;
    }

    static {
        try {
            generatedClass = Class.forName("org.w3c.jigsaw.pagecompile.GeneratedFrame");
        } catch (Exception ex) {
            throw new RuntimeException("No GeneratedFrame class found.");
        }
        edu.hkust.clap.monitor.Monitor.loopBegin(1091);
for (int i = 0; i < 128; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1091);
{
            startIncrements[i] = 5;
            endIncrements[i] = 7;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1091);

        startIncrements[(int) ('<')] = 4;
        startIncrements[(int) ('j')] = 3;
        startIncrements[(int) ('a')] = 2;
        startIncrements[(int) ('v')] = 1;
        endIncrements[(int) ('<')] = 6;
        endIncrements[(int) ('/')] = 5;
        endIncrements[(int) ('j')] = 4;
        endIncrements[(int) ('a')] = 1;
        endIncrements[(int) ('v')] = 2;
    }

    protected Segment classSegs[] = null;

    protected Segment importSegs[] = null;

    protected Segment extendsSegs[] = null;

    protected Segment implementsSegs[] = null;

    protected Segment bodySegs[] = null;

    private PageCompileProp props = null;

    protected PageCompileProp getPageCompileProps() {
        if (props == null) {
            httpd server = (httpd) getServer();
            props = (PageCompileProp) server.getPropertySet(PageCompileProp.PAGE_COMPILE_PROP_NAME);
        }
        return props;
    }

    private String gcname = null;

    protected String getGeneratedClassName() {
        updateGeneratedClassAttributes();
        return gcname;
    }

    private String packagename = null;

    protected String getGeneratedPackageName() {
        updateGeneratedClassAttributes();
        return packagename;
    }

    private String packagedClassName = null;

    protected String getPackagedClassName() {
        updateGeneratedClassAttributes();
        if (packagename == null) return gcname; else return packagename + "." + gcname;
    }

    private File gcfile = null;

    protected File getGeneratedClassFile() {
        updateGeneratedClassAttributes();
        return gcfile;
    }

    private File ccfile = null;

    private File getCompiledClassFile() {
        updateGeneratedClassAttributes();
        return ccfile;
    }

    /**
     * update classfile, package name, classname
     */
    private void updateGeneratedClassAttributes() {
        if ((gcname == null) || (gcfile == null)) {
            File dir = getPageCompileProps().getCompiledPageDirectory();
            String url = fresource.getURLPath();
            int idx = url.lastIndexOf('.');
            if (idx != -1) url = url.substring(0, idx);
            int idx2 = url.lastIndexOf('/');
            if (idx2 != -1) {
                gcname = url.substring(idx2 + 1);
                File gcdir = null;
                if (idx2 != 0) {
                    String rep = url.substring(0, idx2);
                    packagename = rep.substring(1);
                    packagename = packagename.replace('/', '.');
                    gcdir = new File(dir, rep.substring(1));
                } else {
                    gcdir = dir;
                    packagename = null;
                }
                if (!gcdir.exists()) gcdir.mkdirs();
                gcfile = new File(gcdir, gcname + ".java");
                ccfile = new File(gcdir, gcname + ".class");
            } else {
                throw new RuntimeException("Can't update generated class " + "attributes from url : " + fresource.getURLPath());
            }
        }
    }

    protected boolean classCompiled() {
        return getCompiledClassFile().exists();
    }

    private PageCompiler compiler = null;

    protected PageCompiler getCompiler() {
        try {
            String cname = getPageCompileProps().getCompilerClassName();
            Class compilerClass = Class.forName(cname);
            return (PageCompiler) compilerClass.newInstance();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Register the resource and add Properties in httpd.
     * @param resource The resource to register.
     */
    public void registerResource(FramedResource resource) {
        super.registerResource(resource);
        if (getPageCompileProps() == null) {
            synchronized (this.getClass()) {
                httpd s = (httpd) getServer();
                if (s != null) {
                    ObservableProperties props = s.getProperties();
                    s.registerPropertySet(new PageCompileProp(s));
                }
            }
        }
        File generatedClassDir = getPageCompileProps().getCompiledPageDirectory();
        createClassLoader(generatedClassDir);
    }

    protected byte[] readUnparsed() throws IOException {
        File file = fresource.getFile();
        ByteArrayOutputStream out = new ByteArrayOutputStream((int) file.length());
        FileInputStream in = new FileInputStream(file);
        byte[] buf = new byte[4096];
        int len = 0;
        edu.hkust.clap.monitor.Monitor.loopBegin(31);
while ((len = in.read(buf)) != -1) { 
edu.hkust.clap.monitor.Monitor.loopInc(31);
out.write(buf, 0, len);} 
edu.hkust.clap.monitor.Monitor.loopEnd(31);

        in.close();
        out.close();
        unparsed = out.toByteArray();
        return unparsed;
    }

    /**
     * Analogous to standard C's <code>strncmp</code>, for byte arrays.
     * (Should be in some utility package, I'll put it here for now)
     * @param ba1 the first byte array
     * @param off1 where to start in the first array
     * @param ba2 the second byte array
     * @param off2 where to start in the second array
     * @param n the length to compare up to
     * @return <strong>true</strong> if both specified parts of the arrays are
     *           equal, <strong>false</strong> if they aren't .
     */
    public static final boolean byteArrayNEquals(byte[] ba1, int off1, byte[] ba2, int off2, int n) {
        int corr = off2 - off1;
        int max = n + off1;
        edu.hkust.clap.monitor.Monitor.loopBegin(42);
for (int i = off1; i < max; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(42);
if (ba1[i] != ba2[i + corr]) return false;} 
edu.hkust.clap.monitor.Monitor.loopEnd(42);

        return true;
    }

    /**
     * Does the same as Character.isSpace, without need to cast the
     * byte into a char.
     * @param ch the character
     * @return whether or not ch is ASCII white space
     * @see java.lang.Character#isSpace
     */
    private final boolean isSpace(byte ch) {
        return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';
    }

    /**
     * isSpace or '=';
     * @param ch the character
     * @return whether or not ch is ASCII white space or '='
     */
    private final boolean isSpaceOrEqual(byte ch) {
        return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r' || ch == '=';
    }

    protected int parseType(byte unparsed[], int startParam, int endParam) {
        StringBuffer typebf = new StringBuffer(10);
        StringBuffer valbf = new StringBuffer(10);
        String value = null;
        int typeidx = startParam;
        char ch;
        edu.hkust.clap.monitor.Monitor.loopBegin(1092);
while ((typeidx <= endParam) && (isSpace(unparsed[typeidx]))) { 
edu.hkust.clap.monitor.Monitor.loopInc(1092);
typeidx++;} 
edu.hkust.clap.monitor.Monitor.loopEnd(1092);

        edu.hkust.clap.monitor.Monitor.loopBegin(1093);
while ((typeidx <= endParam) && (!isSpaceOrEqual(unparsed[typeidx]))) { 
edu.hkust.clap.monitor.Monitor.loopInc(1093);
{
            ch = (char) unparsed[typeidx++];
            typebf.append(ch);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1093);

        if (!(typebf.toString().equalsIgnoreCase("type"))) return Segment.getDefaultType();
        edu.hkust.clap.monitor.Monitor.loopBegin(1094);
while ((typeidx <= endParam) && (isSpaceOrEqual(unparsed[typeidx]))) { 
edu.hkust.clap.monitor.Monitor.loopInc(1094);
typeidx++;} 
edu.hkust.clap.monitor.Monitor.loopEnd(1094);

        edu.hkust.clap.monitor.Monitor.loopBegin(1095);
while ((typeidx <= endParam) && (!isSpace(unparsed[typeidx]))) { 
edu.hkust.clap.monitor.Monitor.loopInc(1095);
{
            ch = (char) unparsed[typeidx++];
            valbf.append(ch);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1095);

        return Segment.getType(valbf.toString());
    }

    protected Segment[] parse() {
        unparsed = null;
        try {
            unparsed = readUnparsed();
        } catch (IOException ex) {
            return null;
        }
        Vector buildSegments = new Vector(20);
        int byteIdx = 0;
        int StartSeg = 0;
        int startParam = 0;
        int type = -1;
        byte ch;
        do {
            byteIdx += 4;
            edu.hkust.clap.monitor.Monitor.loopBegin(1096);
while (byteIdx < unparsed.length) { 
edu.hkust.clap.monitor.Monitor.loopInc(1096);
{
                if ((ch = unparsed[byteIdx]) == (byte) 'a') {
                    if (byteArrayNEquals(unparsed, byteIdx - 4, startPat, 0, 4)) {
                        break;
                    }
                }
                byteIdx += startIncrements[ch >= 0 ? ch : 0];
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1096);

            if (++byteIdx >= unparsed.length) break;
            if (byteIdx > 6) buildSegments.addElement(new Segment(StartSeg, byteIdx - 6));
            startParam = byteIdx;
            edu.hkust.clap.monitor.Monitor.loopBegin(1097);
while (byteIdx < unparsed.length) { 
edu.hkust.clap.monitor.Monitor.loopInc(1097);
{
                if (unparsed[byteIdx] == (byte) '>') break;
                byteIdx++;
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1097);

            if (++byteIdx >= unparsed.length) break;
            type = parseType(unparsed, startParam, byteIdx - 2);
            StartSeg = byteIdx;
            byteIdx += 6;
            edu.hkust.clap.monitor.Monitor.loopBegin(1098);
while (byteIdx < unparsed.length) { 
edu.hkust.clap.monitor.Monitor.loopInc(1098);
{
                if ((ch = unparsed[byteIdx]) == (byte) '>') {
                    if (byteArrayNEquals(unparsed, byteIdx - 6, endPat, 0, 6)) {
                        break;
                    }
                }
                byteIdx += endIncrements[ch >= 0 ? ch : 0];
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1098);

            if (++byteIdx >= unparsed.length) break;
            buildSegments.addElement(new Segment(StartSeg, byteIdx - 8, type));
            StartSeg = byteIdx;
        } while (byteIdx < unparsed.length);
        buildSegments.addElement(new Segment(StartSeg, unparsed.length));
        Segment[] segs = new Segment[buildSegments.size()];
        buildSegments.copyInto(segs);
        return segs;
    }

    private Segment[] getSegmentArrayFromVector(Vector V) {
        int size = V.size();
        if (size < 1) return null;
        Segment[] segs = new Segment[size];
        V.copyInto(segs);
        return segs;
    }

    protected void separateSegments(Segment segments[]) {
        Vector classV = new Vector(3);
        Vector importV = new Vector(3);
        Vector extendsV = new Vector(1);
        Vector implementsV = new Vector(1);
        Vector bodyV = new Vector(5);
        edu.hkust.clap.monitor.Monitor.loopBegin(1099);
for (int i = 0; i < segments.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1099);
{
            switch(segments[i].getType()) {
                case Segment.CLASS:
                    classV.addElement(segments[i]);
                    break;
                case Segment.IMPORT:
                    importV.addElement(segments[i]);
                    break;
                case Segment.EXTENDS:
                    extendsV.addElement(segments[i]);
                    break;
                case Segment.IMPLEMENTS:
                    implementsV.addElement(segments[i]);
                    break;
                case Segment.CODE:
                case Segment.PRINT:
                case Segment.TEXT:
                default:
                    bodyV.addElement(segments[i]);
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1099);

        classSegs = getSegmentArrayFromVector(classV);
        importSegs = getSegmentArrayFromVector(importV);
        extendsSegs = getSegmentArrayFromVector(extendsV);
        implementsSegs = getSegmentArrayFromVector(implementsV);
        bodySegs = getSegmentArrayFromVector(bodyV);
    }

    protected byte[] getPackageStatement() {
        if (getGeneratedPackageName() != null) return (new String("package " + getGeneratedPackageName() + ";\n\n")).getBytes();
        return null;
    }

    protected byte[] getClassDeclarationStatement() {
        return (new String("public class " + getGeneratedClassName() + " extends ")).getBytes();
    }

    protected String getFilePath() {
        String path = fresource.getFile().getAbsolutePath();
        StringBuffer filepath = new StringBuffer();
        int idx = path.indexOf('\\');
        if (idx == -1) return path;
        edu.hkust.clap.monitor.Monitor.loopBegin(1100);
while ((idx = path.indexOf('\\')) != -1) { 
edu.hkust.clap.monitor.Monitor.loopInc(1100);
{
            filepath.append(path.substring(0, idx));
            filepath.append("\\\\");
            path = path.substring(idx + 1);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1100);

        filepath.append(path);
        return filepath.toString();
    }

    protected byte[] getGetMethodDeclaration() {
        StringBuffer decl = new StringBuffer(256);
        decl.append("    protected void get(org.w3c.jigsaw.http.Request " + "request,\n");
        decl.append("                       org.w3c.jigsaw.http.Reply " + "reply,\n");
        decl.append("                       " + "org.w3c.jigsaw.pagecompile.PageCompileOutputStream " + "out)\n");
        decl.append("        throws java.io.IOException\n");
        decl.append("    {\n");
        decl.append("        org.w3c.jigsaw.pagecompile.PageCompileFile " + "_file = " + "new org.w3c.jigsaw.pagecompile.PageCompileFile(\"");
        decl.append(getFilePath() + "\");\n");
        return decl.toString().getBytes();
    }

    protected byte[] getClassEnd() {
        byte end[] = { (byte) ' ', (byte) ' ', (byte) ' ', (byte) ' ', (byte) '}', (byte) '\n', (byte) '}' };
        return end;
    }

    protected byte[] getSegmentBytes(Segment seg) {
        if (seg.getType() == Segment.PRINT) {
            byte part1[] = (new String("        out.print(String.valueOf(")).getBytes();
            byte part3[] = { (byte) ')', (byte) ')', (byte) ';', (byte) '\n' };
            int seglen = seg.end - seg.start + 1;
            int len = part1.length + seglen + part3.length;
            byte statement[] = new byte[len];
            System.arraycopy(part1, 0, statement, 0, part1.length);
            System.arraycopy(unparsed, seg.start, statement, part1.length, seglen);
            System.arraycopy(part3, 0, statement, part1.length + seglen, part3.length);
            return statement;
        } else if (seg.getType() != Segment.TEXT) {
            int len = seg.end - seg.start + 1;
            byte segbytes[] = new byte[len];
            System.arraycopy(unparsed, seg.start, segbytes, 0, len);
            return segbytes;
        } else {
            return (new String("        _file.writeBytes(" + seg.start + "," + seg.end + ",out);\n")).getBytes();
        }
    }

    /**
     * Generate the frame.
     * @param request the incomming request.
     * @exception ResourceException if a fatal error occurs.
     * @exception ProtocolException if compilation failed
     */
    public GeneratedFrame generateFrame(Request request) throws ResourceException, ProtocolException {
        separateSegments(parse());
        File classFile = getGeneratedClassFile();
        if (classFile.exists()) classFile.delete();
        if (getCompiledClassFile().exists()) getCompiledClassFile().delete();
        try {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(classFile));
            byte[] pack = getPackageStatement();
            if (pack != null) out.write(pack);
            if (importSegs != null) {
                edu.hkust.clap.monitor.Monitor.loopBegin(1101);
for (int i = 0; i < importSegs.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1101);
out.write(getSegmentBytes(importSegs[i]));} 
edu.hkust.clap.monitor.Monitor.loopEnd(1101);

                out.write('\n');
            }
            out.write(getClassDeclarationStatement());
            if (extendsSegs != null) {
                if (extendsSegs.length > 0) out.write(getSegmentBytes(extendsSegs[0]));
                out.write(' ');
            } else {
                out.write((new String("org.w3c.jigsaw.pagecompile.GeneratedFrame ")).getBytes());
            }
            if (implementsSegs != null) {
                if (implementsSegs.length > 0) {
                    byte imp[] = { (byte) 'i', (byte) 'm', (byte) 'p', (byte) 'l', (byte) 'e', (byte) 'm', (byte) 'e', (byte) 'n', (byte) 't', (byte) 's', (byte) ' ' };
                    out.write(imp);
                    out.write(getSegmentBytes(implementsSegs[0]));
                }
            }
            out.write(' ');
            out.write('{');
            out.write('\n');
            out.write('\n');
            if (classSegs != null) {
                edu.hkust.clap.monitor.Monitor.loopBegin(1102);
for (int i = 0; i < classSegs.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1102);
out.write(getSegmentBytes(classSegs[i]));} 
edu.hkust.clap.monitor.Monitor.loopEnd(1102);

                out.write('\n');
            }
            out.write(getGetMethodDeclaration());
            if (bodySegs != null) edu.hkust.clap.monitor.Monitor.loopBegin(1103);
for (int i = 0; i < bodySegs.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1103);
out.write(getSegmentBytes(bodySegs[i]));} 
edu.hkust.clap.monitor.Monitor.loopEnd(1103);

            out.write(getClassEnd());
            out.flush();
            out.close();
        } catch (IOException ex) {
        }
        PageCompiler compiler = getCompiler();
        if (compiler == null) throw new ResourceException("Can't load compiler: " + getPageCompileProps().getCompilerClassName());
        String args[] = { classFile.getAbsolutePath() };
        PageCompileOutputStream out = new PageCompileOutputStream();
        if (!compiler.compile(args, out)) {
            if (out.size() > 0) {
                Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
                error.setStream(out.getInputStream());
                error.setContentLength(out.size());
                error.setContentType(MimeType.TEXT_PLAIN);
                throw new HTTPException(error);
            }
        }
        try {
            GeneratedClassLoader loader = getClassLoader();
            if (loader.classChanged(getPackagedClassName())) loader = getNewClassLoader();
            Class generatedClass = loader.loadClass(getPackagedClassName());
            GeneratedFrame gframe = (GeneratedFrame) generatedClass.newInstance();
            return gframe;
        } catch (Exception ex) {
            throw new ResourceException(ex.getMessage());
        }
    }

    protected void registerNewGeneratedFrame(Request request) throws ResourceException, ProtocolException {
        ResourceFrame frames[] = collectFrames(generatedClass);
        if (frames != null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(1104);
for (int i = 0; i < frames.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1104);
unregisterFrame(frames[i]);} 
edu.hkust.clap.monitor.Monitor.loopEnd(1104);

        }
        GeneratedFrame frame = null;
        frame = generateFrame(request);
        if (frame != null) {
            Hashtable defs = new Hashtable(3);
            defs.put("identifier", "generated-frame");
            defs.put("content-type", MimeType.TEXT_HTML);
            registerFrame(frame, defs);
        }
    }

    protected void checkContent(Request request) throws ResourceException, ProtocolException {
        if (fresource != null) {
            File file = fresource.getFile();
            long lmt = file.lastModified();
            long cmt = fresource.getFileStamp();
            if ((!classCompiled()) || ((cmt < 0) || (cmt < lmt))) {
                fresource.updateFileAttributes();
                registerNewGeneratedFrame(request);
            }
        }
    }

    /**
     * Makes sure that checkContent() is called on _any_ HTTP method,
     * so that the internal representation of commands is always consistent.
     * @param request The HTTPRequest
     * @return a ReplyInterface instance
     * @exception ProtocolException If processing the request failed.
     * @exception ResourceException If this resource got a fatal error.
     */
    public ReplyInterface perform(RequestInterface request) throws ProtocolException, ResourceException {
        if (!checkRequest(request)) return null;
        checkContent((Request) request);
        return super.perform(request);
    }
}
