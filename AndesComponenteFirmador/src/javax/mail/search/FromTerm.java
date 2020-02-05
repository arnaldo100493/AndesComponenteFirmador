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
public final class FromTerm extends AddressTerm {

    private static final long serialVersionUID = 5214730291502658665L;

    public FromTerm() {
        super();
    }

    public FromTerm(Address address) {
        super(address);
    }

    @Override
    public boolean match(Message msg) {
        Address[] from;
        try {
            from = msg.getFrom();
        } catch (Exception e) {
            return false;
        }

        if (from == null) {
            return false;
        }
        for (int i = 0; i < from.length; i++) {
            if (match(from[i])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FromTerm)) {
            return false;
        }
        return super.equals(obj);
    }

}
