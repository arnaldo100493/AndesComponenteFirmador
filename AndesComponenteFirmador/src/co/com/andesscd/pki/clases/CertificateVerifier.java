/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.andesscd.pki.clases;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertStore;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author abarrime
 */
public class CertificateVerifier {

    public CertificateVerifier() {

    }

    public static PKIXCertPathBuilderResult verifyCertificate(X509Certificate cert, Set<X509Certificate> additionalCerts) throws CertificateVerificationException {
        try {
            if (isSelfSigned(cert)) {
                throw new CertificateVerificationException("The certificate is self-signed.");
            }

            Set<X509Certificate> trustedRootCerts = new HashSet<>();
            Set<X509Certificate> intermediateCerts = new HashSet<>();
            for (X509Certificate additionalCert : additionalCerts) {
                if (isSelfSigned(additionalCert)) {
                    trustedRootCerts.add(additionalCert);
                    continue;
                }
                intermediateCerts.add(additionalCert);
            }

            PKIXCertPathBuilderResult verifiedCertChain = verifyCertificate(cert, trustedRootCerts, intermediateCerts);

            CRLClient clienteCRL = new CRLClient();
            clienteCRL.consultarCRL(cert);

            return verifiedCertChain;
        } catch (CertPathBuilderException certPathEx) {
            throw new CertificateVerificationException("Error building certification path: " + cert.getSubjectX500Principal(), certPathEx);

        } catch (CertificateVerificationException cvex) {
            throw cvex;
        } catch (Exception ex) {
            throw new CertificateVerificationException("Error verifying the certificate: " + cert.getSubjectX500Principal(), ex);
        }
    }

    public static boolean isSelfSigned(X509Certificate cert) throws CertificateException, NoSuchAlgorithmException, NoSuchProviderException {
        try {
            PublicKey key = cert.getPublicKey();
            cert.verify(key);
            return true;
        } catch (SignatureException sigEx) {

            return false;
        } catch (InvalidKeyException keyEx) {

            return false;
        }
    }

    private static PKIXCertPathBuilderResult verifyCertificate(X509Certificate cert, Set<X509Certificate> trustedRootCerts, Set<X509Certificate> intermediateCerts) throws GeneralSecurityException {
        X509CertSelector selector = new X509CertSelector();
        selector.setCertificate(cert);

        Set<TrustAnchor> trustAnchors = new HashSet<>();
        for (X509Certificate trustedRootCert : trustedRootCerts) {
            trustAnchors.add(new TrustAnchor(trustedRootCert, null));
        }

        PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(trustAnchors, selector);

        pkixParams.setRevocationEnabled(false);

        CertStore intermediateCertStore = CertStore.getInstance("Collection", new CollectionCertStoreParameters(intermediateCerts), "BC");

        pkixParams.addCertStore(intermediateCertStore);

        CertPathBuilder builder = CertPathBuilder.getInstance("PKIX", "BC");
        PKIXCertPathBuilderResult result = (PKIXCertPathBuilderResult) builder.build(pkixParams);

        return result;
    }

}
