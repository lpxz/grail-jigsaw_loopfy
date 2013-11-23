package org.w3c.jigsaw.webdav;

import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.www.webdav.xml.DAVBody;
import org.w3c.www.webdav.xml.DAVFactory;
import org.w3c.www.webdav.xml.DAVMultiStatus;
import org.w3c.www.webdav.xml.DAVNode;
import org.w3c.www.webdav.xml.DAVResponse;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class MultiStatusException extends Exception {

    private boolean multi = false;

    private String url = null;

    private String status = null;

    private String msg = null;

    private Document document = null;

    public Document getDocument() {
        if (document == null) {
            document = DAVBody.createDocument(DAVNode.MULTISTATUS_NODE);
            DAVResponse dr = null;
            if (msg != null) {
                dr = DAVFactory.createResponse(url, status, msg, document);
            } else {
                dr = DAVFactory.createResponse(url, status, document);
            }
            document.getDocumentElement().appendChild(dr.getNode());
        }
        return document;
    }

    public void addResponses(Document doc, DAVMultiStatus dms) {
        if (multi) {
            Element odms = document.getDocumentElement();
            try {
                DAVNode.exportChildren(doc, dms.getNode(), odms, true);
            } catch (DOMException ex) {
                ex.printStackTrace();
            }
        } else {
            DAVResponse dr = null;
            if (msg != null) {
                dr = DAVFactory.createResponse(url, status, msg, doc);
            } else {
                dr = DAVFactory.createResponse(url, status, doc);
            }
            dms.addDAVNode(dr);
        }
    }

    public MultiStatusException(Document document) {
        super("Forwarded");
        this.document = document;
        this.multi = true;
    }

    public MultiStatusException(String url, String status, String msg) {
        super(url + " [" + status + "] " + msg);
        this.url = url;
        this.status = status;
        this.msg = msg;
        this.multi = false;
    }

    public MultiStatusException(String url, String status) {
        super(url + " [" + status + "]");
        this.url = url;
        this.status = status;
        this.multi = false;
    }
}
