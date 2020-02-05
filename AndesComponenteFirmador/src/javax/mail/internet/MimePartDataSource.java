/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail.internet;

import com.sun.mail.util.FolderClosedIOException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownServiceException;
import javax.activation.DataSource;
import javax.mail.FolderClosedException;
import javax.mail.MessageAware;
import javax.mail.MessageContext;
import javax.mail.MessagingException;

/**
 *
 * @author abarrime
 */
public class MimePartDataSource implements DataSource, MessageAware {

    protected MimePart part;
    private MessageContext context;

    public MimePartDataSource() {
        this.part = null;
    }

    public MimePartDataSource(MimePart part) {
        this.part = part;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            InputStream is;
            if (this.part instanceof MimeBodyPart) {
                is = ((MimeBodyPart) this.part).getContentStream();
            } else if (this.part instanceof MimeMessage) {
                is = ((MimeMessage) this.part).getContentStream();
            } else {
                throw new MessagingException("Unknown part");
            }
            String encoding = MimeBodyPart.restrictEncoding(this.part, this.part.getEncoding());

            if (encoding != null) {
                return MimeUtility.decode(is, encoding);
            }
            return is;
        } catch (FolderClosedException fex) {
            throw new FolderClosedIOException(fex.getFolder(), fex.getMessage());
        } catch (MessagingException mex) {
            throw new IOException(mex.getMessage());
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnknownServiceException("Writing not supported");
    }

    @Override
    public String getContentType() {
        try {
            return this.part.getContentType();
        } catch (MessagingException mex) {

            return "application/octet-stream";
        }
    }

    @Override
    public String getName() {
        try {
            if (this.part instanceof MimeBodyPart) {
                return ((MimeBodyPart) this.part).getFileName();
            }
        } catch (MessagingException mex) {
        }

        return "";
    }

    @Override
    public synchronized MessageContext getMessageContext() {
        if (this.context == null) {
            this.context = new MessageContext(this.part);
        }
        return this.context;
    }

}
