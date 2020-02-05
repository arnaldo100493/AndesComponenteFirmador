/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.andesscd.pki.clases;

import co.com.andesscd.Auxiliar;
import co.com.andesscd.Base64Coder;
import java.applet.Applet;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileInputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author abarrime
 */
public class AndesSCDApplet extends Applet {

    public String path;
    public String name;

    public AndesSCDApplet() {

    }

    @Override
    public void init() {
        System.out.println("El applet ha iniciado 4");
        try {
            CMS.setFull(null);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    protected static String quote(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        char c = Character.MIN_VALUE;

        int len = string.length();
        StringBuilder sb = new StringBuilder(len + 4);

        sb.append('"');
        for (int i = 0; i < len; i++) {
            c = string.charAt(i);
            switch (c) {
                case '"':
                case '\\':
                    sb.append('\\');
                    sb.append(c);
                    break;

                case '/':
                    sb.append('\\');

                    sb.append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    if (c < ' ') {
                        String t = "000" + Integer.toHexString(c);
                        sb.append("\\u" + t.substring(t.length() - 4));
                        break;
                    }
                    sb.append(c);
                    break;
            }
        }
        sb.append('"');
        return sb.toString();
    }

    public String listarWindowsStore(int serialRadix) {
        String json = "[";

        try {
            HashMap certificados = (HashMap) AccessController.doPrivileged(new PrivilegedAction() {
                @Override
                public Object run() {
                    try {
                        return CMS.listarWindowsStore();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        JOptionPane.showMessageDialog(null, "No fue posible listar los certificados del almacen de windows. " + e.getMessage(), "Error", 0);
                        return null;
                    }
                }
            });

            if (certificados == null) {

                return json;
            }
            Iterator<Map.Entry> it = certificados.entrySet().iterator();
            while (it.hasNext()) {
                String o = "{";
                Map.Entry e = it.next();
                X509Certificate cert = (X509Certificate) e.getValue();
                String subject = cert.getSubjectX500Principal().toString();
                o = o + "\"alias\":" + quote((String) e.getKey()) + ", \"serial\":" + quote(cert.getSerialNumber().toString(serialRadix)) + ", \"dn\":" + quote(subject) + "";
                o = o + "}";
                if (it.hasNext()) {
                    o = o + ", ";
                }
                json = json + o;
            }
            return json;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return json;
        } finally {
            Exception exception = null;
            json = json + "]";
        }

    }

    public void setFuenteHorariaLocal() {
        CMS.setFuenteHorariaLocal();
    }

    public void setFuenteHorariaSNTP(String urlFuenteHoraria) {
        CMS.setFuenteHorariaSNTP(urlFuenteHoraria);
    }

    public void setFuenteHorariaTSA(String urlFuenteHoraria, String loginFuenteHoraria, String passwordFuenteHoraria) {
        CMS.setFuenteHorariaTSA(urlFuenteHoraria, loginFuenteHoraria, passwordFuenteHoraria);
    }

    public String firmar(String datos, String alias, String password, int intentos) {
        class Accion
                implements PrivilegedAction {

            String datos;
            String alias;
            String password;
            String tsaUrl;
            String tsaUser;
            String tsaPassword;
            String firma;
            int intentos;

            public Accion(String datos, String alias, String password, int intentos) {
                this.datos = datos;
                this.alias = alias;
                this.password = password;
                this.tsaUrl = this.tsaUrl;
                this.tsaUser = this.tsaUser;
                this.tsaPassword = this.tsaPassword;
                this.intentos = intentos;
            }

            @Override
            public Object run() {
                String error;
                while (true) {
                    try {
                        this.intentos--;
                        this.firma = CMS.firmar(this.datos, this.alias, this.password);
                        return this.firma;
                    } catch (Exception e) {
                        error = e.getMessage();
                        System.out.println(e.getMessage());
                        System.out.println("Intentando firmar nuevamente");

                        if (this.intentos <= 0) {
                            break;
                        }
                    }
                }
                JOptionPane.showMessageDialog(null, error, "Error", 0);
                return null;
            }
        };

        Accion accion = new Accion(datos, alias, password, intentos);
        AccessController.doPrivileged(accion);

        return accion.firma;
    }

    public String file2Base64() {
        class Accion
                implements PrivilegedAction {

            String path = "";
            String name = "";
            String base64 = "";

            public Object run() {
                try {
                    JFileChooser fc = new JFileChooser();
                    int returnVal = fc.showOpenDialog(null);
                    if (returnVal == 0) {
                        File file = fc.getSelectedFile();
                        if (file.isFile()) {
                            this.base64 = new String(Base64Coder.encode(Auxiliar.inputStream2ByteArray(new FileInputStream(file))));
                            this.path = file.getAbsolutePath();
                            this.name = file.getName();
                        }
                    }
                    return this.base64;
                } catch (HeadlessException | java.io.IOException e) {
                    System.out.println(e.getMessage());
                    JOptionPane.showMessageDialog(null, "No fue posible convertir el archovo a base 64: " + e.getMessage(), "Error", 0);
                    return null;
                }
            }
        };

        Accion accion = new Accion();
        AccessController.doPrivileged(accion);

        this.path = accion.path;
        this.name = accion.name;
        return accion.base64;
    }

}
