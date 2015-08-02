package org.bpmscript.exec;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.bpmscript.BpmScriptException;
import org.bpmscript.channel.IScriptChannel;
import org.bpmscript.process.DefinitionConfiguration;
import org.bpmscript.process.IDefinitionConfiguration;
import org.mozilla.javascript.Scriptable;

/**
 * A test script channel that records what was sent to it
 */
public class TestScriptChannel implements IScriptChannel {

	private Map<String, BlockingQueue<Object>> replies = new ConcurrentHashMap<String, BlockingQueue<Object>>();
    private DefinitionConfiguration definitionConfiguration;
	
	public TestScriptChannel() {
        definitionConfiguration = new DefinitionConfiguration();
        definitionConfiguration.getProperties().put("unserializeable", new UnserializeableObject());
	}
	
	public void reply(String pid, Object in, Object out) throws BpmScriptException {
		BlockingQueue<Object> blockingQueue = null;
		synchronized(replies) {
			blockingQueue = replies.get(pid);
			if(blockingQueue == null) {
				blockingQueue = new LinkedBlockingQueue<Object>();
				replies.put(pid, blockingQueue);
			}
		}
		blockingQueue.add(out);
	}

	public Object getReply(String pid, long duration) throws InterruptedException {
		BlockingQueue<Object> blockingQueue = null;
		synchronized(replies) {
			blockingQueue = replies.get(pid);
			if(blockingQueue == null) {
				blockingQueue = new LinkedBlockingQueue<Object>();
				replies.put(pid, blockingQueue);
			}
		}
		return blockingQueue.poll(duration, TimeUnit.MILLISECONDS);
	}

    public void send(String fromPid, String fromBranch, String fromVersion, String queueId, Object message)
            throws BpmScriptException {
        throw new BpmScriptException("unsupported operation");
    }

    public void sendTimeout(String fromPid, String fromBranch, String fromVersion, String queueId, long duration)
            throws BpmScriptException {
        // noop
    }

    /**
     * @see org.bpmscript.channel.IScriptChannel#getContent(java.lang.Object)
     */
    public Object getContent(Scriptable scope, Object message) {
        return null;
    }

    /**
     * @see org.bpmscript.channel.ISendChannel#sendOneWay(java.lang.Object)
     */
    public void sendOneWay(Object message) throws BpmScriptException {
        throw new BpmScriptException("unsupported operation");
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
        return definitionConfiguration;
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
