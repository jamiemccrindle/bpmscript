package org.bpmscript.integration.internal.jms;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

/**
 * Converts objects to {@link ObjectMessage}'s and back again
 */
public class SerializingMessageConverter implements MessageConverter {
    
    /**
     * Create an {@link ObjectMessage} from an object.
     */
    public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
        if (object instanceof Serializable) {
            Serializable serializable = (Serializable) object;
            ObjectMessage objectMessage = session.createObjectMessage();
            objectMessage.setObject(serializable);
            return objectMessage;
        } else {
            throw new MessageConversionException("object " + object + " not serializable");
        }
    }

    /**
     * Gets an Object from an {@link ObjectMessage}
     */
    public Object fromMessage(Message message) throws JMSException, MessageConversionException {
        if (message instanceof ObjectMessage) {
            ObjectMessage objectMessage = (ObjectMessage) message;
            Serializable object = objectMessage.getObject();
            return object;
        } else {
            throw new MessageConversionException("message " + message + " is not an ObjectMessage");
        }
    }
}