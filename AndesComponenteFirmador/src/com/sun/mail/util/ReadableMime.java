/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.mail.util;

import java.io.InputStream;
import javax.mail.MessagingException;
/**
 *
 * @author abarrime
 */
public interface ReadableMime {
    
    public InputStream getMimeStream() throws MessagingException;
    
}
