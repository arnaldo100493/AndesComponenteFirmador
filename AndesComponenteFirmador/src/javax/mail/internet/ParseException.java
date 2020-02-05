/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail.internet;

import javax.mail.MessagingException;

/**
 *
 * @author abarrime
 */
public class ParseException extends MessagingException {

    private static final long serialVersionUID = 7649991205183658089L;

    public ParseException() {
        super();
    }

    public ParseException(String s) {
        super(s);
    }

}
