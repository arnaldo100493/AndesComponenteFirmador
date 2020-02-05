/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.mail.util;

import java.io.IOException;

/**
 *
 * @author abarrime
 */
public class SocketConnectException extends IOException {

    private String host;
    private int port;
    private int cto;
    private static final long serialVersionUID = 3997871560538755463L;

    public SocketConnectException() {
        super();
        initCause(this);
        this.host = "";
        this.port = 0;
        this.cto = 0;
    }

    public SocketConnectException(String msg, Exception cause, String host, int port, int cto) {
        super(msg);
        initCause(cause);
        this.host = host;
        this.port = port;
        this.cto = cto;
    }

    public Exception getException() {
        assert getCause() instanceof Exception;
        return (Exception) getCause();
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
