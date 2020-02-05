/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.mail.imap;

/**
 *
 * @author abarrime
 */
public class AppendUID {

    public long uidvalidity = -1L;
    public long uid = -1L;

    public AppendUID() {
        this.uidvalidity = 0L;
        this.uid = 0L;
    }

    public AppendUID(long uidvalidity, long uid) {
        this.uidvalidity = uidvalidity;
        this.uid = uid;
    }

}
