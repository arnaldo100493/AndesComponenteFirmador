/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail.internet;

/**
 *
 * @author abarrime
 */
public class AddressException extends ParseException {

    protected String ref = null;

    protected int pos = -1;

    private static final long serialVersionUID = 9134583443539323120L;

    public AddressException() {
        super();
    }

    public AddressException(String s) {
        super(s);
    }

    public AddressException(String s, String ref) {
        super(s);
        this.ref = ref;
    }

    public AddressException(String s, String ref, int pos) {
        super(s);
        this.ref = ref;
        this.pos = pos;
    }

    public String getRef() {
        return this.ref;
    }

    public int getPos() {
        return this.pos;
    }

    @Override
    public String toString() {
        String s = super.toString();
        if (this.ref == null) {
            return s;
        }
        s = s + " in string ``" + this.ref + "''";
        if (this.pos < 0) {
            return s;
        }
        return s + " at position " + this.pos;
    }

}
