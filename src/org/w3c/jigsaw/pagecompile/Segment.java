package org.w3c.jigsaw.pagecompile;

import java.util.Vector;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class Segment {

    public static final int CODE = 1;

    public static final int IMPORT = 2;

    public static final int EXTENDS = 3;

    public static final int IMPLEMENTS = 4;

    public static final int CLASS = 5;

    public static final int PRINT = 6;

    public static final int TEXT = 7;

    int start = -1;

    int end = -1;

    int TYPE = -1;

    /**
     * get the default type.
     * @return an int.
     */
    public static int getDefaultType() {
        return CODE;
    }

    /**
     * Get the segments with the same type.
     * @param segments an array of Segment.
     * @param type the type
     * @return an array of Segment.
     */
    public static Segment[] getSegmentMatching(Segment segments[], int type) {
        Vector segs = new Vector(10);
        edu.hkust.clap.monitor.Monitor.loopBegin(175);
for (int i = 0; i < segments.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(175);
{
            if (segments[i].TYPE == type) segs.addElement(segments[i]);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(175);

        Segment[] matchingSegs = new Segment[segs.size()];
        segs.copyInto(matchingSegs);
        return matchingSegs;
    }

    /**
     * get the type relative to the given String.
     * @param type The String type.
     * @return an int.
     */
    public static int getType(String type) {
        if (type.equalsIgnoreCase("import")) return IMPORT; else if (type.equalsIgnoreCase("extends")) return EXTENDS; else if (type.equalsIgnoreCase("class")) return CLASS; else if (type.equalsIgnoreCase("implements")) return IMPLEMENTS; else if (type.equalsIgnoreCase("print")) return PRINT; else if (type.equalsIgnoreCase("code")) return CODE; else return CODE;
    }

    public int getType() {
        return TYPE;
    }

    Segment(int start, int end) {
        this.start = start;
        this.end = end;
        this.TYPE = TEXT;
    }

    Segment(int start, int end, int TYPE) {
        this.start = start;
        this.end = end;
        this.TYPE = TYPE;
    }
}
