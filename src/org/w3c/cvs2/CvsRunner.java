package org.w3c.cvs2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

class CvsRunner implements CVS {

    public static boolean debug = true;

    /**
     * Dump the given string into a temporary file.
     * This is used for th <code>-f</code> argument of the cvs commit command.
     * This method should only be used from a synchronized method.
     * @param string The string to dump.
     */
    File getTemporaryFile(String string) throws CvsException {
        String fn = "cvs-" + System.currentTimeMillis() + "-" + string.length();
        File temp = null;
        try {
            temp = File.createTempFile(fn, "-" + string.length());
            temp.deleteOnExit();
            PrintStream out = new PrintStream(new FileOutputStream(temp));
            out.print(string);
            out.close();
            return temp;
        } catch (IOException ex) {
            error("temporaryFile", "unable to create/use temporary file: " + temp.getAbsolutePath());
        }
        return temp;
    }

    /**
     * Emit an error.
     * Some abnormal situation occured, emit an error message.
     * @param mth The method in which the error occured.
     * @param msg The message to emit.
     * @exception CvsException The exception that will be thrown as a 
     *     result of the error.
     */
    protected void error(String mth, String msg) throws CvsException {
        String emsg = this.getClass().getName() + "[" + mth + "]: " + msg;
        System.err.println(emsg);
        throw new CvsException(emsg);
    }

    /**
     * Read the given input stream as a text.
     * @param in The input stream to read from.
     * @param into The StringBuffer to fill.
     * @return The provided StringBuffer, filled with the stream content.
     */
    private StringBuffer readText(InputStream procin, StringBuffer into) throws IOException {
        DataInputStream in = new DataInputStream(procin);
        String line = null;
        edu.hkust.clap.monitor.Monitor.loopBegin(986);
while ((line = in.readLine()) != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(986);
{
            if (into != null) into.append(line + "\n");
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(986);

        return into;
    }

    /**
     * Get a filename between quote contained in this String.
     * @return the filename of null.
     */
    protected String getQuotedFilename(String line) {
        int idx = line.indexOf('\'');
        if (idx == -1) return null;
        char ch;
        StringBuffer buffer = new StringBuffer();
        try {
            edu.hkust.clap.monitor.Monitor.loopBegin(987);
while ((ch = line.charAt(idx++)) != '\'') { 
edu.hkust.clap.monitor.Monitor.loopInc(987);
buffer.append(ch);} 
edu.hkust.clap.monitor.Monitor.loopEnd(987);

        } catch (ArrayIndexOutOfBoundsException ex) {
        }
        return buffer.toString();
    }

    /**
     * Parse the error message and throw an exception if necessary.
     * @exception UpToDateCheckFailedException if file was not up to date.
     * @exception CvsAddException if an error ocurs during add.
     * @exception IOException if an IO error occurs.
     */
    protected StringBuffer readError(InputStream procin, StringBuffer into) throws IOException, CvsException {
        DataInputStream in = new DataInputStream(procin);
        String line = null;
        edu.hkust.clap.monitor.Monitor.loopBegin(988);
while ((line = in.readLine()) != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(988);
{
            if (into != null) into.append(line + "\n");
            if (line.startsWith("cvs commit:")) {
                line = line.substring(12);
                if (line.startsWith("Up-to-date check failed")) {
                    String filename = getQuotedFilename(line);
                    throw new UpToDateCheckFailedException(filename);
                } else {
                    throw new CvsCommitException(line);
                }
            } else if (line.startsWith("cvs add:")) {
                throw new CvsAddException(line.substring(8));
            } else if (line.startsWith("cvs update:")) {
                throw new CvsUpdateException(line.substring(11));
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(988);

        return into;
    }

    /**
     * Wait for the underlying CVS process to finish.
     * Once the process is terminated, all relevant streams are closed, and
     * an exception if potentially thrown if the process indicated failure.
     * @param proc The CVS process.
     * @param ccode Should we expect a zero status from the child process.
     * @exception CvsException If a zero status is expected, and the CVS
     * process exit status is not zero.
     */
    protected synchronized void waitForCompletion(Process proc, boolean ccode) throws CvsException {
        edu.hkust.clap.monitor.Monitor.loopBegin(989);
while (true) { 
edu.hkust.clap.monitor.Monitor.loopInc(989);
{
            try {
                CvsException exception = null;
                StringBuffer errorlog = new StringBuffer();
                try {
                    errorlog = readError(proc.getErrorStream(), errorlog);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (CvsException cvs_ex) {
                    exception = cvs_ex;
                } finally {
                    try {
                        proc.getInputStream().close();
                    } catch (Exception ex) {
                    }
                    try {
                        proc.getOutputStream().close();
                    } catch (Exception ex) {
                    }
                    try {
                        proc.getErrorStream().close();
                    } catch (Exception ex) {
                    }
                    proc.waitFor();
                }
                int ecode = proc.exitValue();
                if (ecode != 0) {
                    String msg = ("Process exited with error code: " + ecode + " error [" + errorlog + "]");
                    if (debug) System.err.println(msg);
                    if (ccode) {
                        if (exception == null) throw new CvsException(msg); else throw exception;
                    }
                }
                edu.hkust.clap.monitor.Monitor.loopEnd(989);
                return;
            } catch (InterruptedException e) {
            }
        }} 


    }

    /**
     * Run a cvs command, return the process object.
     * @param args The command to run.
     * @exception IOException If the process couldn't be launched.
     */
    protected Process runCvsProcess(String args[]) throws IOException {
        if (debug) {
            edu.hkust.clap.monitor.Monitor.loopBegin(990);
for (int i = 0; i < args.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(990);
System.out.print(args[i] + " ");} 
edu.hkust.clap.monitor.Monitor.loopEnd(990);

            System.out.println();
        }
        return Runtime.getRuntime().exec(args);
    }

    /**
     * Run a cvs command, return the process object.
     * @param args The command to run.
     * @param env The extra environment.
     * @exception IOException If the process couldn't be launched.
     */
    protected Process runCvsProcess(String args[], String env[]) throws IOException {
        if (debug) {
            edu.hkust.clap.monitor.Monitor.loopBegin(990);
for (int i = 0; i < args.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(990);
System.out.print(args[i] + " ");} 
edu.hkust.clap.monitor.Monitor.loopEnd(990);

            System.out.println();
        }
        return Runtime.getRuntime().exec(args, env);
    }

    /**
     * Get the CVS command String array.
     * @param cvs The target CvsDirectory in which the command is to be run.
     * @param cvsopts The CVS wide options.
     * @param cmdopts The command, and its optional options.
     * @return A String array, giving the command to be executed.
     */
    protected String[] getCommand(CvsDirectory cvs, String cvsopts[], String cmdopts[]) {
        String cvswrapper[] = cvs.getCvsWrapper();
        String cvsdefs[] = cvs.getCvsDefaults();
        String cmd[] = new String[cvswrapper.length + cvsdefs.length + ((cvsopts != null) ? cvsopts.length : 0) + ((cmdopts != null) ? cmdopts.length : 0)];
        int cmdptr = 0;
        if (cvswrapper != null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(991);
for (int i = 0; i < cvswrapper.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(991);
cmd[cmdptr++] = cvswrapper[i];} 
edu.hkust.clap.monitor.Monitor.loopEnd(991);

        }
        if (cvsdefs != null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(992);
for (int i = 0; i < cvsdefs.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(992);
cmd[cmdptr++] = cvsdefs[i];} 
edu.hkust.clap.monitor.Monitor.loopEnd(992);

        }
        if (cvsopts != null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(993);
for (int i = 0; i < cvsopts.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(993);
cmd[cmdptr++] = cvsopts[i];} 
edu.hkust.clap.monitor.Monitor.loopEnd(993);

        }
        if (cmdopts != null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(994);
for (int i = 0; i < cmdopts.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(994);
cmd[cmdptr++] = cmdopts[i];} 
edu.hkust.clap.monitor.Monitor.loopEnd(994);

        }
        return cmd;
    }

    /**
     * Parse the CVS output for the update command.
     * @param procin The CVS process output.
     * @param handler The handler to callback.
     * @exception IOException If IO error occurs.
     * @exception CvsException If the CVS process failed.
     */
    private void parseUpdateOutput(InputStream procin, UpdateHandler handler) throws IOException, CvsException {
        DataInputStream in = new DataInputStream(procin);
        String line = null;
        edu.hkust.clap.monitor.Monitor.loopBegin(995);
while ((line = in.readLine()) != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(995);
{
            if (line.length() <= 0) continue;
            int status = -1;
            int ch = line.charAt(0);
            if (line.charAt(1) != ' ') continue;
            switch(ch) {
                case 'U':
                    status = FILE_OK;
                    break;
                case 'A':
                    status = FILE_A;
                    break;
                case 'R':
                    status = FILE_R;
                    break;
                case 'M':
                    status = FILE_M;
                    break;
                case 'C':
                    status = FILE_C;
                    break;
                case '?':
                    status = FILE_Q;
                    break;
                default:
                    continue;
            }
            handler.notifyEntry(line.substring(2), status);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(995);

    }

    private void parseStatusOutput(InputStream procin, StatusHandler handler) throws IOException, CvsException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(procin));
        String line = null;
        StringTokenizer st = null;
        String file = null;
        String revision = null;
        String token = null;
        String st_opt = null;
        edu.hkust.clap.monitor.Monitor.loopBegin(996);
while ((line = reader.readLine()) != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(996);
{
            if (line.length() <= 0) continue;
            if (line.startsWith("==")) {
                file = null;
                revision = null;
                continue;
            }
            st = new StringTokenizer(line);
            if (st.hasMoreTokens()) {
                try {
                    token = st.nextToken();
                    if (token.equals("File:")) {
                        file = st.nextToken();
                        if (file.equals("no") && st.hasMoreTokens()) {
                            token = st.nextToken();
                            if (token.equals("file") && st.hasMoreTokens()) file = st.nextToken();
                        }
                    } else if (token.equals("Repository") && st.nextToken().equals("revision:")) {
                        revision = st.nextToken();
                    } else if (token.equals("Sticky")) {
                        token = st.nextToken();
                        if (token.equals("Tag:")) {
                        } else if (token.equals("Date:")) {
                        } else if (token.equals("Options:")) {
                            st_opt = st.nextToken();
                            if (st_opt.equals("(none)")) {
                                st_opt = null;
                            }
                        }
                    }
                } catch (NoSuchElementException ex) {
                    ex.printStackTrace();
                    file = null;
                    revision = null;
                }
            }
            if ((file != null) && (revision != null)) {
                handler.notifyEntry(file, revision, st_opt);
                file = null;
                revision = null;
                st_opt = null;
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(996);

    }

    /**
     * Parse the CVS output for the commit command.
     * @param procin The CVS process output.
     * @param handler The handler to callback.
     * @exception IOException If some IO error occur.
     * @exception CvsException If the CVS process failed.
     */
    private void parseCommitOutput(InputStream procin, CommitHandler handler) throws IOException, CvsException {
        DataInputStream in = new DataInputStream(procin);
        String line = null;
        edu.hkust.clap.monitor.Monitor.loopBegin(997);
while ((line = in.readLine()) != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(997);
{
            if (!line.startsWith("Checking in ")) continue;
            String filename = line.substring("Checking in ".length(), line.length() - 1);
            handler.notifyEntry(filename, FILE_OK);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(997);

        return;
    }

    /**
     * Run the 'update -p -r <revision>' command.
     * @param cvs The target CvsDirectory in which the command is to be run.
     * @param name The file name to which the update command applies.
     * @param revision The revision to retrieve
     * @param out The OutputStream 
     * @exception CvsException If the underlying CVS process fails
     */
    void cvsRevert(CvsDirectory cvs, String name, String revision, OutputStream out, String env[]) throws CvsException {
        String cmdopts[] = { "update", "-p", "-r", revision, name };
        String command[] = getCommand(cvs, null, cmdopts);
        try {
            Process proc = runCvsProcess(command, env);
            BufferedInputStream in = new BufferedInputStream(proc.getInputStream());
            BufferedOutputStream bout = new BufferedOutputStream(out);
            byte buf[] = new byte[4096];
            try {
                edu.hkust.clap.monitor.Monitor.loopBegin(998);
for (int got = 0; (got = in.read(buf)) > 0; ) { 
edu.hkust.clap.monitor.Monitor.loopInc(998);
bout.write(buf, 0, got);} 
edu.hkust.clap.monitor.Monitor.loopEnd(998);

                bout.flush();
            } catch (IOException ex) {
                throw new CvsException(ex.getMessage());
            } finally {
                try {
                    bout.close();
                } catch (Exception ex) {
                }
            }
            waitForCompletion(proc, true);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new CvsException(ex.getMessage());
        }
    }

    /**
     * Run the 'update -p -r <revision>' command.
     * @param cvs The target CvsDirectory in which the command is to be run.
     * @param name The file name to which the update command applies.
     * @param revision The revision to retrieve
     * @param file The local file
     * @exception CvsException If the underlying CVS process fails
     */
    void cvsRevert(CvsDirectory cvs, String name, String revision, File file, String env[]) throws CvsException {
        try {
            cvsRevert(cvs, name, revision, new FileOutputStream(file), env);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new CvsException(ex.getMessage());
        }
    }

    /**
     * Run the update command.
     * @param names The file names to which the update command applies.
     * @param handler The CVS output parsing handler.
     * @exception CvsException If the underlying CVS process fails.
     * @see UpdateHandler
     */
    void cvsUpdate(CvsDirectory cvs, String names[], UpdateHandler handler) throws CvsException {
        String cmdopts[] = new String[names.length + 1];
        cmdopts[0] = "update";
        System.arraycopy(names, 0, cmdopts, 1, names.length);
        String command[] = getCommand(cvs, null, cmdopts);
        try {
            Process proc = runCvsProcess(command);
            parseUpdateOutput(proc.getInputStream(), handler);
            waitForCompletion(proc, true);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new CvsException(ex.getMessage());
        }
    }

    /**
     * Run the update command on the whole directory. (not recursivly)
     * @param cvs The target CvsDirectory in which the command is to be run.
     * @param handler The CVS output handler.
     * @exception CvsException If something failed.
     */
    void cvsUpdate(CvsDirectory cvs, UpdateHandler handler) throws CvsException {
        String cmdopts[] = { "update", "-l" };
        String command[] = getCommand(cvs, null, cmdopts);
        try {
            Process proc = runCvsProcess(command);
            parseUpdateOutput(proc.getInputStream(), handler);
            waitForCompletion(proc, true);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new CvsException(ex.getMessage());
        }
    }

    /**
     * Check this file status.
     * We just run the update command with the -n toggle.
     * @param cvs The target CvsDirectory in which the command is to be run.
     * @param handler The underlying update handler callback.
     * @exception CvsException If the CVS process failed.
     */
    void cvsLoad(CvsDirectory cvs, String filename, UpdateHandler handler) throws CvsException {
        String cvsopts[] = { "-n" };
        String cmdopts[] = { "update", "-I", "!", filename };
        String command[] = getCommand(cvs, cvsopts, cmdopts);
        try {
            Process proc = runCvsProcess(command);
            parseUpdateOutput(proc.getInputStream(), handler);
            waitForCompletion(proc, false);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new CvsException(ex.getMessage());
        }
    }

    /**
     * Check this file status.
     * We just run the update command and the status with the -n toggle.
     * @param cvs The target CvsDirectory in which the command is to be run.
     * @param handler The underlying update handler callback.
     * @param statush The underlying status handler callback.
     * @exception CvsException If the CVS process failed.
     */
    void cvsLoad(CvsDirectory cvs, String filename, UpdateHandler handler, StatusHandler statush) throws CvsException {
        String cvsopts[] = { "-n" };
        String cmdopts[] = { "update", "-I", "!", filename };
        String command[] = getCommand(cvs, cvsopts, cmdopts);
        try {
            Process proc = runCvsProcess(command);
            parseUpdateOutput(proc.getInputStream(), handler);
            waitForCompletion(proc, false);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new CvsException(ex.getMessage());
        }
        String cmdstatus[] = new String[2];
        cmdstatus[0] = "status";
        cmdstatus[1] = filename;
        command = getCommand(cvs, null, cmdstatus);
        try {
            Process proc = runCvsProcess(command);
            parseStatusOutput(proc.getInputStream(), statush);
            waitForCompletion(proc, false);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new CvsException(ex.getMessage());
        }
    }

    /**
     * Check all this directory files status.
     * We just run the update command and the status with the -n toggle.
     * @param cvs The target CvsDirectory in which the command is to be run.
     * @param handler The underlying update handler callback.
     * @param statush The underlying status handler callback.
     * @exception CvsException If the CVS process failed.
     */
    void cvsLoad(CvsDirectory cvs, UpdateHandler handler, StatusHandler statush) throws CvsException {
        String cvsopts[] = { "-n" };
        String cmdopts[] = { "update", "-I", "!", "-l" };
        String command[] = getCommand(cvs, cvsopts, cmdopts);
        try {
            Process proc = runCvsProcess(command);
            parseUpdateOutput(proc.getInputStream(), handler);
            waitForCompletion(proc, false);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new CvsException(ex.getMessage());
        }
        String cmdstatus[] = { "status", "-l" };
        command = getCommand(cvs, null, cmdstatus);
        try {
            Process proc = runCvsProcess(command);
            parseStatusOutput(proc.getInputStream(), statush);
            waitForCompletion(proc, false);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new CvsException(ex.getMessage());
        }
    }

    /**
     * Check this directory file status.
     * We just run the update command with the -n toggle.
     * @param cvs The target CvsDirectory in which the command is to be run.
     * @param statush The underlying status handler callback.
     * @exception CvsException If the CVS process failed.
     */
    void cvsStatus(CvsDirectory cvs, String filename, StatusHandler statush) throws CvsException {
        String cmdstatus[] = new String[2];
        cmdstatus[0] = "status";
        cmdstatus[1] = filename;
        String command[] = getCommand(cvs, null, cmdstatus);
        try {
            Process proc = runCvsProcess(command);
            parseStatusOutput(proc.getInputStream(), statush);
            waitForCompletion(proc, false);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new CvsException(ex.getMessage());
        }
    }

    /**
     * Run the commit command on the given set of files.
     * @param cvs The target CvsDirectory in which the command is to be run.
     * @param names The name of files to commit.
     * @param comment Description of the file changes.
     * @param handler The CVS output callback.
     * @exception CvsException If the CVS process failed.
     */
    void cvsCommit(CvsDirectory cvs, String names[], String comment, CommitHandler handler) throws CvsException {
        cvsCommit(cvs, names, comment, handler, null);
    }

    /**
     * Run the commit command on the given set of files.
     * @param cvs The target CvsDirectory in which the command is to be run.
     * @param names The name of files to commit.
     * @param comment Description of the file changes.
     * @param handler The CVS output callback.
     * @param env The extra env to use during commit.
     * @exception CvsException If the CVS process failed.
     */
    void cvsCommit(CvsDirectory cvs, String names[], String comment, CommitHandler handler, String env[]) throws CvsException {
        String cmdopts[] = new String[names.length + 3];
        File tmpfile = getTemporaryFile(comment);
        cmdopts[0] = "commit";
        cmdopts[1] = "-F";
        cmdopts[2] = tmpfile.getAbsolutePath();
        System.arraycopy(names, 0, cmdopts, 3, names.length);
        String command[] = getCommand(cvs, null, cmdopts);
        try {
            Process proc = runCvsProcess(command, env);
            parseCommitOutput(proc.getInputStream(), handler);
            waitForCompletion(proc, true);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new CvsException(ex.getMessage());
        } finally {
            tmpfile.delete();
        }
    }

    /**
     * Ru the commit comamnd in the given directory.
     * @param cvs The target CvsDirectory in which the command is to be run.
     * @param msg Description of directory changes since last commit.
     * @param handler The CVS output callback.
     * @exception CvsException If the CVS process failed.
     */
    void cvsCommit(CvsDirectory cvs, String msg, CommitHandler handler) throws CvsException {
        cvsCommit(cvs, msg, handler, (String[]) null);
    }

    /**
     * Ru the commit comamnd in the given directory.
     * @param cvs The target CvsDirectory in which the command is to be run.
     * @param msg Description of directory changes since last commit.
     * @param handler The CVS output callback.
     * @param env The extra env to use during commit.
     * @exception CvsException If the CVS process failed.
     */
    void cvsCommit(CvsDirectory cvs, String msg, CommitHandler handler, String env[]) throws CvsException {
        File tmpfile = getTemporaryFile(msg);
        String cmdopts[] = new String[3];
        cmdopts[0] = "commit";
        cmdopts[1] = "-F";
        cmdopts[2] = tmpfile.getAbsolutePath();
        String command[] = getCommand(cvs, null, cmdopts);
        try {
            Process proc = runCvsProcess(command, env);
            parseCommitOutput(proc.getInputStream(), handler);
            waitForCompletion(proc, true);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new CvsException(ex.getMessage());
        } finally {
            tmpfile.delete();
        }
    }

    /**
     * perform a cvs get 
     */
    void cvsGet(CvsDirectory cvs, String path) throws CvsException {
        String cmdopts[] = new String[2];
        cmdopts[0] = "get";
        cmdopts[1] = path;
        String command[] = getCommand(cvs, null, cmdopts);
        try {
            Process proc = runCvsProcess(command);
            readText(proc.getInputStream(), null);
            waitForCompletion(proc, true);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new CvsException(ex.getMessage());
        }
    }

    /**
     * Run the log command on the given file
     * @param cvs The target CvsDirectory in which the command is to be run.
     * @param name The name of the file to log.
     * @exception CvsException If the CVS command failed.
     * @return A String containing the file's log.
     */
    String cvsLog(CvsDirectory cvs, String name) throws CvsException {
        String cmdopts[] = new String[2];
        cmdopts[0] = "log";
        cmdopts[1] = name;
        String command[] = getCommand(cvs, null, cmdopts);
        StringBuffer into = new StringBuffer();
        try {
            Process proc = runCvsProcess(command);
            readText(proc.getInputStream(), into);
            waitForCompletion(proc, false);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new CvsException(ex.getMessage());
        }
        return into.toString();
    }

    /**
     * Run the diff command on the given file
     * @param cvs The target CvsDirectory in which the command is to be run.
     * @param name The name of the file to diff.
     * @exception CvsException If the CVS command failed.
     * @return A String containing the diff, or <strong>null</strong> if the
     * file is in sync with the repository.
     */
    String cvsDiff(CvsDirectory cvs, String name) throws CvsException {
        String cmdopts[] = new String[2];
        cmdopts[0] = "diff";
        cmdopts[1] = name;
        String command[] = getCommand(cvs, null, cmdopts);
        StringBuffer into = new StringBuffer();
        try {
            Process proc = runCvsProcess(command);
            readText(proc.getInputStream(), into);
            waitForCompletion(proc, false);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new CvsException(ex.getMessage());
        }
        return (into.length() > 0) ? into.toString() : null;
    }

    /**
     * Run the remove command.
     * @param cvs The target CvsDirectory in which the command is to be run.
     * @param names The name of the files to remove locally.
     * @exception CvsException If the CVS command failed.
     */
    void cvsRemove(CvsDirectory cvs, String names[]) throws CvsException {
        String cmdopts[] = new String[names.length + 1];
        cmdopts[0] = "remove";
        System.arraycopy(names, 0, cmdopts, 1, names.length);
        String command[] = getCommand(cvs, null, cmdopts);
        try {
            Process proc = runCvsProcess(command);
            readText(proc.getInputStream(), null);
            waitForCompletion(proc, true);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new CvsException(ex.getMessage());
        }
    }

    /**
     * Run the add command
     * @param cvs The target CvsDirectory in which the command is to be run.
     * @param names The name of the files to add locally.
     * @exception CvsException If the CVS command failed.
     */
    void cvsAdd(CvsDirectory cvs, String names[]) throws CvsException {
        cvsAdd(cvs, names, null);
    }

    /**
     * Run the add command
     * @param cvs The target CvsDirectory in which the command is to be run.
     * @param names The name of the files to add locally.
     * @param env The extra env to use during the process.
     * @exception CvsException If the CVS command failed.
     */
    void cvsAdd(CvsDirectory cvs, String names[], String env[]) throws CvsException {
        String cmdopts[] = new String[names.length + 1];
        cmdopts[0] = "add";
        System.arraycopy(names, 0, cmdopts, 1, names.length);
        String command[] = getCommand(cvs, null, cmdopts);
        try {
            Process proc = runCvsProcess(command, env);
            readText(proc.getInputStream(), null);
            waitForCompletion(proc, true);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new CvsException(ex.getMessage());
        }
    }

    /**
     * Run the admin command
     * @param cvs The target CvsDirectory in which the command is to be run.
     * @param command The rcs command
     * @exception CvsException If the CVS command failed.
     */
    void cvsAdmin(CvsDirectory cvs, String command[]) throws CvsException {
        String cmdopts[] = new String[command.length + 1];
        cmdopts[0] = "admin";
        System.arraycopy(command, 0, cmdopts, 1, command.length);
        String cvscommand[] = getCommand(cvs, null, cmdopts);
        try {
            Process proc = runCvsProcess(cvscommand);
            readText(proc.getInputStream(), null);
            waitForCompletion(proc, true);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new CvsException(ex.getMessage());
        }
    }

    /**
     * Update a directory.
     * @param cvs The target CvsDirectory in which the command is to be run.
     * @param subdir The sub-directory of the above that needs to be updated.
     * @param handler The CVS output handler.
     * @exception CvsException If the CVS process fails.
     */
    void cvsUpdateDirectory(CvsDirectory cvs, File subdir, UpdateHandler handler) throws CvsException {
        String cmdopts[] = new String[4];
        cmdopts[0] = "update";
        cmdopts[1] = "-l";
        cmdopts[2] = "-d";
        cmdopts[3] = subdir.getName();
        String command[] = getCommand(cvs, null, cmdopts);
        try {
            Process proc = runCvsProcess(command);
            parseUpdateOutput(proc.getInputStream(), handler);
            waitForCompletion(proc, true);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new CvsException(ex.getMessage());
        }
    }
}
