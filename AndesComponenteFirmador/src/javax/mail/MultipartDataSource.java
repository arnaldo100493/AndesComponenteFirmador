/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail;

import javax.activation.DataSource;

/**
 *
 * @author abarrime
 */
public interface MultipartDataSource extends DataSource {

    public int getCount();

    public BodyPart getBodyPart(int paramInt) throws MessagingException;
}
