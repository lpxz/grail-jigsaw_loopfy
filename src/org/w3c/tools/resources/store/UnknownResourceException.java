package org.w3c.tools.resources.store;

import org.w3c.tools.resources.Resource;

public class UnknownResourceException extends RuntimeException {

    public UnknownResourceException(Resource resource) {
        super("ResourceStore mismatch for resource " + resource.getIdentifier());
    }
}
