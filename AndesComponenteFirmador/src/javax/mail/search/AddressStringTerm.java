/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail.search;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

/**
 *
 * @author abarrime
 */
public abstract class AddressStringTerm extends StringTerm {

    private static final long serialVersionUID = 3086821234204980368L;

    protected AddressStringTerm() {
        super();
    }

    protected AddressStringTerm(String pattern) {
        super(pattern, true);
    }

    protected boolean match(Address a) {
        if (a instanceof InternetAddress) {
            InternetAddress ia = (InternetAddress) a;

            return match(ia.toUnicodeString());
        }
        return match(a.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AddressStringTerm)) {
            return false;
        }
        return super.equals(obj);
    }

}
