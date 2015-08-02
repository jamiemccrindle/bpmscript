package org.bpmscript.channel;

import org.bpmscript.BpmScriptException;

/**
 * A channel for sending timeouts. This channel can also be used for scheduling.
 */
public interface ITimeoutChannel {
    
    /**
     * Send a timeout. This method should send the timeout and return immediately. It
     * should only throw an exception if there was a problem sending the timeout, otherwise
     * the exception should come back as a response message.
     * 
     * @param fromPid the instance that this timeout is from
     * @param fromBranch the branch that this timeout is from
     * @param fromVersion the version that this timeout is from 
     * @param queueId the queue id that this timeout is from
     * @param duration the duration in milliseconds before the timeout should return
     * @throws BpmScriptException if there was a problem creating the timeout
     */
	public void sendTimeout(String fromPid, String fromBranch, String fromVersion, String queueId, long duration) throws BpmScriptException;
}
