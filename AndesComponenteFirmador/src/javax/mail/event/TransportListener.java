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
public interface TransportListener {

    public void messageDelivered(TransportEvent paramTransportEvent);

    public void messageNotDelivered(TransportEvent paramTransportEvent);

    public void messagePartiallyDelivered(TransportEvent paramTransportEvent);
}
