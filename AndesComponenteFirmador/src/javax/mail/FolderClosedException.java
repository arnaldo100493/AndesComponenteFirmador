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
public class FolderClosedException extends MessagingException {

    private transient Folder folder;
    private static final long serialVersionUID = 1687879213433302315L;

    public FolderClosedException() {
        super();
        this.folder = null;
    }

    public FolderClosedException(Folder folder) {
        this(folder, null);
    }

    public FolderClosedException(Folder folder, String message) {
        super(message);
        this.folder = folder;
    }

    public FolderClosedException(Folder folder, String message, Exception e) {
        super(message, e);
        this.folder = folder;
    }

    public Folder getFolder() {
        return this.folder;
    }

}
