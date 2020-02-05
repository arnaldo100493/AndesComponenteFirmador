/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail;

import java.util.Vector;
import javax.mail.event.FolderEvent;
import javax.mail.event.FolderListener;
import javax.mail.event.MailEvent;
import javax.mail.event.StoreEvent;
import javax.mail.event.StoreListener;

/**
 *
 * @author abarrime
 */
public abstract class Store extends Service {

    private volatile Vector storeListeners;
    private volatile Vector folderListeners;

    protected Store(Session session, URLName urlname) {
        super(session, urlname);

        this.storeListeners = null;

        this.folderListeners = null;
    }

    public abstract Folder getDefaultFolder() throws MessagingException;

    public abstract Folder getFolder(String paramString) throws MessagingException;

    public abstract Folder getFolder(URLName paramURLName) throws MessagingException;

    public Folder[] getPersonalNamespaces() throws MessagingException {
        return new Folder[]{getDefaultFolder()};
    }

    public synchronized void addFolderListener(FolderListener l) {
        if (this.folderListeners == null) {
            this.folderListeners = new Vector();
        }
        this.folderListeners.addElement(l);
    }

    public Folder[] getUserNamespaces(String user) throws MessagingException {
        return new Folder[0];
    }

    public Folder[] getSharedNamespaces() throws MessagingException {
        return new Folder[0];
    }

    public synchronized void removeFolderListener(FolderListener l) {
        if (this.folderListeners != null) {
            this.folderListeners.removeElement(l);
        }
    }

    public synchronized void addStoreListener(StoreListener l) {
        if (this.storeListeners == null) {
            this.storeListeners = new Vector();
        }
        this.storeListeners.addElement(l);
    }

    protected void notifyFolderListeners(int type, Folder folder) {
        if (this.folderListeners == null) {
            return;
        }
        FolderEvent e = new FolderEvent(this, folder, type);
        queueEvent((MailEvent) e, this.folderListeners);
    }

    public synchronized void removeStoreListener(StoreListener l) {
        if (this.storeListeners != null) {
            this.storeListeners.removeElement(l);
        }
    }

    protected void notifyStoreListeners(int type, String message) {
        if (this.storeListeners == null) {
            return;
        }
        StoreEvent e = new StoreEvent(this, type, message);
        queueEvent((MailEvent) e, this.storeListeners);
    }

    protected void notifyFolderRenamedListeners(Folder oldF, Folder newF) {
        if (this.folderListeners == null) {
            return;
        }
        FolderEvent e = new FolderEvent(this, oldF, newF, 3);
        queueEvent((MailEvent) e, this.folderListeners);
    }

}
