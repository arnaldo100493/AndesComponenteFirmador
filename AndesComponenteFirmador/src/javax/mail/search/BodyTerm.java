/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail.search;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;

/**
 *
 * @author abarrime
 */
public final class BodyTerm extends StringTerm {

    private static final long serialVersionUID = -4888862527916911385L;

    public BodyTerm() {
        super();
    }

    public BodyTerm(String pattern) {
        super(pattern);
    }

    @Override
    public boolean match(Message msg) {
        return matchPart((Part) msg);
    }

    private boolean matchPart(Part p) {
        try {
            if (p.isMimeType("text/*")) {
                String s = (String) p.getContent();
                if (s == null) {
                    return false;
                }

                return match(s);
            }
            if (p.isMimeType("multipart/*")) {
                Multipart mp = (Multipart) p.getContent();
                int count = mp.getCount();
                for (int i = 0; i < count; i++) {
                    if (matchPart((Part) mp.getBodyPart(i))) {
                        return true;
                    }
                }
            } else if (p.isMimeType("message/rfc822")) {
                return matchPart((Part) p.getContent());
            }
        } catch (Exception ex) {
        }

        return false;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof BodyTerm)) {
            return false;
        }
        return super.equals(obj);
    }

}
