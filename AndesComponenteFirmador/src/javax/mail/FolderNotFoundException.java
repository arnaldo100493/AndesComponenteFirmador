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
public class FolderNotFoundException extends MessagingException {

    private transient Folder folder;
    private static final long serialVersionUID = 472612108891249403L;

    public FolderNotFoundException() {
        super();
    }

    public FolderNotFoundException(Folder folder) {
        this.folder = folder;
    }

    public FolderNotFoundException(Folder folder, String s) {
        super(s);
        this.folder = folder;
    }

    public FolderNotFoundException(Folder folder, String s, Exception e) {
        super(s, e);
        this.folder = folder;
    }

    public FolderNotFoundException(String s, Folder folder) {
        super(s);
        this.folder = folder;
    }

    public Folder getFolder() {
        return this.folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }
}
