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
public abstract class DERObject extends ASN1Encodable implements DERTags {

    public DERObject() {

    }

    public DERObject toASN1Object() {
        return this;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object paramObject);

    abstract void encode(DEROutputStream paramDEROutputStream) throws IOException;

}
