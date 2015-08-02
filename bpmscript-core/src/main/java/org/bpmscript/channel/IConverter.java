package org.bpmscript.channel;

/**
 * A converter / transformer interface
 */
public interface IConverter {
    /**
     * Transform "in" to an out message.
     * 
     * @param in the message to transform
     * @return the transformed message
     */
	Object convert(Object in);
}
