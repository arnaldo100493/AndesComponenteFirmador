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
public class ConnectionException extends ProtocolException {

    private transient Protocol p;
    private static final long serialVersionUID = 5749739604257464727L;

    public ConnectionException() {
    }

    public ConnectionException(String s) {
        super(s);
    }

    public ConnectionException(Protocol p, Response r) {
        super(r);
        this.p = p;
    }

    public Protocol getProtocol() {
        return this.p;
    }

}
