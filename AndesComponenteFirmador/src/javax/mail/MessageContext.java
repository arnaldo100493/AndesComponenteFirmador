/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail;

/**
 *
 * @author abarrime
 */
public class MessageContext {

    private Part part;

    public MessageContext() {
        this.part = null;
    }

    public MessageContext(Part part) {
        this.part = part;
    }

    public Part getPart() {
        return this.part;
    }

    public Message getMessage() {
        try {
            return getMessage(this.part);
        } catch (MessagingException ex) {
            return null;
        }
    }

    private static Message getMessage(Part p) throws MessagingException {
        while (p != null) {
            if (p instanceof Message) {
                return (Message) p;
            }
            BodyPart bp = (BodyPart) p;
            Multipart mp = bp.getParent();
            if (mp == null) {
                return null;
            }
            p = mp.getParent();
        }
        return null;
    }

    public Session getSession() {
        Message msg = getMessage();
        return (msg != null) ? msg.getSession() : null;
    }

}
