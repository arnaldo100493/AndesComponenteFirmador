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
public interface FolderListener {

    public void folderCreated(FolderEvent paramFolderEvent);

    public void folderDeleted(FolderEvent paramFolderEvent);

    public void folderRenamed(FolderEvent paramFolderEvent);

}
