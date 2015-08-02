package org.bpmscript.integration.internal.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

import com.thoughtworks.xstream.XStream;

/**
 * Converts objects to {@link ObjectMessage}'s and back again
 */
public class XStreamMessageConverter implements MessageConverter {
    
    private XStream xstream = new XStream();
    
    /**
     * Create an {@link ObjectMessage} from an object.
     */
    public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
        TextMessage message = session.createTextMessage();
        message.setText(xstream.toXML(object));
        return message;
    }

    /**
     * Gets an Object from an {@link ObjectMessage}
     */
    public Object fromMessage(Message message) throws JMSException, MessageConversionException {
        TextMessage textMessage = (TextMessage) message;
        return xstream.fromXML(textMessage.getText());
    }
}