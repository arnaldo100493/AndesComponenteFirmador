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
public final class MessageIDTerm extends StringTerm {

    private static final long serialVersionUID = -2121096296454691963L;

    public MessageIDTerm(String msgid) {
        super(msgid);
    }

    @Override
    public boolean match(Message msg) {
        String[] s;
        try {
            s = msg.getHeader("Message-ID");
        } catch (Exception e) {
            return false;
        }

        if (s == null) {
            return false;
        }
        for (int i = 0; i < s.length; i++) {
            if (match(s[i])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MessageIDTerm)) {
            return false;
        }
        return super.equals(obj);
    }

}
