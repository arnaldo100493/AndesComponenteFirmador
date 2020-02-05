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
public class DecodingException extends IOException {

    private static final long serialVersionUID = -6913647794421459390L;

    public DecodingException() {
        super();
    }

    public DecodingException(String s) {
        super(s);
    }

}
