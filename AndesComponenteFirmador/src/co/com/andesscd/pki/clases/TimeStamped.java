/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.andesscd.pki.clases;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.SimpleTimeZone;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.Selector;
import org.bouncycastle.util.Store;

/**
 *
 * @author abarrime
 */
public class TimeStamped {

    private GregorianCalendar fechaEstampado;
    private byte[] hash;
    private String hashAlgoritmoOid;
    private X509Certificate firmante;
    private byte[] encoded;

    public TimeStamped(byte[] encodedTimeStamped) throws CMSException, TSPException, IOException, CertificateException, Exception {
        TimeStampToken timeStampToken = new TimeStampToken(new CMSSignedData(encodedTimeStamped));
        CMSSignedData cmsSignedData = new CMSSignedData(encodedTimeStamped);
        SimpleTimeZone simpleTimeZone = new SimpleTimeZone(0, "America/Bogota");

        this.encoded = encodedTimeStamped;
        this.fechaEstampado = new GregorianCalendar(simpleTimeZone);
        this.fechaEstampado.setTime(timeStampToken.getTimeStampInfo().getGenTime());
        this.hash = timeStampToken.getTimeStampInfo().getMessageImprintDigest();
        this.hashAlgoritmoOid = timeStampToken.getTimeStampInfo().getMessageImprintAlgOID();

        Store certificateStore = cmsSignedData.getCertificates();
        Iterator<SignerInformation> signerIterator = cmsSignedData.getSignerInfos().getSigners().iterator();
        if (signerIterator.hasNext()) {

            SignerInformation signerInformation = signerIterator.next();
            Iterator<X509CertificateHolder> certificateIterator = certificateStore.getMatches((Selector) signerInformation.getSID()).iterator();
            X509CertificateHolder certificateHolder = certificateIterator.next();
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            this.firmante = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certificateHolder.getEncoded()));
        }
    }

    public GregorianCalendar getFechaEstampado() {
        return this.fechaEstampado;
    }

    public byte[] getHash() {
        return this.hash;
    }

    public String getHashAlgorithmOid() {
        return this.hashAlgoritmoOid;
    }

    public X509Certificate getCertificadoFirmante() {
        return this.firmante;
    }

    public byte[] getEncoded() {
        return this.encoded;
    }

    public boolean verificarDatos(InputStream stream) throws NoSuchAlgorithmException, IOException, Exception {
        int leidos;
        byte[] buffer = new byte[1048576];

        MessageDigest sha1 = MessageDigest.getInstance("SHA");
        do {
            leidos = stream.read(buffer);
            if (leidos <= 0) {
                continue;
            }
            sha1.update(buffer, 0, leidos);
        } while (leidos > 0);

        byte[] hashCalculado = sha1.digest();
        if (this.hash == null) {
            throw new Exception("El hash en el token es nulo");
        }
        if (this.hash.length != hashCalculado.length) {
            return false;
        }
        for (int i = 0; i < this.hash.length; i++) {
            if (this.hash[i] != hashCalculado[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean verificarDatos(String rutaArchivo) throws NoSuchAlgorithmException, IOException, Exception {
        return verificarDatos(new FileInputStream(rutaArchivo));
    }

}
