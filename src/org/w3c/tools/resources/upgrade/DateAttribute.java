package org.w3c.tools.resources.upgrade;

import java.util.Date;

public class DateAttribute extends LongAttribute {

    public DateAttribute(String name, Long def, Integer flags) {
        super(name, def, flags);
        this.type = "java.util.Date";
    }
}
