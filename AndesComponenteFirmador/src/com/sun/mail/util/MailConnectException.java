/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.mail.util;

import javax.mail.MessagingException;

/**
 *
 * @author abarrime
 */
public class MailConnectException extends MessagingException {

    private String host;
    private int port;
    private int cto;
    private static final long serialVersionUID = -3818807731125317729L;
    
    public MailConnectException(){
        
    }

    public MailConnectException(SocketConnectException cex) {
        super("Couldn't connect to host, port: " + cex.getHost() + ", " + cex.getPort() + "; timeout " + cex.getConnectionTimeout() + ((cex.getMessage() != null) ? ("; " + cex.getMessage()) : ""));

        this.host = cex.getHost();
        this.port = cex.getPort();
        this.cto = cex.getConnectionTimeout();
        setNextException(cex.getException());
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public int getConnectionTimeout() {
        return this.cto;
    }

}
