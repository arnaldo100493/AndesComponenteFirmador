/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.andesscd.pki.excepciones;

/**
 *
 * @author abarrime
 */
public class SntpException extends Exception {

    public SntpException() {
        super();
    }

    public SntpException(String mensaje) {
        super(mensaje);
    }

}
