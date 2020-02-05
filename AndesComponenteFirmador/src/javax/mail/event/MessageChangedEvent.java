/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail.event;

import javax.mail.Message;

/**
 *
 * @author abarrime
 */
public class MessageChangedEvent extends MailEvent {

    public static final int FLAGS_CHANGED = 1;
    public static final int ENVELOPE_CHANGED = 2;
    protected int type;
    protected transient Message msg;
    private static final long serialVersionUID = -4974972972105535108L;

    public MessageChangedEvent(Object source, int type, Message msg) {
        super(source);
        this.msg = msg;
        this.type = type;
    }

    public int getMessageChangeType() {
        return this.type;
    }

    public Message getMessage() {
        return this.msg;
    }

    @Override
    public void dispatch(Object listener) {
        ((MessageChangedListener) listener).messageChanged(this);
    }

}
