/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail.search;

import javax.mail.Address;

/**
 *
 * @author abarrime
 */
public abstract class AddressTerm extends SearchTerm {

    protected Address address;
    private static final long serialVersionUID = 2005405551929769980L;

    protected AddressTerm() {
        this.address = null;
    }

    protected AddressTerm(Address address) {
        this.address = address;
    }

    public Address getAddress() {
        return this.address;
    }

    protected boolean match(Address a) {
        return a.equals(this.address);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof AddressTerm)) {
            return false;
        }
        AddressTerm at = (AddressTerm) obj;
        return at.address.equals(this.address);
    }

    public int hashCode() {
        return this.address.hashCode();
    }

}
