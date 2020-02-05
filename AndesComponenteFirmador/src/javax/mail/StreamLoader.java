/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author abarrime
 */
interface StreamLoader {

    void load(InputStream paramInputStream) throws IOException;
}
