/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.mail.util;

import java.io.OutputStream;

/**
 *
 * @author abarrime
 */
public class BEncoderStream extends BASE64EncoderStream {

    public BEncoderStream(OutputStream out) {
        super(out, 2147483647);
    }

    public static int encodedLength(byte[] b) {
        return (b.length + 2) / 3 * 4;
    }

}
