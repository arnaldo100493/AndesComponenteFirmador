/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.mail.util;

import java.io.IOException;

/**
 *
 * @author abarrime
 */
public class MessageRemovedIOException extends IOException {

    private static final long serialVersionUID = 4280468026581616424L;

    public MessageRemovedIOException() {
        super();
    }

    public MessageRemovedIOException(String s) {
        super(s);
    }

}
