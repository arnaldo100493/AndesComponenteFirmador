/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail.search;

import javax.mail.MessagingException;

/**
 *
 * @author abarrime
 */
public class SearchException extends MessagingException {

    private static final long serialVersionUID = -7092886778226268686L;

    public SearchException() {
        super();
    }

    public SearchException(String s) {
        super(s);
    }

}
