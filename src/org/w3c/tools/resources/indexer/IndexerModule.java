package org.w3c.tools.resources.indexer;

import org.w3c.tools.resources.ContainerResource;
import org.w3c.tools.resources.ResourceContext;
import org.w3c.tools.resources.ResourceReference;

public class IndexerModule {

    public static final String NAME = "org.w3c.jigsaw.indexer".intern();

    private static final String INDEXER = "org.w3c.jigsaw.indexer.name".intern();

    protected IndexersCatalog catalog = null;

    public void registerIndexer(ResourceContext ctxt, String name) {
        ctxt.registerModule(INDEXER, name);
    }

    public ResourceReference getIndexer(ResourceContext ctxt) {
        String idxname = (String) ctxt.getModule(INDEXER);
        try {
            return catalog.lookup(idxname);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public ResourceReference getIndexer(String name) {
        try {
            return catalog.lookup(name);
        } catch (Exception ex) {
            return null;
        }
    }

    public IndexerModule(IndexersCatalog catalog) {
        this.catalog = catalog;
    }
}
