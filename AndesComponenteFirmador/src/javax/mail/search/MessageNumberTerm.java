/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail.search;

import javax.mail.Message;

/**
 *
 * @author abarrime
 */
public final class MessageNumberTerm extends IntegerComparisonTerm {

    private static final long serialVersionUID = -5379625829658623812L;

    public MessageNumberTerm() {
        super();
    }

    public MessageNumberTerm(int number) {
        super(3, number);
    }

    @Override
    public boolean match(Message msg) {
        int msgno;
        try {
            msgno = msg.getMessageNumber();
        } catch (Exception e) {
            return false;
        }

        return match(msgno);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MessageNumberTerm)) {
            return false;
        }
        return super.equals(obj);
    }

}
