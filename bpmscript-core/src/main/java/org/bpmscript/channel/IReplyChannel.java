package org.bpmscript.channel;

import org.bpmscript.BpmScriptException;

/**
 * Channel used for replying to messages
 */
public interface IReplyChannel {
    
    /**
     * Reply to a message. The reason the "in" message is needed to to handle things
     * like copying the correlation id or return address. In the case of MessageExchanges
     * the out message will be copied into the "in" message exchange.
     * 
     * @param pid the process id
     * @param in the in message or message exchange
     * @param out the out message
     * @throws BpmScriptException if there was a problem replying
     */
	void reply(String pid, Object in, Object out) throws BpmScriptException;
}
