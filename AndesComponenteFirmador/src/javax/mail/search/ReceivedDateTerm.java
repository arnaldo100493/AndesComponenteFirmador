/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail.search;

import java.util.Date;
import javax.mail.Message;

/**
 *
 * @author abarrime
 */
public final class ReceivedDateTerm extends DateTerm {

    private static final long serialVersionUID = -2756695246195503170L;

    public ReceivedDateTerm() {
        super();
    }

    public ReceivedDateTerm(int comparison, Date date) {
        super(comparison, date);
    }

    @Override
    public boolean match(Message msg) {
        Date d;
        try {
            d = msg.getReceivedDate();
        } catch (Exception e) {
            return false;
        }

        if (d == null) {
            return false;
        }
        return match(d);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ReceivedDateTerm)) {
            return false;
        }
        return super.equals(obj);
    }

}
