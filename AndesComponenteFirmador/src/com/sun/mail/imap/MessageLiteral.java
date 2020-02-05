/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.mail.imap;

import com.sun.mail.iap.Literal;
import com.sun.mail.util.CRLFOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.mail.Message;
import javax.mail.MessagingException;

/**
 *
 * @author abarrime
 */
class MessageLiteral implements Literal {

    private Message msg;
    private int msgSize = -1;

    private byte[] buf;
    
    public MessageLiteral(){
        
    }

    public MessageLiteral(Message msg, int maxsize) throws MessagingException, IOException {
        this.msg = msg;

        LengthCounter lc = new LengthCounter(maxsize);
        CRLFOutputStream cRLFOutputStream = new CRLFOutputStream(lc);
        msg.writeTo((OutputStream) cRLFOutputStream);
        cRLFOutputStream.flush();
        this.msgSize = lc.getSize();
        this.buf = lc.getBytes();
    }

    public int size() {
        return this.msgSize;
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        try {
            if (this.buf != null) {
                os.write(this.buf, 0, this.msgSize);
            } else {
                CRLFOutputStream cRLFOutputStream = new CRLFOutputStream(os);
                this.msg.writeTo((OutputStream) cRLFOutputStream);
            }
        } catch (MessagingException mex) {

            throw new IOException("MessagingException while appending message: " + mex);
        }
    }
}
