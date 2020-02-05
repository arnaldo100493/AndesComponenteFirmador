/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail.event;

/**
 *
 * @author abarrime
 */
public abstract class TransportAdapter implements TransportListener {

    public TransportAdapter() {

    }

    @Override
    public void messageDelivered(TransportEvent e) {

    }

    @Override
    public void messageNotDelivered(TransportEvent e) {

    }

    @Override
    public void messagePartiallyDelivered(TransportEvent e) {

    }
}
