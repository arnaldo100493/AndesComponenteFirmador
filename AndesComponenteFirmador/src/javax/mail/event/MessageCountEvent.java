/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail.event;

import javax.mail.Folder;
import javax.mail.Message;

/**
 *
 * @author abarrime
 */
public class MessageCountEvent extends MailEvent {

    public static final int ADDED = 1;
    public static final int REMOVED = 2;
    protected int type;
    protected boolean removed;
    protected transient Message[] msgs;
    private static final long serialVersionUID = -7447022340837897369L;
    
    public MessageCountEvent(Object source){
        super(source);
    }

    public MessageCountEvent(Folder folder, int type, boolean removed, Message[] msgs) {
        super(folder);
        this.type = type;
        this.removed = removed;
        this.msgs = msgs;
    }

    public int getType() {
        return this.type;
    }

    public boolean isRemoved() {
        return this.removed;
    }

    public Message[] getMessages() {
        return this.msgs;
    }

    @Override
    public void dispatch(Object listener) {
        if (this.type == 1) {
            ((MessageCountListener) listener).messagesAdded(this);
        } else {
            ((MessageCountListener) listener).messagesRemoved(this);
        }
    }

}
