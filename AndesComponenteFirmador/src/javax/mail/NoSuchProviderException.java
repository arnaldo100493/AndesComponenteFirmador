/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail;

/**
 *
 * @author abarrime
 */
public class NoSuchProviderException extends MessagingException {

    private static final long serialVersionUID = 8058319293154708827L;

    public NoSuchProviderException() {
        super();
    }

    public NoSuchProviderException(String message) {
        super(message);
    }

    public NoSuchProviderException(String message, Exception e) {
        super(message, e);
    }

}
