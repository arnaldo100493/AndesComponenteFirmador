/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail.event;

import java.util.EventObject;

/**
 *
 * @author abarrime
 */
public abstract class MailEvent extends EventObject {

    private static final long serialVersionUID = 1846275636325456631L;

    public MailEvent(Object source) {
        super(source);
    }

    public abstract void dispatch(Object paramObject);

}
