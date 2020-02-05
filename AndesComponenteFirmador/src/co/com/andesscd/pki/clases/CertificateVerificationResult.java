/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.andesscd.pki.clases;

import java.security.cert.PKIXCertPathBuilderResult;

/**
 *
 * @author abarrime
 */
public class CertificateVerificationResult {

    private boolean valid;
    private PKIXCertPathBuilderResult result;
    private Throwable exception;

    public CertificateVerificationResult() {
        this.valid = false;
        this.result = null;
        this.exception = null;
    }

    public CertificateVerificationResult(PKIXCertPathBuilderResult result) {
        this.valid = true;
        this.result = result;
    }

    public CertificateVerificationResult(Throwable exception) {
        this.valid = false;
        this.exception = exception;
    }

    public boolean isValid() {
        return this.valid;
    }

    public PKIXCertPathBuilderResult getResult() {
        return this.result;
    }

    public Throwable getException() {
        return this.exception;
    }

}
