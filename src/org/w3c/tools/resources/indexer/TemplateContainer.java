package org.w3c.tools.resources.indexer;

import java.io.File;
import org.w3c.tools.resources.ExternalContainer;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceContext;
import org.w3c.tools.resources.ServerInterface;

public class TemplateContainer extends ExternalContainer {

    public File getRepository(ResourceContext context) {
        return new File(context.getServer().getIndexerDirectory(), getIdentifier());
    }

    public TemplateContainer(ResourceContext context, String id) {
        super(id, context, true);
    }
}
