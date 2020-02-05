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
public final class SizeTerm extends IntegerComparisonTerm {

    private static final long serialVersionUID = -2556219451005103709L;

    public SizeTerm() {
        super();
    }

    public SizeTerm(int comparison, int size) {
        super(comparison, size);
    }

    @Override
    public boolean match(Message msg) {
        int size;
        try {
            size = msg.getSize();
        } catch (Exception e) {
            return false;
        }

        if (size == -1) {
            return false;
        }
        return match(size);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SizeTerm)) {
            return false;
        }
        return super.equals(obj);
    }

}