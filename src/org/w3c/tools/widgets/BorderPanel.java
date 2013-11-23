package org.w3c.tools.widgets;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Panel;

/**
 * A Panel with a border (SOLID, RAISED, LOWERED, IN or OUT)
 * @author Benoit.Mahe@sophia.inria.fr 
 * @author Thierry.Kormann@sophia.inria.fr 
 */
public class BorderPanel extends Panel {

    public static final int SOLID = 0;

    public static final int RAISED = 1;

    public static final int LOWERED = 2;

    public static final int IN = 3;

    public static final int OUT = 4;

    private static final int DEFAULT_THICKNESS = 2;

    private int type;

    private int thickness;

    protected Insets insets = null;

    /**
     * Constructor.
     * @param type The border type (SOLID, RAISED, LOWERED, IN or OUT)
     * @param thickness The border thickness.
     */
    public BorderPanel(int type, int thickness) {
        this.type = type;
        this.thickness = thickness;
        build();
    }

    /**
     * Constructor.
     * @param type The border type (SOLID, RAISED, LOWERED, IN or OUT)
     */
    public BorderPanel(int type) {
        this.type = type;
        thickness = DEFAULT_THICKNESS;
        build();
    }

    private void build() {
        insets = new Insets(thickness, thickness, thickness, thickness);
    }

    /**
     * Paint the border (if any), and then, paint the components.
     * @param graphics the specified Graphics window
     */
    public void paint(Graphics graphics) {
        if (thickness > 0) {
            Dimension size = size();
            graphics.setColor(getBackground());
            switch(type) {
                case SOLID:
                    graphics.setColor(getForeground());
                    edu.hkust.clap.monitor.Monitor.loopBegin(480);
for (int i = 0; i < thickness; ++i) { 
edu.hkust.clap.monitor.Monitor.loopInc(480);
graphics.drawRect(i, i, size.width - i * 2 - 1, size.height - i * 2 - 1);} 
edu.hkust.clap.monitor.Monitor.loopEnd(480);

                    break;
                case RAISED:
                    edu.hkust.clap.monitor.Monitor.loopBegin(481);
for (int i = 0; i < thickness; ++i) { 
edu.hkust.clap.monitor.Monitor.loopInc(481);
graphics.draw3DRect(i, i, size.width - i * 2 - 1, size.height - i * 2 - 1, true);} 
edu.hkust.clap.monitor.Monitor.loopEnd(481);

                    break;
                case LOWERED:
                    edu.hkust.clap.monitor.Monitor.loopBegin(482);
for (int i = 0; i < thickness; ++i) { 
edu.hkust.clap.monitor.Monitor.loopInc(482);
graphics.draw3DRect(i, i, size.width - i * 2 - 1, size.height - i * 2 - 1, false);} 
edu.hkust.clap.monitor.Monitor.loopEnd(482);

                    break;
                case IN:
                    graphics.draw3DRect(0, 0, size.width - 1, size.height - 1, false);
                    graphics.draw3DRect(thickness - 1, thickness - 1, size.width - thickness * 2 + 1, size.height - thickness * 2 + 1, true);
                    break;
                case OUT:
                    graphics.draw3DRect(0, 0, size.width - 1, size.height - 1, true);
                    graphics.draw3DRect(thickness - 1, thickness - 1, size.width - thickness * 2 + 1, size.height - thickness * 2 + 1, false);
                    break;
            }
        }
        super.paint(graphics);
    }

    public Insets getInsets() {
        return insets;
    }

    public void setInsets(Insets insets) {
        this.insets = insets;
    }
}
