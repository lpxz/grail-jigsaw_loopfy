package org.w3c.util;

public class StringUtils {

    /**
     * to hex converter
     */
    private static final char[] toHex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /**
     * convert an array of bytes to an hexadecimal string
     * @param an array of bytes
     * @return a string
     */
    public static String toHexString(byte b[]) {
        int pos = 0;
        char[] c = new char[b.length * 2];
        edu.hkust.clap.monitor.Monitor.loopBegin(932);
for (int i = 0; i < b.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(932);
{
            c[pos++] = toHex[(b[i] >> 4) & 0x0F];
            c[pos++] = toHex[b[i] & 0x0f];
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(932);

        return new String(c);
    }
}
