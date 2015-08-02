package org.bpmscript.integration.internal;

public class PausingService {
    
    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    private long pauseTimeMillis = 10000;
    
	public void pause() {
		try {
		    log.debug("Pausing for " + pauseTimeMillis + " milliseconds");
			Thread.sleep(pauseTimeMillis);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

    public void setPauseTimeMillis(long pauseTimeMillis) {
        this.pauseTimeMillis = pauseTimeMillis;
    }
}
