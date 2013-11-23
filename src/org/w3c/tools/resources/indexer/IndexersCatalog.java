package org.w3c.tools.resources.indexer;

import java.io.File;
import org.w3c.tools.resources.ExternalContainer;
import org.w3c.tools.resources.ResourceContext;
import org.w3c.tools.resources.ServerInterface;

public class IndexersCatalog extends ExternalContainer {

    public File getRepository(ResourceContext context) {
        return new File(context.getServer().getIndexerDirectory(), "idx.db");
    }

    public IndexersCatalog(ResourceContext context) {
        super("indexers".intern(), context, true);
    }
}
