package org.w3c.www.http;

class ParseState {

    int ioff = -1;

    int ooff = -1;

    int start = -1;

    int end = -1;

    int bufend = -1;

    boolean isSkipable = true;

    boolean isQuotable = true;

    boolean spaceIsSep = true;

    byte separator = (byte) ',';

    final void prepare() {
        ioff = ooff;
        start = -1;
        end = -1;
    }

    final void prepare(ParseState ps) {
        this.ioff = ps.start;
        this.bufend = ps.end;
    }

    final String toString(byte raw[]) {
        return new String(raw, 0, start, end - start);
    }

    final String toString(byte raw[], boolean lower) {
        if (lower) {
            edu.hkust.clap.monitor.Monitor.loopBegin(139);
for (int i = start; i < end; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(139);
raw[i] = (((raw[i] >= 'A') && (raw[i] <= 'Z')) ? (byte) (raw[i] - 'A' + 'a') : raw[i]);} 
edu.hkust.clap.monitor.Monitor.loopEnd(139);

        } else {
            edu.hkust.clap.monitor.Monitor.loopBegin(140);
for (int i = start; i < end; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(140);
raw[i] = (((raw[i] >= 'a') && (raw[i] <= 'z')) ? (byte) (raw[i] - 'a' + 'A') : raw[i]);} 
edu.hkust.clap.monitor.Monitor.loopEnd(140);

        }
        return new String(raw, 0, start, end - start);
    }

    ParseState(int ioff) {
        this.ioff = ioff;
    }

    ParseState(int ioff, int bufend) {
        this.ioff = ioff;
        this.bufend = bufend;
    }

    ParseState() {
    }
}
