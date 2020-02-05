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
public class ParsingException extends ProtocolException {

    private static final long serialVersionUID = 7756119840142724839L;

    public ParsingException() {
        super();
    }

    public ParsingException(String s) {
        super(s);
    }

    public ParsingException(Response r) {
        super(r);
    }

}
