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
public abstract class MessageCountAdapter implements MessageCountListener {

    public MessageCountAdapter() {

    }

    public void messagesAdded(MessageCountEvent e) {
    }

    public void messagesRemoved(MessageCountEvent e) {
    }

}
