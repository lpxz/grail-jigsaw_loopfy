package org.w3c.jigsaw.proxy;

import java.io.IOException;
import java.io.InputStream;
import org.w3c.jigsaw.http.Reply;
import org.w3c.www.protocol.http.ContinueEvent;
import org.w3c.www.protocol.http.Request;
import org.w3c.www.protocol.http.RequestEvent;
import org.w3c.www.protocol.http.RequestObserver;

public class ProxyRequestObserver implements RequestObserver {

    /**
     * Our client, ie the one that sends us a request to fulfill.
     */
    org.w3c.jigsaw.http.Request request = null;

    ForwardFrame frame = null;

    /**
     * Call back, invoked by the HttpManager callback thread.
     * Each time a request status changes (due to progress in its processing)
     * this callback gets called, with the new status as an argument.
     * @param preq The pending request that has made some progress.
     * @param event The event to broadcast.
     */
    public void notifyProgress(RequestEvent event) {
        Request req = event.request;
        if (event instanceof ContinueEvent) {
            ContinueEvent cevent = (ContinueEvent) event;
            if ((req.getMajorVersion() == 1) && (req.getMinorVersion() == 1)) {
                try {
                    if ((cevent.packet != null) && (frame != null)) {
                        Reply r = null;
                        try {
                            r = frame.dupReply(request, cevent.packet);
                            r.setStream((InputStream) null);
                        } catch (Exception ex) {
                        }
                        request.getClient().sendContinue(r);
                    } else {
                        request.getClient().sendContinue();
                    }
                } catch (IOException ex) {
                }
            }
        }
    }

    public ProxyRequestObserver(org.w3c.jigsaw.http.Request request) {
        this.request = request;
    }

    public ProxyRequestObserver(org.w3c.jigsaw.http.Request request, ForwardFrame frame) {
        this.request = request;
        this.frame = frame;
    }
}
