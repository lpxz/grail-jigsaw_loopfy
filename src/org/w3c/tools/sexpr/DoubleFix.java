package org.w3c.tools.sexpr;

public class DoubleFix {

    /**
   * A fix for Double.valueOf in JDK 1.0.2
   */
    public static Double valueOf(String rep) throws NumberFormatException {
        String s = rep.trim();
        int point = s.indexOf('.');
        if (point == -1) throw new NumberFormatException();
        double whole = 0;
        if (point > 0) whole = (double) Integer.parseInt(s.substring(0, point));
        double fraction = 0;
        if (point < s.length() - 1) fraction = (double) Integer.parseInt(s.substring(point + 1)); else if (point == 0 || !Character.isDigit(s.charAt(point + 1))) throw new NumberFormatException();
        int m;
        edu.hkust.clap.monitor.Monitor.loopBegin(1088);
for (m = 10; m < fraction; m *= 10) { 
edu.hkust.clap.monitor.Monitor.loopInc(1088);
;} 
edu.hkust.clap.monitor.Monitor.loopEnd(1088);

        return new Double(whole + fraction / (double) m);
    }
}
