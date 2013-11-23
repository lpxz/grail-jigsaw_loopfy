package org.w3c.www.http;

public class HeaderDescription {

    Class cls = null;

    String name = null;

    byte title[] = null;

    int offset = -1;

    /**
     * Get this header name, lower case (can be used as header id).
     * @return A String giving the header identifier.
     */
    public String getName() {
        return name;
    }

    /**
     * Get this header title, ready for emission.
     * @return The actual bytes to be emited for this header title.
     */
    public byte[] getTitle() {
        return title;
    }

    /**
     * Get this header parser, as an HeaderValue compatible instance.
     * @return An instance of HeaderValue, suitable for holding and parsing
     * the header value.
     */
    public HeaderValue getHolder() {
        try {
            return (HeaderValue) cls.newInstance();
        } catch (NoSuchMethodError er) {
            throw new RuntimeException("Invalid class (method) for " + name);
        } catch (InstantiationError ex) {
            throw new RuntimeException("Invalid class (method) for " + name);
        } catch (Exception ex) {
            throw new RuntimeException("Invalid class for " + name);
        }
    }

    /**
     * Is this header description the one of that header.
     * @param h The header access token.
     */
    public boolean isHeader(int h) {
        return h == offset;
    }

    HeaderDescription(String title, String clsname, int offset) {
        try {
            this.title = new byte[title.length()];
            title.getBytes(0, this.title.length, this.title, 0);
            this.name = title.toLowerCase();
            this.offset = offset;
            this.cls = Class.forName(clsname);
            this.cls.newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Invalid header description " + name);
        }
    }

    HeaderDescription(String title, String clsname) {
        this(title, clsname, -1);
    }
}
