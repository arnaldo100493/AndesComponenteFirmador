/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.andesscd.pki.clases;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Extensions;

/**
 *
 * @author abarrime
 */
public class CRLClient {

    public CRLClient() {

    }

    public ResultadoVerificacion consultarCRL(X509Certificate cert) {
        try {
            List<String> crlDistPoints = getCrlDistributionPoints(cert);
            if (crlDistPoints.isEmpty()) {
                return ResultadoVerificacion.ESTADO_DE_REVOCACION_DESCONOCIDO;
            }
            for (String crlDP : crlDistPoints) {
                X509CRL crl = downloadCRL(crlDP);
                if (crl.isRevoked(cert)) {
                    return ResultadoVerificacion.CERTIFICADO_REVOCADO;
                }
            }
            return ResultadoVerificacion.VALIDO;
        } catch (Exception ex) {
            return ResultadoVerificacion.ESTADO_DE_REVOCACION_DESCONOCIDO;
        }
    }

    public ResultadoVerificacion consultarCRL(KeyStore keyStore, String alias) throws KeyStoreException {
        return consultarCRL((X509Certificate) keyStore.getCertificate(alias));
    }

    private static X509CRL downloadCRL(String crlURL) throws IOException, CertificateException, CRLException, CertificateVerificationException, NamingException {
        if (crlURL.startsWith("http://") || crlURL.startsWith("https://") || crlURL.startsWith("ftp://")) {

            X509CRL crl = downloadCRLFromWeb(crlURL);
            return crl;
        }
        if (crlURL.startsWith("ldap://")) {
            X509CRL crl = downloadCRLFromLDAP(crlURL);
            return crl;
        }
        throw new CertificateVerificationException("Can not download CRL from certificate distribution point: " + crlURL);
    }

    private static X509CRL downloadCRLFromLDAP(String ldapURL) throws CertificateException, NamingException, CRLException, CertificateVerificationException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
        env.put("java.naming.provider.url", ldapURL);

        DirContext ctx = new InitialDirContext(env);
        Attributes avals = ctx.getAttributes("");
        Attribute aval = avals.get("certificateRevocationList;binary");
        byte[] val = (byte[]) aval.get();
        if (val == null || val.length == 0) {
            throw new CertificateVerificationException("Can not download CRL from: " + ldapURL);
        }

        InputStream inStream = new ByteArrayInputStream(val);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509CRL crl = (X509CRL) cf.generateCRL(inStream);
        return crl;
    }

    private static X509CRL downloadCRLFromWeb(String crlURL) throws MalformedURLException, IOException, CertificateException, CRLException {
        URL url = new URL(crlURL);
        InputStream crlStream = url.openStream();
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509CRL crl = (X509CRL) cf.generateCRL(crlStream);
            return crl;
        } finally {
            crlStream.close();
        }
    }

    private static List<String> getCrlDistributionPoints(X509Certificate cert) throws CertificateParsingException, IOException {
        byte[] crldpExt = cert.getExtensionValue(X509Extensions.CRLDistributionPoints.getId());

        if (crldpExt == null) {

            List<String> emptyList = new ArrayList<>();
            return emptyList;
        }
        ASN1InputStream oAsnInStream = new ASN1InputStream(new ByteArrayInputStream(crldpExt));

        DERObject derObjCrlDP = oAsnInStream.readObject();
        DEROctetString dosCrlDP = (DEROctetString) derObjCrlDP;
        byte[] crldpExtOctets = dosCrlDP.getOctets();
        ASN1InputStream oAsnInStream2 = new ASN1InputStream(new ByteArrayInputStream(crldpExtOctets));

        DERObject derObj2 = oAsnInStream2.readObject();
        CRLDistPoint distPoint = CRLDistPoint.getInstance(derObj2);
        List<String> crlUrls = new ArrayList<>();
        for (DistributionPoint dp : distPoint.getDistributionPoints()) {
            DistributionPointName dpn = dp.getDistributionPoint();

            if (dpn != null
                    && dpn.getType() == 0) {
                GeneralName[] genNames = GeneralNames.getInstance(dpn.getName()).getNames();

                for (int j = 0; j < genNames.length; j++) {
                    if (genNames[j].getTagNo() == 6) {
                        String url = DERIA5String.getInstance(genNames[j].getName()).getString();

                        crlUrls.add(url);
                    }
                }
            }
        }

        return crlUrls;
    }

}
