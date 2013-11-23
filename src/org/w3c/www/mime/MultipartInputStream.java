package org.w3c.www.mime;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A class to handle multipart MIME input streams. See RC 1521.
 * This class handles multipart input streams, as defined by the RFC 1521. 
 * It prvides a sequential interface to all MIME parts, and for each part
 * it delivers a suitable InputStream for getting its body.
 */
public class MultipartInputStream extends InputStream {

    InputStream in = null;

    byte boundary[] = null;

    byte buffer[] = null;

    boolean partEnd = false;

    boolean fileEnd = false;

    private final boolean readBoundaryBytes() throws IOException {
        int pos = 0;
        edu.hkust.clap.monitor.Monitor.loopBegin(1075);
while (pos < buffer.length) { 
edu.hkust.clap.monitor.Monitor.loopInc(1075);
{
            int got = in.read(buffer, pos, buffer.length - pos);
            if (got < 0) return false;
            pos += got;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1075);

        return true;
    }

    protected boolean skipToBoundary() throws IOException {
        int ch = in.read();
        edu.hkust.clap.monitor.Monitor.loopBegin(1076);
        skip: 
while (ch != -1) { 
edu.hkust.clap.monitor.Monitor.loopInc(1076);
{
            if (ch != '-') {
                ch = in.read();
                continue;
            }
            if ((ch = in.read()) != '-') continue;
            in.mark(boundary.length);
            if (!readBoundaryBytes()) {
                in.reset();
                ch = in.read();
                continue skip;
            }
            edu.hkust.clap.monitor.Monitor.loopBegin(1077);
for (int i = 0; i < boundary.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1077);
{
                if (buffer[i] != boundary[i]) {
                    in.reset();
                    ch = in.read();
                    continue skip;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1077);

            if ((ch = in.read()) == '\r') {
                ch = in.read();
            }
            in.mark(3);
            if (in.read() == '-') {
                if (in.read() == '\r' && in.read() == '\n') {
                    fileEnd = true;
                    return false;
                }
            }
            in.reset();
            return true;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1076);

        fileEnd = true;
        return false;
    }

    /**
     * Read one byte of data from the current part.
     * @return A byte of data, or <strong>-1</strong> if end of file.
     * @exception IOException If some IO error occured.
     */
    public int read() throws IOException {
        int ch;
        if (partEnd) return -1;
        switch(ch = in.read()) {
            case '\r':
                in.mark(boundary.length + 3);
                int c1 = in.read();
                int c2 = in.read();
                int c3 = in.read();
                if ((c1 == '\n') && (c2 == '-') && (c3 == '-')) {
                    if (!readBoundaryBytes()) {
                        in.reset();
                        return ch;
                    }
                    edu.hkust.clap.monitor.Monitor.loopBegin(1078);
for (int i = 0; i < boundary.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1078);
{
                        if (buffer[i] != boundary[i]) {
                            in.reset();
                            return ch;
                        }
                    }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1078);

                    partEnd = true;
                    if ((ch = in.read()) == '\r') {
                        in.read();
                    } else if (ch == '-') {
                        if (in.read() == '-') fileEnd = true;
                    } else {
                        fileEnd = (ch == -1);
                    }
                    return -1;
                } else {
                    in.reset();
                    return ch;
                }
            case -1:
                fileEnd = true;
                return -1;
            default:
                return ch;
        }
    }

    /**
     * Read n bytes of data from the current part.
     * @return the number of bytes data, read or <strong>-1</strong> 
     * if end of file.
     * @exception IOException If some IO error occured.
     */
    public int read(byte b[], int off, int len) throws IOException {
        int got = 0;
        int ch;
        edu.hkust.clap.monitor.Monitor.loopBegin(1079);
while (got < len) { 
edu.hkust.clap.monitor.Monitor.loopInc(1079);
{
            if ((ch = read()) == -1) return (got == 0) ? -1 : got;
            b[off + (got++)] = (byte) (ch & 0xFF);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1079);

        return got;
    }

    public long skip(long n) throws IOException {
        edu.hkust.clap.monitor.Monitor.loopBegin(1080);
while ((--n >= 0) && (read() != -1)) { 
edu.hkust.clap.monitor.Monitor.loopInc(1080);
;} 
edu.hkust.clap.monitor.Monitor.loopEnd(1080);

        return n;
    }

    public int available() throws IOException {
        return in.available();
    }

    /**
     * Switch to the next available part of data.
     * One can interrupt the current part, and use this method to switch
     * to next part before current part was totally read.
     * @return A boolean <strong>true</strong> if there next partis ready,
     * or <strong>false</strong> if this was the last part.
     */
    public boolean nextInputStream() throws IOException {
        if (fileEnd) {
            return false;
        }
        if (!partEnd) {
            return skipToBoundary();
        } else {
            partEnd = false;
            return true;
        }
    }

    /**
     * Construct a new multipart input stream.
     * @param in The initial (multipart) input stream.
     * @param boundary The input stream MIME boundary.
     */
    public MultipartInputStream(InputStream in, byte boundary[]) {
        this.in = (in.markSupported() ? in : new BufferedInputStream(in, boundary.length + 4));
        this.boundary = boundary;
        this.buffer = new byte[boundary.length];
        this.partEnd = false;
        this.fileEnd = false;
    }
}
