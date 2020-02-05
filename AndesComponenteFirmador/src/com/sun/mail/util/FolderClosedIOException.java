/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.mail.util;

import java.io.IOException;
import javax.mail.Folder;

/**
 *
 * @author abarrime
 */
public class FolderClosedIOException extends IOException {

    private transient Folder folder;
    private static final long serialVersionUID = 4281122580365555735L;
    
    public FolderClosedIOException(){
        super();
    }

    public FolderClosedIOException(Folder folder) {
        this(folder, null);
    }

    public FolderClosedIOException(Folder folder, String message) {
        super(message);
        this.folder = folder;
    }

    public Folder getFolder() {
        return this.folder;
    }

}
