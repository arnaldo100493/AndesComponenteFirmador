/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail.internet;

import java.io.InputStream;

/**
 *
 * @author abarrime
 */
public interface SharedInputStream {

    public long getPosition();

    public InputStream newStream(long paramLong1, long paramLong2);
}
