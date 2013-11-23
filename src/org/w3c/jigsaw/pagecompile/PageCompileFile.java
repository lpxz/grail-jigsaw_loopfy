package org.w3c.jigsaw.pagecompile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class PageCompileFile {

    private byte[] filedata = null;

    protected void readFileData(String filename) throws IOException {
        File file = new File(filename);
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
        filedata = out.toByteArray();
    }

    /**
     * Write some bytes from this file in the given output stream.
     * @param start start position in the file
     * @param end end position in the file
     * @param out the destination output stream
     * @exception IOException if an IO error occurs
     */
    public void writeBytes(int start, int end, OutputStream out) throws IOException {
        int len = end - start + 1;
        byte b[] = new byte[len];
        if (start + len > filedata.length) len--;
        System.arraycopy(filedata, start, b, 0, len);
        out.write(b);
    }

    /**
     * Create a PageCompileFile.
     * @param filename the filename
     * @exception IOException if an IO error occurs.
     */
    public PageCompileFile(String filename) throws IOException {
        readFileData(filename);
    }
}
