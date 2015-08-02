package org.bpmscript.channel;

import org.bpmscript.BpmScriptException;

/**
 * Send a message from an instance no a particular branch and version from
 * a specific queue within the instance.
 */
public interface ISendChannel {
    
    /**
     * Send a Request-Response message. This could result in single response (future) or 
     * multiple responses (queue)
     * 
     * @param fromPid the process instance id
     * @param fromBranch the branch
     * @param fromVersion the version
     * @param queueId the queue id
     * @param message the message to be sent
     * @throws BpmScriptException if something goes wrong converting the message
     */
    public void send(String fromPid, String fromBranch, String fromVersion, String queueId, Object message) throws BpmScriptException;
    
    /**
     * Creates a callback object (i.e. message) that can then be attached to a queue.
     * 
     * @param fromPid the process instance id
     * @param fromBranch the branch
     * @param fromVersion the version
     * @param queueId the queue id
     * @param message the message to be sent
     * @throws BpmScriptException if something goes wrong converting the message
     */
    public Object createCallback(String fromPid, String fromBranch, String fromVersion, String queueId, Object message) throws BpmScriptException;
    
    /**
     * Send a one way message that doesn't expect a response
     * 
     * @param message the message to send
     * @throws BpmScriptException if something goes wrong converting the message or sending it
     */
    public void sendOneWay(Object message) throws BpmScriptException;
}
