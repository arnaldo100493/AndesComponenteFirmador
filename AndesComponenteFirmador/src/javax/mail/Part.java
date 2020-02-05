/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.activation.DataHandler;

/**
 *
 * @author abarrime
 */
public interface Part {

    public static final String ATTACHMENT = "attachment";

    public static final String INLINE = "inline";

    public int getSize() throws MessagingException;

    public int getLineCount() throws MessagingException;

    public String getContentType() throws MessagingException;

    public boolean isMimeType(String paramString) throws MessagingException;

    public String getDisposition() throws MessagingException;

    public void setDisposition(String paramString) throws MessagingException;

    public String getDescription() throws MessagingException;

    public void setDescription(String paramString) throws MessagingException;

    public String getFileName() throws MessagingException;

    public void setFileName(String paramString) throws MessagingException;

    public InputStream getInputStream() throws IOException, MessagingException;

    public DataHandler getDataHandler() throws MessagingException;

    public Object getContent() throws IOException, MessagingException;

    public void setDataHandler(DataHandler paramDataHandler) throws MessagingException;

    public void setContent(Object paramObject, String paramString) throws MessagingException;

    public void setText(String paramString) throws MessagingException;

    public void setContent(Multipart paramMultipart) throws MessagingException;

    public void writeTo(OutputStream paramOutputStream) throws IOException, MessagingException;

    public String[] getHeader(String paramString) throws MessagingException;

    public void setHeader(String paramString1, String paramString2) throws MessagingException;

    public void addHeader(String paramString1, String paramString2) throws MessagingException;

    public void removeHeader(String paramString) throws MessagingException;

    public Enumeration getAllHeaders() throws MessagingException;

    public Enumeration getMatchingHeaders(String[] paramArrayOfString) throws MessagingException;

    public Enumeration getNonMatchingHeaders(String[] paramArrayOfString) throws MessagingException;

}
