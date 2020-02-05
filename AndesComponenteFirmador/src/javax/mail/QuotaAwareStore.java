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
public interface QuotaAwareStore {

    public Quota[] getQuota(String paramString) throws MessagingException;

    public void setQuota(Quota paramQuota) throws MessagingException;

}
