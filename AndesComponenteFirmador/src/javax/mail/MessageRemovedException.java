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
public class MessageRemovedException extends MessagingException {

    private static final long serialVersionUID = 1951292550679528690L;

    public MessageRemovedException() {
        super();
    }

    public MessageRemovedException(String s) {
        super(s);
    }

    public MessageRemovedException(String s, Exception e) {
        super(s, e);
    }

}
