/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail;

import java.io.Serializable;

/**
 *
 * @author abarrime
 */
public abstract class Address implements Serializable {

    private static final long serialVersionUID = -5822459626751992278L;

    public Address() {

    }

    public abstract String getType();

    @Override
    public abstract String toString();

    @Override
    public abstract boolean equals(Object paramObject);

}
