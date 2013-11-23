package org.w3c.jigsaw.admin;

import java.net.URL;
import org.w3c.tools.resources.serialization.ResourceDescription;

public class RemoteResourceFactory {

    AdminContext admin = null;

    public RemoteResource createRemoteResource(URL parent, String identifier, ResourceDescription descr) {
        return new PlainRemoteResource(admin, parent, identifier, descr);
    }

    RemoteResourceFactory(AdminContext admin) {
        this.admin = admin;
    }
}
