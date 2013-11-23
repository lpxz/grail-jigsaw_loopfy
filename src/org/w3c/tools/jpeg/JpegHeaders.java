package org.w3c.tools.jpeg;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Vector;
import java.util.Hashtable;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 * jpeg reading code adapted from rdjpgcom from The Independent JPEG Group 
 */
public class JpegHeaders implements Jpeg {

    protected File jpegfile = null;

    protected InputStream in = null;

    protected Vector vcom = null;

    protected Vector vacom[] = new Vector[16];

    protected String comments[] = null;

    protected byte appcomments[][] = null;

    protected Exif exif = null;

    protected int compression = -1;

    protected int bitsPerPixel = -1;

    protected int height = -1;

    protected int width = -1;

    protected int numComponents = -1;

    /**
     * Get the comments extracted from the jpeg stream
     * @return an array of Strings
     */
    public String[] getComments() {
        if (comments == null) {
            comments = new String[vcom.size()];
            vcom.copyInto(comments);
        }
        return comments;
    }

    /**
     * Get the application specific values extracted from the jpeg stream
     * @return an array of Strings
     */
    public String[] getStringAPPComments(int marker) {
        if ((marker < M_APP0) || (marker > M_APP15)) {
            return null;
        }
        int idx = marker - M_APP0;
        int asize = vacom[idx].size();
        if (appcomments == null) {
            appcomments = new byte[asize][];
            vacom[idx].copyInto(appcomments);
        }
        String strappcomments[] = new String[asize];
        edu.hkust.clap.monitor.Monitor.loopBegin(125);
for (int i = 0; i < asize; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(125);
{
            try {
                strappcomments[i] = new String(appcomments[i], "ISO-8859-1");
            } catch (UnsupportedEncodingException ex) {
            }
            ;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(125);

        return strappcomments;
    }

    /**
     * An old default, it gets only the M_APP12
     */
    public String[] getStringAppComments() {
        return getStringAPPComments(M_APP12);
    }

    /**
     * A get XMP in APP1
     */
    public String getXMP() throws IOException, JpegException {
        String magicstr = "W5M0MpCehiHzreSzNTczkc9d";
        char magic[] = magicstr.toCharArray();
        String magicendstr = "<?xpacket";
        char magicend[] = magicendstr.toCharArray();
        char c;
        int length;
        int h = 0, i = 0, j = 0, k;
        char buf[] = new char[256];
        String app1markers[] = getStringAPPComments(M_APP1);
        String app1marker = new String();
        boolean found = false;
        edu.hkust.clap.monitor.Monitor.loopBegin(126);
for (h = 0; h < app1markers.length; h++) { 
edu.hkust.clap.monitor.Monitor.loopInc(126);
{
            if (found == false && app1markers[h].indexOf(magicstr) != -1) {
                found = true;
                app1marker = app1marker.concat(app1markers[h]);
            } else if (found == true) {
                app1marker = app1marker.concat(app1markers[h]);
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(126);

        StringReader app1reader = new StringReader(app1marker);
        StringBuffer sbuf = new StringBuffer();
        length = read2bytes(app1reader);
        if (length < 2) throw new JpegException("Erroneous JPEG marker length");
        length -= 2;
        app1reader = new StringReader(app1marker);
        edu.hkust.clap.monitor.Monitor.loopBegin(127);
while (length > 0 && j < magic.length) { 
edu.hkust.clap.monitor.Monitor.loopInc(127);
{
            buf[i] = (char) (app1reader.read());
            if (buf[i] == -1) throw new JpegException("Premature EOF in JPEG file");
            if (buf[i] == magic[j]) {
                j++;
            } else {
                j = 0;
            }
            i = (i + 1) % 100;
            length--;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(127);

        if (j == magic.length) {
            k = i;
            do {
                i = (i + 100 - 1) % 100;
            } while (buf[i] != '<' || buf[(i + 1) % 100] != '?' || buf[(i + 2) % 100] != 'x' || buf[(i + 3) % 100] != 'p');
            edu.hkust.clap.monitor.Monitor.loopBegin(128);
for (; i != k; i = (i + 1) % 100) { 
edu.hkust.clap.monitor.Monitor.loopInc(128);
sbuf.append(buf[i]);} 
edu.hkust.clap.monitor.Monitor.loopEnd(128);

            j = 0;
            edu.hkust.clap.monitor.Monitor.loopBegin(129);
while (length > 0 && j < magicend.length) { 
edu.hkust.clap.monitor.Monitor.loopInc(129);
{
                c = (char) (app1reader.read());
                if (c == -1) throw new JpegException("Premature EOF in JPEG file");
                if (c == magicend[j]) j++; else j = 0;
                sbuf.append(c);
                length--;
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(129);

            edu.hkust.clap.monitor.Monitor.loopBegin(130);
while (length > 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(130);
{
                c = (char) (app1reader.read());
                if (c == -1) throw new JpegException("Premature EOF in JPEG file");
                sbuf.append(c);
                length--;
                if (c == '>') break;
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(130);

        }
        edu.hkust.clap.monitor.Monitor.loopBegin(131);
while (length > 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(131);
{
            app1reader.read();
            length--;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(131);

        return (sbuf.toString());
    }

    public byte[][] getByteArrayAPPComment() {
        return null;
    }

    /**
     * The old way of extracting comments in M_APP12 markers
     * @deprecated use getStringAppComments instead
     */
    public String[] getAppComments() {
        return getStringAppComments();
    }

    protected int scanHeaders() throws IOException, JpegException {
        int marker;
        vcom = new Vector(1);
        vacom = new Vector[16];
        edu.hkust.clap.monitor.Monitor.loopBegin(132);
for (int i = 0; i < 16; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(132);
{
            vacom[i] = new Vector(1);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(132);

        if (firstMarker() != M_SOI) throw new JpegException("Expected SOI marker first");
while (true) { 
{
            marker = nextMarker();
            switch(marker) {
                case M_SOF0:
                case M_SOF1:
                case M_SOF2:
                case M_SOF3:
                case M_SOF5:
                case M_SOF6:
                case M_SOF7:
                case M_SOF9:
                case M_SOF10:
                case M_SOF11:
                case M_SOF13:
                case M_SOF14:
                case M_SOF15:
                    compression = marker;
                    readImageInfo();
                    break;
                case M_SOS:
                    skipVariable();
                    updateExif();
                    return marker;
                case M_EOI:
                    updateExif();
                    return marker;
                case M_COM:
                    vcom.addElement(new String(processComment(), "ISO-8859-1"));
                    break;
                case M_APP0:
                case M_APP1:
                case M_APP2:
                case M_APP3:
                case M_APP4:
                case M_APP5:
                case M_APP6:
                case M_APP7:
                case M_APP8:
                case M_APP9:
                case M_APP10:
                case M_APP11:
                case M_APP12:
                case M_APP13:
                case M_APP14:
                case M_APP15:
                    byte data[] = processComment();
                    vacom[marker - M_APP0].addElement(data);
                    if (marker == M_APP1) {
                        if (exif != null) {
                            exif.parseExif(data);
                        }
                    }
                    break;
                default:
                    skipVariable();
                    break;
            }
        }} 

    }

    /** Update the EXIF to include the intrinsic values */
    protected void updateExif() {
        if (exif == null) {
            return;
        }
        if (compression >= 0) {
            switch(compression) {
                case -1:
                    break;
                case M_SOF0:
                    exif.setCompression("Baseline");
                    break;
                case M_SOF1:
                    exif.setCompression("Extended sequential");
                    break;
                case M_SOF2:
                    exif.setCompression("Progressive");
                    break;
                case M_SOF3:
                    exif.setCompression("Lossless");
                    break;
                case M_SOF5:
                    exif.setCompression("Differential sequential");
                    break;
                case M_SOF6:
                    exif.setCompression("Differential progressive");
                    break;
                case M_SOF7:
                    exif.setCompression("Differential lossless");
                    break;
                case M_SOF9:
                    exif.setCompression("Extended sequential, arithmetic coding");
                    break;
                case M_SOF10:
                    exif.setCompression("Progressive, arithmetic coding");
                    break;
                case M_SOF11:
                    exif.setCompression("Lossless, arithmetic coding");
                    break;
                case M_SOF13:
                    exif.setCompression("Differential sequential, arithmetic coding");
                    break;
                case M_SOF14:
                    exif.setCompression("Differential progressive, arithmetic coding");
                    break;
                case M_SOF15:
                    exif.setCompression("Differential lossless, arithmetic coding");
                    break;
                default:
                    exif.setCompression("Unknown" + compression);
            }
        }
        if (bitsPerPixel >= 0) {
            exif.setBPP(bitsPerPixel);
        }
        if (height >= 0) {
            exif.setHeight(height);
        }
        if (width >= 0) {
            exif.setWidth(width);
        }
        if (numComponents >= 0) {
            exif.setNumCC(numComponents);
        }
    }

    protected byte[] processComment() throws IOException, JpegException {
        int length;
        length = read2bytes();
        if (length < 2) throw new JpegException("Erroneous JPEG marker length");
        length -= 2;
        StringBuffer buffer = new StringBuffer(length);
        byte comment[] = new byte[length];
        int got, pos;
        pos = 0;
        edu.hkust.clap.monitor.Monitor.loopBegin(134);
while (length > 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(134);
{
            got = in.read(comment, pos, length);
            if (got < 0) throw new JpegException("EOF while reading jpeg comment");
            pos += got;
            length -= got;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(134);

        return comment;
    }

    protected int read2bytes() throws IOException, JpegException {
        int c1, c2;
        c1 = in.read();
        if (c1 == -1) throw new JpegException("Premature EOF in JPEG file");
        c2 = in.read();
        if (c2 == -1) throw new JpegException("Premature EOF in JPEG file");
        return (((int) c1) << 8) + ((int) c2);
    }

    protected int read2bytes(StringReader sr) throws IOException, JpegException {
        int c1, c2;
        c1 = sr.read();
        if (c1 == -1) throw new JpegException("Premature EOF in JPEG file");
        c2 = sr.read();
        if (c2 == -1) throw new JpegException("Premature EOF in JPEG file");
        return (((int) c1) << 8) + ((int) c2);
    }

    /**
     * skip the body after a marker
     */
    protected void skipVariable() throws IOException, JpegException {
        long len = (long) read2bytes() - 2;
        if (len < 0) throw new JpegException("Erroneous JPEG marker length");
        edu.hkust.clap.monitor.Monitor.loopBegin(135);
while (len > 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(135);
{
            long saved = in.skip(len);
            if (saved < 0) throw new IOException("Error while reading jpeg stream");
            len -= saved;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(135);

    }

    /**
     * read the image info then the section
     */
    protected void readImageInfo() throws IOException, JpegException {
        long len = (long) read2bytes() - 2;
        if (len < 0) throw new JpegException("Erroneous JPEG marker length");
        bitsPerPixel = in.read();
        len--;
        height = read2bytes();
        len -= 2;
        width = read2bytes();
        len -= 2;
        numComponents = in.read();
        len--;
        edu.hkust.clap.monitor.Monitor.loopBegin(135);
while (len > 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(135);
{
            long saved = in.skip(len);
            if (saved < 0) throw new IOException("Error while reading jpeg stream");
            len -= saved;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(135);

    }

    protected int firstMarker() throws IOException, JpegException {
        int c1, c2;
        c1 = in.read();
        c2 = in.read();
        if (c1 != 0xFF || c2 != M_SOI) throw new JpegException("Not a JPEG file");
        return c2;
    }

    protected int nextMarker() throws IOException {
        int discarded_bytes = 0;
        int c;
        c = in.read();
        edu.hkust.clap.monitor.Monitor.loopBegin(136);
while (c != 0xFF) { 
edu.hkust.clap.monitor.Monitor.loopInc(136);
c = in.read();} 
edu.hkust.clap.monitor.Monitor.loopEnd(136);

        do {
            c = in.read();
        } while (c == 0xFF);
        return c;
    }

    /**
     * get the headers out of a file, ignoring EXIF
     */
    public JpegHeaders(File jpegfile) throws FileNotFoundException, JpegException, IOException {
        parseJpeg(jpegfile, null);
    }

    /**
     * get the headers out of a file, including EXIF
     */
    public JpegHeaders(File jpegfile, Exif exif) throws FileNotFoundException, JpegException, IOException {
        parseJpeg(jpegfile, exif);
    }

    /**
     * get the headers out of a stream, ignoring EXIF
     */
    public JpegHeaders(InputStream in) throws JpegException, IOException {
        this.in = in;
        scanHeaders();
    }

    /**
     * get the headers out of a stream, including EXIF
     */
    public JpegHeaders(InputStream in, Exif exif) throws JpegException, IOException {
        this.exif = exif;
        this.in = in;
        scanHeaders();
    }

    protected void parseJpeg(File jpegfile, Exif exif) throws FileNotFoundException, JpegException, IOException {
        this.exif = exif;
        this.jpegfile = jpegfile;
        this.in = new BufferedInputStream(new FileInputStream(jpegfile));
        try {
            scanHeaders();
        } finally {
            try {
                in.close();
            } catch (Exception ex) {
            }
            ;
        }
    }

    public static void main(String args[]) {
        try {
            JpegHeaders headers = new JpegHeaders(new File(args[0]));
            String comments[] = headers.getComments();
            if (comments != null) {
                edu.hkust.clap.monitor.Monitor.loopBegin(137);
for (int i = 0; i < comments.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(137);
System.out.println(comments[i]);} 
edu.hkust.clap.monitor.Monitor.loopEnd(137);

            }
            System.out.println(headers.getXMP());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
