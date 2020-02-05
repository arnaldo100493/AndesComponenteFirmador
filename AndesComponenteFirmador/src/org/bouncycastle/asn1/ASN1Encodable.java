/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bouncycastle.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author abarrime
 */
public abstract class ASN1Encodable implements DEREncodable {

    public static final String DER = "DER";
    public static final String BER = "BER";

    public ASN1Encodable() {

    }

    public byte[] getEncoded() throws IOException {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        ASN1OutputStream aOut = new ASN1OutputStream(bOut);

        aOut.writeObject(this);

        return bOut.toByteArray();

    }

}
