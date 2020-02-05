/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail.event;

/**
 *
 * @author abarrime
 */
public interface MessageCountListener {

    public void messagesAdded(MessageCountEvent paramMessageCountEvent);

    public void messagesRemoved(MessageCountEvent paramMessageCountEvent);

}
