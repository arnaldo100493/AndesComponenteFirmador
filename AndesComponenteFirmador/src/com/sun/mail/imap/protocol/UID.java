/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ParsingException;

/**
 *
 * @author abarrime
 */
public class UID implements Item {

    static final char[] name = new char[]{'U', 'I', 'D'};

    public int seqnum;

    public long uid;

    public UID() {

    }

    public UID(FetchResponse r) throws ParsingException {
        this.seqnum = r.getNumber();
        r.skipSpaces();
        this.uid = r.readLong();
    }

}
