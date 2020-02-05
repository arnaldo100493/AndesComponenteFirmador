/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ParsingException;
import com.sun.mail.iap.Response;
import java.util.Date;
import java.util.Vector;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;

/**
 *
 * @author abarrime
 */
public class ENVELOPE implements Item {

    static final char[] name = new char[]{'E', 'N', 'V', 'E', 'L', 'O', 'P', 'E'};

    public int msgno;
    public Date date = null;

    public String subject;

    public InternetAddress[] from;
    public InternetAddress[] sender;
    public InternetAddress[] replyTo;
    public InternetAddress[] to;
    public InternetAddress[] cc;
    public InternetAddress[] bcc;
    public String inReplyTo;
    public String messageId;
    private static MailDateFormat mailDateFormat = new MailDateFormat();

    public ENVELOPE() {

    }

    public ENVELOPE(FetchResponse r) throws ParsingException {
        this.msgno = r.getNumber();

        r.skipSpaces();

        if (r.readByte() != 40) {
            throw new ParsingException("ENVELOPE parse error");
        }
        String s = r.readString();
        if (s != null) {
            try {
                this.date = mailDateFormat.parse(s);
            } catch (Exception pex) {
            }
        }

        this.subject = r.readString();
        this.from = parseAddressList(r);
        this.sender = parseAddressList(r);
        this.replyTo = parseAddressList(r);
        this.to = parseAddressList(r);
        this.cc = parseAddressList(r);
        this.bcc = parseAddressList(r);
        this.inReplyTo = r.readString();
        this.messageId = r.readString();

        if (r.readByte() != 41) {
            throw new ParsingException("ENVELOPE parse error");
        }
    }

    private InternetAddress[] parseAddressList(Response r) throws ParsingException {
        r.skipSpaces();

        byte b = r.readByte();
        if (b == 40) {
            Vector<IMAPAddress> v = new Vector();

            do {
                IMAPAddress a = new IMAPAddress(r);

                if (a.isEndOfGroup()) {
                    continue;
                }
                v.addElement(a);
            } while (r.peekByte() != 41);

            r.skip(1);

            InternetAddress[] a = new InternetAddress[v.size()];
            v.copyInto((Object[]) a);
            return a;
        }
        if (b == 78 || b == 110) {
            r.skip(2);
            return null;
        }
        throw new ParsingException("ADDRESS parse error");
    }

}
