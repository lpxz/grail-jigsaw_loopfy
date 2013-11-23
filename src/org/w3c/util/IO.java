package org.w3c.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class IO {

    /**
     * Copy source into dest.
     */
    public static void copy(File source, File dest) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(source));
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
        byte buffer[] = new byte[1024];
        int read = -1;
        edu.hkust.clap.monitor.Monitor.loopBegin(1001);
while ((read = in.read(buffer, 0, 1024)) != -1) { 
edu.hkust.clap.monitor.Monitor.loopInc(1001);
out.write(buffer, 0, read);} 
edu.hkust.clap.monitor.Monitor.loopEnd(1001);

        out.flush();
        out.close();
        in.close();
    }

    /**
     * Copy source into dest.
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        BufferedInputStream bin = new BufferedInputStream(in);
        BufferedOutputStream bout = new BufferedOutputStream(out);
        byte buffer[] = new byte[1024];
        int read = -1;
        edu.hkust.clap.monitor.Monitor.loopBegin(1002);
while ((read = bin.read(buffer, 0, 1024)) != -1) { 
edu.hkust.clap.monitor.Monitor.loopInc(1002);
bout.write(buffer, 0, read);} 
edu.hkust.clap.monitor.Monitor.loopEnd(1002);

        bout.flush();
        bout.close();
        bin.close();
    }

    /**
     * Delete recursivly
     * @param file the file (or directory) to delete.
     */
    public static boolean delete(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                if (clean(file)) {
                    return file.delete();
                } else {
                    return false;
                }
            } else {
                return file.delete();
            }
        }
        return true;
    }

    /**
     * Clean recursivly
     * @param file the directory to clean
     */
    public static boolean clean(File file) {
        if (file.isDirectory()) {
            String filen[] = file.list();
            edu.hkust.clap.monitor.Monitor.loopBegin(1003);
for (int i = 0; i < filen.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(1003);
{
                File subfile = new File(file, filen[i]);
                if ((subfile.isDirectory()) && (!clean(subfile))) {
                    return false;
                } else if (!subfile.delete()) {
                    return false;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1003);

        }
        return true;
    }
}
