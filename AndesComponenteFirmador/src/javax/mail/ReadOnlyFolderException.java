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
public class ReadOnlyFolderException extends MessagingException {

    private transient Folder folder;
    private static final long serialVersionUID = 5711829372799039325L;

    public ReadOnlyFolderException() {
        super();
        this.folder = null;
    }

    public ReadOnlyFolderException(Folder folder) {
        this(folder, null);
    }

    public ReadOnlyFolderException(Folder folder, String message) {
        super(message);
        this.folder = folder;
    }

    public ReadOnlyFolderException(Folder folder, String message, Exception e) {
        super(message, e);
        this.folder = folder;
    }

    public Folder getFolder() {
        return this.folder;
    }
}
