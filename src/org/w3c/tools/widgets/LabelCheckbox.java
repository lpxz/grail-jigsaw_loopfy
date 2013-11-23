package org.w3c.tools.widgets;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.LayoutManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class LabelCheckbox extends BorderPanel implements ItemListener {

    Label label = null;

    Checkbox checkbox = null;

    String strue = "on";

    String sfalse = "off";

    Dimension size = null;

    public void itemStateChanged(ItemEvent e) {
        switch(e.getStateChange()) {
            case ItemEvent.SELECTED:
                setState(true);
                break;
            case ItemEvent.DESELECTED:
                setState(false);
                break;
            default:
        }
    }

    String getString(boolean check) {
        return (check ? strue : sfalse);
    }

    public void setState(boolean state) {
        checkbox.setState(state);
        label.setText(getString(state));
    }

    public boolean getState() {
        return checkbox.getState();
    }

    /**
     * Create a new LabelCheckbox
     */
    public LabelCheckbox(int type, int thickness) {
        super(type, thickness);
        init();
    }

    /**
     * Create a new LabelCheckbox
     */
    public LabelCheckbox(int type) {
        super(type);
        init();
    }

    /**
     * Create a new LabelCheckbox
     */
    public LabelCheckbox() {
        super(IN);
        init();
    }

    public Dimension getPreferredSize() {
        return size;
    }

    public Dimension getMinimumSize() {
        return size;
    }

    public Dimension getSize() {
        return size;
    }

    private void init() {
        setLayout(new BorderLayout());
        label = new Label(getString(true));
        checkbox = new Checkbox();
        checkbox.setState(true);
        checkbox.addItemListener(this);
        add(checkbox, "West");
        add(label, "Center");
        size = new Dimension(75, 30);
    }
}
