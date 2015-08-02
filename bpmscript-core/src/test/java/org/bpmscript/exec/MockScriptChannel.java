package org.bpmscript.exec;

import org.bpmscript.BpmScriptException;
import org.bpmscript.channel.IScriptChannel;
import org.bpmscript.process.DefinitionConfiguration;
import org.bpmscript.process.IDefinitionConfiguration;
import org.mozilla.javascript.Scriptable;

public class MockScriptChannel implements IScriptChannel {

	private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
			.getLog(getClass());
	
	public void reply(String pid, Object in, Object out) throws BpmScriptException {
		log.info("reply:" + String.valueOf(out));
	}

    public void send(String fromPid, String fromBranch, String fromVersion, String queueId, Object message)
            throws BpmScriptException {
        log.info("send:" + fromPid + " " + String.valueOf(message));
    }

    public void sendTimeout(String fromPid, String fromBranch, String fromVersion, String queueId, long duration)
            throws BpmScriptException {
        log.info("sendTimeout:" + fromPid + " " + duration);
    }

    /* (non-Javadoc)
     * @see org.bpmscript.channel.IScriptChannel#getContent(java.lang.Object)
     */
    public Object getContent(Scriptable scope, Object message) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.bpmscript.channel.ISendChannel#sendOneWay(java.lang.Object)
     */
    public void sendOneWay(Object message) throws BpmScriptException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.bpmscript.channel.IScriptChannel#getContent(java.lang.Object)
     */
    public Object getContent(Object message) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.bpmscript.channel.IScriptChannel#getDefinitionConfiguration(java.lang.String)
     */
    public IDefinitionConfiguration getDefinitionConfiguration(String definitionName) {
        // TODO Auto-generated method stub
        return new DefinitionConfiguration();
    }

    /* (non-Javadoc)
     * @see org.bpmscript.channel.ISendChannel#createCallback(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
     */
    public Object createCallback(String fromPid, String fromBranch, String fromVersion, String queueId, Object message)
            throws BpmScriptException {
        // TODO Auto-generated method stub
        return null;
    }

}
