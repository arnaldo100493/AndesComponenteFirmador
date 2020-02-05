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
public class MethodNotSupportedException extends MessagingException {

    private static final long serialVersionUID = -3757386618726131322L;

    public MethodNotSupportedException() {
        super();
    }

    public MethodNotSupportedException(String s) {
        super(s);
    }

    public MethodNotSupportedException(String s, Exception e) {
        super(s, e);
    }

}
