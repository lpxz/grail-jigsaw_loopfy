package org.w3c.jigedit.tools;

import org.w3c.tools.resources.FramedResource;

public class ToolsLister extends FramedResource {

    public void initialize(Object values[]) {
        super.initialize(values);
        try {
            registerFrameIfNone("org.w3c.jigedit.tools.ToolsListerFrame", "toolslister-frame");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
