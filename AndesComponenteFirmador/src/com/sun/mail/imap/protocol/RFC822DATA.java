/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ByteArray;
import com.sun.mail.iap.ParsingException;
import java.io.ByteArrayInputStream;

/**
 *
 * @author abarrime
 */
public class RFC822DATA implements Item {

    static final char[] name = new char[]{'R', 'F', 'C', '8', '2', '2'};

    public int msgno;

    public ByteArray data;

    public RFC822DATA() {

    }

    public RFC822DATA(FetchResponse r) throws ParsingException {
        this.msgno = r.getNumber();
        r.skipSpaces();
        this.data = r.readByteArray();
    }

    public ByteArray getByteArray() {
        return this.data;
    }

    public ByteArrayInputStream getByteArrayInputStream() {
        if (this.data != null) {
            return this.data.toByteArrayInputStream();
        }
        return null;
    }

}
