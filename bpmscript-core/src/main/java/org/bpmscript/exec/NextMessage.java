package org.bpmscript.exec;

/**
 * An immutable value object implementation of the INextMessage interface.
 */
public class NextMessage implements INextMessage {
	
	private final String queueId;
	private final Object message;
    private final String pid;
    private final String branch;
    private final String version;

	public NextMessage(String pid, String branch, String version, String queueId, Object message) {
		super();
        this.pid = pid;
        this.branch = branch;
        this.version = version;
		this.queueId = queueId;
		this.message = message;
	}

	public Object getMessage() {
		return message;
	}

	public String getQueueId() {
		return queueId;
	}

    public String getPid() {
        return pid;
    }

    public String getBranch() {
        return branch;
    }

    public String getVersion() {
        return version;
    }
    
    public String getType() {
        return "SCRIPT";
    }

}
