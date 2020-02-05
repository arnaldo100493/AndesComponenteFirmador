/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.mail.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.mail.util.SharedByteArrayInputStream;

/**
 *
 * @author abarrime
 */
public class SharedByteArrayOutputStream extends ByteArrayOutputStream {
    
    public SharedByteArrayOutputStream(){
        super();
    }

    public SharedByteArrayOutputStream(int size) {
        super(size);
    }

    public InputStream toStream() {
        return (InputStream) new SharedByteArrayInputStream(this.buf, 0, this.count);
    }

}
