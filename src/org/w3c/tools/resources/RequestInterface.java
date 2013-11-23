package org.w3c.tools.resources;

public interface RequestInterface {

    /**
   * FIXME doc
   */
    public void setState(String name, String state);

    /**
   * Get the URL path of the target resource.
   */
    public String getURLPath();

    /**
   * Return true is the request is internal.
   * @return a boolean.
   */
    public boolean isInternal();

    /**
   * Get a "Bad request" reply.
   * @return a ReplyInterface instance.
   */
    public ReplyInterface makeBadRequestReply();
}
