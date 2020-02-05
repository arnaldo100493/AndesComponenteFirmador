/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bouncycastle.asn1;

import java.io.IOException;
import java.io.OutputStream;


/**
 *
 * @author abarrime
 */
public class ASN1OutputStream extends DEROutputStream {

    public ASN1OutputStream(OutputStream os) {
        super(os);
    }

    @Override
    public void writeObject(Object obj) throws IOException {
        if (obj == null) {

            writeNull();
        } else if (obj instanceof DERObject) {

            ((DERObject) obj).encode(this);
        } else if (obj instanceof DEREncodable) {

            ((DEREncodable) obj).getDERObject().encode(this);
        } else {

            throw new IOException("object not ASN1Encodable");
        }
    }

}
