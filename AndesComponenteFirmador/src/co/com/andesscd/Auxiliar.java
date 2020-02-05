/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.andesscd;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

/**
 *
 * @author abarrime
 */
public class Auxiliar {

    public Auxiliar() {

    }

    public static void descargarRecurso(Object clase, String recurso, String ruta) throws IOException {
        if (ruta == null || ruta.isEmpty()) {
            ruta = System.getProperty("user.home") + System.getProperty("file.separator");
        }
        ruta = ruta + recurso;
        System.out.println("descarga en: " + ruta);

        File archivo = new File(ruta);
        if (!archivo.exists()) {
            JOptionPane.showMessageDialog(null, "El sistema descargara el archivo " + recurso + ", por favor haga click en aceptar " + "y espere hasta que el mensaje de confirmacion " + "le notifique la finalizacion de la descarga", "Alerta", 1);

            int bytesLeidos = 0;
            int bytesOffset = 0;

            byte[] bytes = new byte[2097152];
            InputStream input = clase.getClass().getResourceAsStream(recurso);

            do {
                bytesLeidos = input.read(bytes, bytesOffset, 100000);
                if (bytesLeidos <= 0) {
                    continue;
                }
                bytesOffset += bytesLeidos;
            } while (bytesLeidos != -1);
            input.close();
            FileOutputStream output = new FileOutputStream(ruta);
            output.write(bytes, 0, bytesOffset);
            output.close();
            JOptionPane.showMessageDialog(null, "El sistema ha descargado el archivo " + recurso + ", por favor haga click en aceptar " + "para continuar", "Alerta", 1);
        }
    }

    public static void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(null, mensaje, "Error", 0);
    }

    public static void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(null, mensaje, "Advertencia", 2);
    }

    public static void mostrarInformacion(String mensaje) {
        JOptionPane.showMessageDialog(null, mensaje, "Informacion", 1);
    }

    public static boolean esEntero(String cadena) {
        try {
            Integer.parseInt(cadena);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static boolean esDecimal(String cadena) {
        try {
            Double.parseDouble(cadena);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static boolean esNombre(String cadena) {
        for (int i = 0; i < cadena.length();) {
            char digito = cadena.charAt(i);

            if (Character.isLetterOrDigit(digito) || digito == '\'' || digito == ' ') {
                i++;
                continue;
            }
            return false;
        }
        return true;
    }

    public static boolean esEmail(String cadena) {
        String expression = "^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = cadena;
        Pattern pattern = Pattern.compile(expression, 2);
        Matcher matcher = pattern.matcher(inputStr);
        return matcher.matches();
    }

    public static boolean esIP(String cadena) {
        String expression = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

        CharSequence inputStr = cadena;
        Pattern pattern = Pattern.compile(expression, 2);
        Matcher matcher = pattern.matcher(inputStr);
        return matcher.matches();
    }

    public static byte[] HexToByte(String cadena) {
        HexBinaryAdapter adapter = new HexBinaryAdapter();
        byte[] bytes = adapter.unmarshal(cadena);
        return bytes;
    }

    public static String ByteToHex(byte[] b) throws Exception {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result = result + Integer.toString((b[i] & 0xFF) + 256, 16).substring(1);
        }
        return result;
    }

    public static boolean esHexadecimal(String cadena) {
        char[] hexDigitArray = cadena.toCharArray();
        int hexDigitLength = hexDigitArray.length;

        for (int i = 0; i < hexDigitLength; i++) {
            boolean isNotHex = (Character.digit(hexDigitArray[i], 16) == -1);
            if (isNotHex) {
                return false;
            }
        }
        return true;
    }

    public static byte[] inputStream2ByteArray(InputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        byte[] data = new byte[16384];
        int nRead;
        while ((nRead = stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }

    public static String getFechaHoraActualLocal() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }
}
