/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.mail.event.MailEvent;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;

/**
 *
 * @author abarrime
 */
public abstract class Transport extends Service {

    private volatile Vector transportListeners;

    public Transport() {
        super();
        this.transportListeners = null;
    }

    public Transport(Session session, URLName urlname) {
        super(session, urlname);
        this.transportListeners = null;
    }

    public static void send(Message msg) throws MessagingException {
        msg.saveChanges();
        send0(msg, msg.getAllRecipients(), null, null);
    }

    public synchronized void addTransportListener(TransportListener l) {
        if (this.transportListeners == null) {
            this.transportListeners = new Vector();
        }
        this.transportListeners.addElement(l);
    }

    public static void send(Message msg, Address[] addresses) throws MessagingException {
        msg.saveChanges();
        send0(msg, addresses, null, null);
    }

    public synchronized void removeTransportListener(TransportListener l) {
        if (this.transportListeners != null) {
            this.transportListeners.removeElement(l);
        }
    }

    public static void send(Message msg, String user, String password) throws MessagingException {
        msg.saveChanges();
        send0(msg, msg.getAllRecipients(), user, password);
    }

    public static void send(Message msg, Address[] addresses, String user, String password) throws MessagingException {
        msg.saveChanges();
        send0(msg, addresses, user, password);
    }

    protected void notifyTransportListeners(int type, Address[] validSent, Address[] validUnsent, Address[] invalid, Message msg) {
        if (this.transportListeners == null) {
            return;
        }
        TransportEvent e = new TransportEvent(this, type, validSent, validUnsent, invalid, msg);

        queueEvent((MailEvent) e, this.transportListeners);
    }

    private static void send0(Message msg, Address[] addresses, String user, String password) throws MessagingException {
        if (addresses == null || addresses.length == 0) {
            throw new SendFailedException("No recipient addresses");
        }
        Hashtable<Object, Object> protocols = new Hashtable<>();
        Vector<Address> invalid = new Vector();
        Vector<Address> validSent = new Vector();
        Vector<Address> validUnsent = new Vector();
        for (int i = 0; i < addresses.length; i++) {
            if (protocols.containsKey(addresses[i].getType())) {
                Vector<Address> v = (Vector) protocols.get(addresses[i].getType());
                v.addElement(addresses[i]);
            } else {
                Vector<Address> w = new Vector();
                w.addElement(addresses[i]);
                protocols.put(addresses[i].getType(), w);
            }
        }
        int dsize = protocols.size();
        if (dsize == 0) {
            throw new SendFailedException("No recipient addresses");
        }
        Session s = (msg.session != null) ? msg.session : Session.getDefaultInstance(System.getProperties(), null);
        if (dsize == 1) {
            Transport transport = s.getTransport(addresses[0]);
            try {
                if (user != null) {
                    transport.connect(user, password);
                } else {
                    transport.connect();
                }
                transport.sendMessage(msg, addresses);
            } finally {
                transport.close();
            }
            return;
        }
        MessagingException chainedEx = null;
        boolean sendFailed = false;
        Enumeration<Vector> e = protocols.elements();
        while (e.hasMoreElements()) {
            Vector v = e.nextElement();
            Address[] protaddresses = new Address[v.size()];
            v.copyInto((Object[]) protaddresses);
            Transport transport;
            if ((transport = s.getTransport(protaddresses[0])) == null) {
                for (int j = 0; j < protaddresses.length; j++) {
                    invalid.addElement(protaddresses[j]);
                }
                continue;
            }
            try {
                transport.connect();
                transport.sendMessage(msg, protaddresses);
            } catch (SendFailedException sex) {
                sendFailed = true;
                if (chainedEx == null) {
                    chainedEx = sex;
                } else {
                    chainedEx.setNextException(sex);
                }
                Address[] a = sex.getInvalidAddresses();
                if (a != null) {
                    for (int j = 0; j < a.length; j++) {
                        invalid.addElement(a[j]);
                    }
                }
                a = sex.getValidSentAddresses();
                if (a != null) {
                    for (int k = 0; k < a.length; k++) {
                        validSent.addElement(a[k]);
                    }
                }
                Address[] c = sex.getValidUnsentAddresses();
                if (c != null) {
                    for (int l = 0; l < c.length; l++) {
                        validUnsent.addElement(c[l]);
                    }
                }
            } catch (MessagingException mex) {
                sendFailed = true;
                if (chainedEx == null) {
                    chainedEx = mex;
                } else {
                    chainedEx.setNextException(mex);
                }
            } finally {
                transport.close();
            }
        }
        if (sendFailed || invalid.size() != 0 || validUnsent.size() != 0) {
            Address[] a = null, b = null, c = null;
            if (validSent.size() > 0) {
                a = new Address[validSent.size()];
                validSent.copyInto((Object[]) a);
            }
            if (validUnsent.size() > 0) {
                b = new Address[validUnsent.size()];
                validUnsent.copyInto((Object[]) b);
            }
            if (invalid.size() > 0) {
                c = new Address[invalid.size()];
                invalid.copyInto((Object[]) c);
            }
            throw new SendFailedException("Sending failed", chainedEx, a, b, c);
        }
    }

    public abstract void sendMessage(Message paramMessage, Address[] paramArrayOfAddress) throws MessagingException;

}
