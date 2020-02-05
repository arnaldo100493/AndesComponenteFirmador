/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.mail.imap;

import javax.mail.Session;
import javax.mail.URLName;

/**
 *
 * @author abarrime
 */
public class IMAPSSLStore extends IMAPStore {

    public IMAPStore() {

    }

    public IMAPSSLStore(Session session, URLName url) {
        super(session, url, "imaps", true);
    }

}
