/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bouncycastle.asn1;

import java.io.IOException;

/**
 *
 * @author abarrime
 */
public interface ASN1ApplicationSpecificParser extends DEREncodable, InMemoryRepresentable {

    public DEREncodable readObject() throws IOException;

}
