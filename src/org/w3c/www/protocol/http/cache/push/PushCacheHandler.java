package org.w3c.www.protocol.http.cache.push;

import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.Socket;

/**
 * PushCacheHandler
 * 
 * Created by PushCacheListener to handle a dialogue with a client
 * @see PushCacheProtocol 
 * 
 * @author Paul Henshaw, The Fantastic Corporation, Paul.Henshaw@fantastic.com
 * @version $Revision: 1.1 $
 * $Id: PushCacheHandler.java,v 1.1 2010/06/15 12:25:43 smhuang Exp $
 */
public class PushCacheHandler extends Thread {

    private Socket _socket = null;

    private boolean _running = false;

    private InputStream _stream = null;

    private byte[] _buffer = new byte[PushCacheProtocol.PACKET_LEN];

    private byte[] _textBuffer = new byte[PushCacheProtocol.MAX_STRING_LEN];

    private PushCacheListener _listener;

    DataInputStream _dataStream = null;

    /**
     * Construct a PushCacheHandler 
     * @param socket  Socket through which Handler will communicate with client
     */
    public PushCacheHandler(PushCacheListener l, Socket s) throws IOException {
        _listener = l;
        _socket = s;
        _stream = _socket.getInputStream();
        _listener.registerHandler(this);
    }

    /**
     * Send an "OK" message back to client
     */
    protected void reply_ok() throws java.io.IOException {
        byte[] ok = PushCacheProtocol.instance().okPacket();
        _socket.getOutputStream().write(ok, 0, ok.length);
    }

    /**
     * Send a "NO" message back to client
     */
    protected void reply_no() throws java.io.IOException {
        byte[] no = PushCacheProtocol.instance().noPacket();
        _socket.getOutputStream().write(no, 0, no.length);
    }

    /**
     * Send an "ERR" message back to client
     */
    protected void reply_error(String message) {
        try {
            byte[] err = PushCacheProtocol.instance().errorPacket(message);
            _socket.getOutputStream().write(err, 0, err.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Request that Handler gracefully cease execution
     */
    public void stopRunning() {
        try {
            _running = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Close sockets, etc.
     */
    protected void cleanup() {
        try {
            _socket.close();
            _stream.close();
        } catch (java.io.IOException e) {
        }
        _socket = null;
        _stream = null;
        _listener.deregisterHandler(this);
    }

    /**
     * Debugging aid, displays buffer to stderr
     */
    public void printBuffer(byte[] buf, int len) {
        byte b;
        Integer myInt = new Integer(0);
        int theInt = 0;
        System.err.println("buffer: ");
        edu.hkust.clap.monitor.Monitor.loopBegin(141);
for (int i = 0; i < len; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(141);
{
            b = buf[i];
            if (!Character.isISOControl((char) b)) {
                Character ch = new Character((char) b);
                System.err.print(" " + ch.charValue() + " ");
            } else {
                theInt = 0xff & b;
                if (theInt < 16) {
                    System.err.print("0" + myInt.toHexString(theInt) + " ");
                } else {
                    System.err.print(myInt.toHexString(theInt) + " ");
                }
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(141);

        System.err.print("\n");
    }

    /**
     * Read data based on the contents of the remain_len field
     */
    protected void readPayload() throws IOException {
        _dataStream.skipBytes(PushCacheProtocol.COMMAND_LEN);
        int rlen = _dataStream.readInt();
        if (rlen == 0) {
            return;
        }
        if (rlen > PushCacheProtocol.MAX_PAYLOAD_LEN) {
            throw new IOException("Payload length " + rlen + " exceeds maximum length " + PushCacheProtocol.MAX_PAYLOAD_LEN);
        }
        _textBuffer = new byte[rlen];
        int sofar = 0;
        int toread = rlen;
        sofar = 0;
        toread = rlen;
        int rv = -1;
        edu.hkust.clap.monitor.Monitor.loopBegin(142);
while (toread > 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(142);
{
            rv = _stream.read(_textBuffer, sofar, toread);
            if (rv < 0) {
                throw new IOException("read returned " + rv + " after reading " + sofar + " bytes");
            }
            sofar += rv;
            toread -= rv;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(142);

        _dataStream = new DataInputStream(new ByteArrayInputStream(_textBuffer));
    }

    /**
     * Handle "ADD" packet
     */
    protected void add() throws IOException {
        int plen = _dataStream.readInt();
        int ulen = _dataStream.readInt();
        if (plen == 0 || ulen == 0) {
            throw new IOException("Zero length path or url in ADD command");
        }
        if (plen > PushCacheProtocol.MAX_STRING_LEN || ulen > PushCacheProtocol.MAX_STRING_LEN) {
            throw new IOException("String too long in ADD command");
        }
        String path = new String(_textBuffer, 8, plen - 1);
        String url = new String(_textBuffer, plen + 8, ulen - 1);
        PushCacheManager.instance().storeReply(new PushReply(path, url));
        reply_ok();
    }

    /**
     * Handle "DEL" packet
     */
    protected void del() throws Exception {
        String url = new String(_textBuffer, 0, _textBuffer.length - 1);
        PushCacheManager.instance().removeURL(url);
        reply_ok();
    }

    /**
     * Handle "CLN" packet
     */
    protected void clean() throws Exception {
        PushCacheManager.instance().cleanCache();
        reply_ok();
    }

    /**
     * Handle "PRS" packet
     */
    protected void present() throws Exception {
        String url = new String(_textBuffer, 0, _textBuffer.length - 1);
        if (PushCacheManager.instance().isPresent(url)) {
            reply_ok();
        } else {
            reply_no();
        }
    }

    /**
     * Handle "NOP" packet (reply OK)
     */
    protected void nop() throws Exception {
        reply_ok();
    }

    /**
     * Handle dialogue with client
     */
    public void run() {
        int done = 0;
        int len = 0;
        int off = 0;
        int rv = 0;
        _running = true;
        try {
            edu.hkust.clap.monitor.Monitor.loopBegin(143);
while (_running) { 
edu.hkust.clap.monitor.Monitor.loopInc(143);
{
                rv = _stream.read(_buffer);
                if (!PushCacheProtocol.instance().isValidProtocolTag(_buffer)) {
                    throw new Exception("Bad protocol tag");
                }
                _dataStream = new DataInputStream(new ByteArrayInputStream(_buffer));
                _dataStream.skipBytes(PushCacheProtocol.TAG_LEN);
                short maj = _dataStream.readShort();
                short min = _dataStream.readShort();
                if (maj != PushCacheProtocol.MAJ_PROTO_VERSION || min > PushCacheProtocol.MIN_PROTO_VERSION) {
                    throw new Exception("Bad protocol version");
                }
                String command = new String(_buffer, PushCacheProtocol.HEADER_LEN, PushCacheProtocol.COMMAND_LEN);
                int com = PushCacheProtocol.instance().parseCommand(command);
                readPayload();
                switch(com) {
                    case PushCacheProtocol.ADD:
                        add();
                        break;
                    case PushCacheProtocol.BYE:
                        stopRunning();
                        break;
                    case PushCacheProtocol.DEL:
                        del();
                        break;
                    case PushCacheProtocol.CLN:
                        clean();
                        break;
                    case PushCacheProtocol.PRS:
                        present();
                        break;
                    case PushCacheProtocol.NOP:
                        nop();
                        break;
                    default:
                        throw new Exception("Unrecognised command \"" + command + "\"");
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(143);

        } catch (Exception e) {
            e.printStackTrace();
            _running = false;
            reply_error(e.getMessage());
        }
        cleanup();
    }
}
