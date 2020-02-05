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
public interface UIDFolder {

    public static final long LASTUID = -1L;

    public long getUIDValidity() throws MessagingException;

    public Message getMessageByUID(long paramLong) throws MessagingException;

    public Message[] getMessagesByUID(long paramLong1, long paramLong2) throws MessagingException;

    public Message[] getMessagesByUID(long[] paramArrayOflong) throws MessagingException;

    public long getUID(Message paramMessage) throws MessagingException;

    public static class FetchProfileItem
            extends FetchProfile.Item {

        protected FetchProfileItem(String name) {
            super(name);
        }

        public static final FetchProfileItem UID = new FetchProfileItem("UID");
    }

}
