/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.mail.iap;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author abarrime
 */
public interface Literal {

    public void writeTo(OutputStream paramOutputStream) throws IOException;

}
