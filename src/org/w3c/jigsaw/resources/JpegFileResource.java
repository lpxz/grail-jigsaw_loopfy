package org.w3c.jigsaw.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import org.w3c.tools.resources.FileResource;
import org.w3c.tools.jpeg.JpegCommentHandler;

public class JpegFileResource extends ImageFileResource {

    /**
     * Save the given stream as the underlying file content.
     * This method preserve the old file version in a <code>~</code> file.
     * @param in The input stream to use as the resource entity.
     * @return A boolean, <strong>true</strong> if the resource was just
     * created, <strong>false</strong> otherwise.
     * @exception IOException If dumping the content failed.
     */
    public synchronized boolean newMetadataContent(InputStream in) throws IOException {
        File file = getFile();
        boolean created = (!file.exists() || (file.length() == 0));
        String name = file.getName();
        File temp = new File(file.getParent(), "#" + name + "#");
        String iomsg = null;
        JpegCommentHandler jpegHandler = new JpegCommentHandler(file);
        try {
            FileOutputStream fout = new FileOutputStream(temp);
            char buf[] = new char[4096];
            Writer writer = jpegHandler.getOutputStreamWriter(fout);
            InputStreamReader reader = new InputStreamReader(in);
            edu.hkust.clap.monitor.Monitor.loopBegin(669);
for (int got = 0; (got = reader.read(buf)) > 0; ) { 
edu.hkust.clap.monitor.Monitor.loopInc(669);
writer.write(buf, 0, got);} 
edu.hkust.clap.monitor.Monitor.loopEnd(669);

            writer.close();
        } catch (IOException ex) {
            iomsg = ex.getMessage();
        } finally {
            if (iomsg != null) {
                temp.delete();
                throw new IOException(iomsg);
            } else {
                if (getBackupFlag()) {
                    File backup = getBackupFile();
                    if (backup.exists()) backup.delete();
                    file.renameTo(getBackupFile());
                }
                if (file.exists()) file.delete();
                temp.renameTo(file);
                updateFileAttributes();
            }
        }
        return created;
    }
}
