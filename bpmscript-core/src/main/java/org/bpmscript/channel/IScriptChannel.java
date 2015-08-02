package org.bpmscript.channel;

import org.bpmscript.process.IDefinitionConfiguration;

/**
 * The Script Channel interface. This is how the script communicates with the outside
 * world.
 */
public interface IScriptChannel extends ISendChannel, ITimeoutChannel, IReplyChannel {

    /**
     * Get the content for a message.
     * 
     * @param message an object message
     * @return the contents of the message
     */
	Object getContent(Object message);
	
	/**
	 * Get the configuration for a definition
	 * 
	 * @param definitionName the name of the definition
	 * @return the configuration for the definition. must not be null
	 */
    IDefinitionConfiguration getDefinitionConfiguration(String definitionName);
}
