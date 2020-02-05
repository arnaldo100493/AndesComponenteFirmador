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
public class LiteralException extends ProtocolException {

    private static final long serialVersionUID = -6919179828339609913L;

    public LiteralException() {
        super();
    }

    public LiteralException(Response r) {
        super(r.toString());
        this.response = r;
    }

}
