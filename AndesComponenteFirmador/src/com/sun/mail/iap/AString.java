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
class AString {

    byte[] bytes;

    AString() {
        this.bytes = new byte[1000];
    }

    AString(byte[] b) {
        this.bytes = b;
    }

}
