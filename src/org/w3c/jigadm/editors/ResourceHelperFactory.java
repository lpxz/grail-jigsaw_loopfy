package org.w3c.jigadm.editors;

import org.w3c.jigadm.PropertyManager;
import org.w3c.jigadm.RemoteResourceWrapper;

public class ResourceHelperFactory {

    public static synchronized ResourceHelper[] getHelpers(RemoteResourceWrapper rrw) {
        PropertyManager pm = PropertyManager.getPropertyManager();
        String classes[] = pm.getHelperClasses(rrw);
        ResourceHelper helpers[] = null;
        if (classes != null) {
            helpers = new ResourceHelper[classes.length];
            if (helpers.length == 0) return null;
            edu.hkust.clap.monitor.Monitor.loopBegin(259);
for (int i = 0; i < classes.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(259);
{
                try {
                    Class c = Class.forName(classes[i]);
                    helpers[i] = (ResourceHelper) c.newInstance();
                } catch (Exception ex) {
                    System.out.println("cannot create helper: \"" + classes[i] + " for \"" + rrw);
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(259);

        }
        return helpers;
    }
}
