/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail.search;

import javax.mail.Address;
import javax.mail.Message;

/**
 *
 * @author abarrime
 */
public final class RecipientTerm extends AddressTerm {

    private Message.RecipientType type;
    private static final long serialVersionUID = 6548700653122680468L;

    public RecipientTerm() {
        super();
        this.type = null;
    }

    public RecipientTerm(Message.RecipientType type, Address address) {
        super(address);
        this.type = type;
    }

    public Message.RecipientType getRecipientType() {
        return this.type;
    }

    public void setRecipientType(Message.RecipientType type) {
        this.type = type;
    }

    @Override
    public boolean match(Message msg) {
        Address[] recipients;
        try {
            recipients = msg.getRecipients(this.type);
        } catch (Exception e) {
            return false;
        }

        if (recipients == null) {
            return false;
        }
        for (int i = 0; i < recipients.length; i++) {
            if (match(recipients[i])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RecipientTerm)) {
            return false;
        }
        RecipientTerm rt = (RecipientTerm) obj;
        return (rt.type.equals(this.type) && super.equals(obj));

    }
}
