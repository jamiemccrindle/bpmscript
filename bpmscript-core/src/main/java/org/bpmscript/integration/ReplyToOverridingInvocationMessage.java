package org.bpmscript.integration;

import org.bpmscript.integration.internal.IInvocationMessage;
import org.bpmscript.integration.internal.InvocationMessageDecorator;

/**
 * Send message
 */
public class ReplyToOverridingInvocationMessage extends InvocationMessageDecorator {

    private static final long serialVersionUID = -6884763265897220166L;
    private final String replyToName;

    /**
     * @param delegate
     */
    public ReplyToOverridingInvocationMessage(IInvocationMessage delegate, String replyToName) {
        super(delegate);
        this.replyToName = replyToName;
    }

    @Override
    public String getReplyTo() {
        return replyToName + ":" + super.getReplyTo();
    }
}
