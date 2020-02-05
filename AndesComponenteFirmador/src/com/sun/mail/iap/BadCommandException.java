/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.mail.iap;

/**
 *
 * @author abarrime
 */
public class BadCommandException extends ProtocolException {

    private static final long serialVersionUID = 5769722539397237515L;

    public BadCommandException() {
        super();
    }

    public BadCommandException(String s) {
        super(s);
    }

    public BadCommandException(Response r) {
        super(r);
    }

}
