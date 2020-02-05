/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail.event;

import javax.mail.Store;

/**
 *
 * @author abarrime
 */
public class StoreEvent extends MailEvent {

    public static final int ALERT = 1;
    public static final int NOTICE = 2;
    protected int type;
    protected String message;
    private static final long serialVersionUID = 1938704919992515330L;
    
    public StoreEvent(Object source){
        super(source);
    }

    public StoreEvent(Store store, int type, String message) {
        super(store);
        this.type = type;
        this.message = message;
    }

    public int getMessageType() {
        return this.type;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public void dispatch(Object listener) {
        ((StoreListener) listener).notification(this);
    }

}
