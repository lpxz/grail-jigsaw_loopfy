package org.w3c.tools.resources;

public class MetaDataFrame extends ResourceFrame {

    /**
     * Get our target resource.
     */
    public Resource getTargetResource() {
        Resource target = (Resource) getResource();
        edu.hkust.clap.monitor.Monitor.loopBegin(626);
while (target instanceof ResourceFrame) { 
edu.hkust.clap.monitor.Monitor.loopInc(626);
{
            target = ((ResourceFrame) target).getResource();
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(626);

        return target;
    }

    /**
     * Perform the request, return null in MetaDataFrame.
     * @param request the incomming request
     * @exception ProtocolException If an error relative to the protocol occurs
     * @exception ResourceException If an error not relative to the 
     * protocol occurs
     */
    public ReplyInterface perform(RequestInterface request) throws ProtocolException, ResourceException {
        return null;
    }
}
