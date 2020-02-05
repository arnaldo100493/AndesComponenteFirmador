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
public class ProtocolException extends Exception {

    protected transient Response response = null;

    private static final long serialVersionUID = -4360500807971797439L;

    public ProtocolException() {
    }

    public ProtocolException(String message) {
        super(message);
    }

    public ProtocolException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProtocolException(Response r) {
        super(r.toString());
        this.response = r;
    }

    public Response getResponse() {
        return this.response;
    }

}
