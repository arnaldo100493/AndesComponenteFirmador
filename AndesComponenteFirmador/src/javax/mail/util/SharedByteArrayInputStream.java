/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.mail.internet.SharedInputStream;

/**
 *
 * @author abarrime
 */
public class SharedByteArrayInputStream extends ByteArrayInputStream implements SharedInputStream {

    protected int start = 0;

    public SharedByteArrayInputStream(byte[] buf) {
        super(buf);
    }

    public SharedByteArrayInputStream(byte[] buf, int offset, int length) {
        super(buf, offset, length);
        this.start = offset;
    }

    @Override
    public long getPosition() {
        return (this.pos - this.start);
    }

    @Override
    public InputStream newStream(long start, long end) {
        if (start < 0L) {
            throw new IllegalArgumentException("start < 0");
        }
        if (end == -1L) {
            end = (this.count - this.start);
        }
        return new SharedByteArrayInputStream(this.buf, this.start + (int) start, (int) (end - start));
    }

}
