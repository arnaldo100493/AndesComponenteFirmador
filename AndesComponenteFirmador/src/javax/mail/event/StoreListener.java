/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail.event;

import java.util.EventListener;

/**
 *
 * @author abarrime
 */
public interface StoreListener extends EventListener {

    public void notification(StoreEvent paramStoreEvent);

}