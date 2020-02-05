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
public class IllegalWriteException extends MessagingException {

    private static final long serialVersionUID = 3974370223328268013L;

    public IllegalWriteException() {
        super();
    }

    public IllegalWriteException(String s) {
        super(s);
    }

    public IllegalWriteException(String s, Exception e) {
        super(s, e);
    }
}
