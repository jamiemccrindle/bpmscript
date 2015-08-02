package org.bpmscript.channel;

/**
 * A Synchronous Channel. Typically used as follows:
 * 
 * try {
 *   sync.expect(id);
 *   
 *   do something that would result in a message being returned
 *   
 *   Object message = sync.get(id, 100000);
 * } finally {
 *   sync.close(id);
 * }
 */
public interface ISyncChannel {

    /**
     * Expect an id. This sets the sync channel up to wait for a message
     * with that id.
     * 
     * "close" must be called or the id won't be cleaned up...
     * 
     * @param id the id to expect
     */
	void expect(String id);
	
	/**
	 * Stop expecting messages with a particular id, "expect" should have
	 * been called first.
	 * 
	 * @param id the id to stop expecting messages for
	 */
	void close(String id);
	
	/**
	 * Wait for a message with a particular id for a particular duration. 
	 * Returns null if the message didn't show up.
	 * 
	 * @param id the id for which a message is expected
	 * @param duration the duration to wait for in milliseconds
	 * @return a message that comes back for that id
	 * @throws InterruptedException if the waiting was interrupted
	 */
	Object get(String id, long duration) throws InterruptedException;

}