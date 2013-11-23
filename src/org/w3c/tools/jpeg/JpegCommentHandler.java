package org.w3c.tools.jpeg;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Vector;

public class JpegCommentHandler {

    protected File jpegfile;

    protected InputStream in;

    /**
     * Get this image reader
     */
    public Reader getReader() throws IOException, JpegException {
        return new StringReader(getComment());
    }

    public String getComment() throws IOException, JpegException {
        JpegHeaders jpeghead = new JpegHeaders(in);
        StringBuffer sb = new StringBuffer();
        String comms[] = jpeghead.getComments();
        edu.hkust.clap.monitor.Monitor.loopBegin(196);
for (int i = 0; i < comms.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(196);
{
            sb.append(comms[i]);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(196);

        return sb.toString();
    }

    /**
     * Get this image writer
     */
    public Writer getOutputStreamWriter(OutputStream out, String enc) throws UnsupportedEncodingException {
        return new JpegCommentWriter(out, in, enc);
    }

    /**
     * Get this image writer
     */
    public Writer getOutputStreamWriter(OutputStream out) {
        return new JpegCommentWriter(out, in);
    }

    /**
     * create it out of a File
     */
    public JpegCommentHandler(File jpegfile) throws FileNotFoundException {
        this.in = new BufferedInputStream(new FileInputStream(jpegfile));
        this.jpegfile = jpegfile;
    }

    /**
     * create it from an input stream
     */
    public JpegCommentHandler(InputStream in) {
        this.in = in;
    }
}
