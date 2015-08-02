package org.bpmscript.exec;

/**
 * An implementation of the IKillMessage interface
 */
public class KillMessage implements IKillMessage {
	
	private final String message;
    private final String pid;
    private final String branch;

	public KillMessage(String pid, String branch, String message) {
		super();
        this.pid = pid;
        this.branch = branch;
        this.message = message;
	}

	public Object getMessage() {
		return message;
	}

    public String getPid() {
        return pid;
    }

    public String getBranch() {
        return branch;
    }
    
    public String getType() {
        return "KILL";
    }

}
