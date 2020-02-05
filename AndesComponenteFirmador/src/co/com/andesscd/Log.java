/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.andesscd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 *
 * @author abarrime
 */
public class Log {

    private PrintWriter log;

    public Log() {

    }

    public Log(String ruta) throws FileNotFoundException {
        File file = new File(ruta);

        if (file.exists()) {
            iniciarLog(new FileOutputStream(ruta));
        } else {
            iniciarLog(new FileOutputStream(ruta));
        }
    }

    public Log(OutputStream stream) {
        iniciarLog(stream);
    }

    private void iniciarLog(OutputStream stream) {
        this.log = new PrintWriter(stream);
    }

    public void escribir(String datos) {
        System.out.println(Auxiliar.getFechaHoraActualLocal() + ": " + datos);
        this.log.println(Auxiliar.getFechaHoraActualLocal() + ": " + datos);
        this.log.flush();
    }
}
