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
public abstract class ConnectionAdapter implements ConnectionListener {

    public ConnectionAdapter() {

    }

    public void opened(ConnectionEvent e) {
    }

    public void disconnected(ConnectionEvent e) {
    }

    public void closed(ConnectionEvent e) {
    }

}
