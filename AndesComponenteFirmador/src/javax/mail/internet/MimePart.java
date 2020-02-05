/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail.internet;

import java.util.Enumeration;
import javax.mail.MessagingException;
import javax.mail.Part;

/**
 *
 * @author abarrime
 */
public interface MimePart extends Part {

    public String getHeader(String paramString1, String paramString2) throws MessagingException;

    public void addHeaderLine(String paramString) throws MessagingException;

    public Enumeration getAllHeaderLines() throws MessagingException;

    public Enumeration getMatchingHeaderLines(String[] paramArrayOfString) throws MessagingException;

    public Enumeration getNonMatchingHeaderLines(String[] paramArrayOfString) throws MessagingException;

    public String getEncoding() throws MessagingException;

    public String getContentID() throws MessagingException;

    public String getContentMD5() throws MessagingException;

    public void setContentMD5(String paramString) throws MessagingException;

    public String[] getContentLanguage() throws MessagingException;

    public void setContentLanguage(String[] paramArrayOfString) throws MessagingException;

    public void setText(String paramString) throws MessagingException;

    public void setText(String paramString1, String paramString2) throws MessagingException;

    public void setText(String paramString1, String paramString2, String paramString3) throws MessagingException;

}
