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
public class CommandFailedException extends ProtocolException {

    private static final long serialVersionUID = 793932807880443631L;

    public CommandFailedException() {
        super();
    }

    public CommandFailedException(String s) {
        super(s);
    }

    public CommandFailedException(Response r) {
        super(r);
    }

}
