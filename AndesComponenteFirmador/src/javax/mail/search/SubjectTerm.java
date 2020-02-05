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
public final class SubjectTerm extends StringTerm {

    private static final long serialVersionUID = 7481568618055573432L;

    public SubjectTerm() {
        super();
    }

    public SubjectTerm(String pattern) {
        super(pattern);
    }

    @Override
    public boolean match(Message msg) {
        String subj;
        try {
            subj = msg.getSubject();
        } catch (Exception e) {
            return false;
        }

        if (subj == null) {
            return false;
        }
        return match(subj);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SubjectTerm)) {
            return false;
        }
        return super.equals(obj);
    }

}
