/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.andesscd.test;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.util.Enumeration;
import java.util.Properties;
import sun.security.pkcs11.SunPKCS11;

/**
 *
 * @author abarrime
 */
public class Test {

    public static final String KEY_STORE_TYPE = "sample.applet.keyStoreType";
    public static final String JCE_PROVIDER_TYPE = "sample.applet.keyProviderType";
    public static final String KEY_STORE_FILE_PATH = "sample.applet.keyStoreFilePath";
    public static final String KEY_STORE_PASSWORD_NEEDED = "sample.applet.keyStorePasswordNeeded";
    public static final String KEY_ACCESS_NEEDED = "sample.applet.keyAcessNeeded";
    public static final String USE_SAME_PASSWORD_FOR_KEYS = "sample.applet.useSamePasswordForKeys";

    public Test() {

    }

    public static void main(String[] args) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("name = NSScrypto");
            sb.append(System.getProperty("line.separator"));
            sb.append("nssDbMode = readOnly");
            sb.append(System.getProperty("line.separator"));

            sb.append("nssLibraryDirectory = tmp");
            sb.append(System.getProperty("line.separator"));
            sb.append("attributes = compatibility");
            sb.append(System.getProperty("line.separator"));
            sb.append("nssSecmodDirectory = /Profiles/h3wjljd8.default");

            byte[] byteArray = sb.toString().getBytes(System.getProperty("file.encoding"));
            ByteArrayInputStream baos = new ByteArrayInputStream(byteArray);
            Provider p = new SunPKCS11(baos);
            Security.addProvider(p);

            KeyStore keyStore = KeyStore.getInstance("PKCS11");
            keyStore.load(null, null);
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                System.out.println(alias);

            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    protected static KeyStore keyStore = null;
    protected static String keyStoreType = null;
    protected static String providerType = null;
    protected static String keyStorePassword = null;
    protected static String filePathToKeyStore = null;

    protected static boolean keyStorePasswordNeeded = true;

    protected static boolean keyAccessNeeded = true;
    protected static boolean useSamePasswordForKeys = false;
    protected static final String PKCS11_NAME = "sample.applet.pkcs11Name";
    protected static String name = null;

    protected static void parseCommomprops(Properties props) {
        keyStorePasswordNeeded = true;
        keyAccessNeeded = true;
        useSamePasswordForKeys = false;
        if ("no".equalsIgnoreCase(props.getProperty("sample.applet.keyStorePasswordNeeded")) || "false".equalsIgnoreCase(props.getProperty("sample.applet.keyStorePasswordNeeded"))) {
            keyStorePasswordNeeded = false;
        }
        if ("no".equalsIgnoreCase(props.getProperty("sample.applet.keyAcessNeeded")) || "false".equalsIgnoreCase(props.getProperty("sample.applet.keyAcessNeeded"))) {
            keyAccessNeeded = false;
        }
        if ("yes".equalsIgnoreCase(props.getProperty("sample.applet.useSamePasswordForKeys")) || "true".equalsIgnoreCase(props.getProperty("sample.applet.useSamePasswordForKeys"))) {
            useSamePasswordForKeys = true;
        }
    }

    public static void setProperties(Properties props) throws Exception {
        name = props.getProperty("sample.applet.pkcs11Name");
        if (name == null) {
            throw new Exception("sample.applet.pkcs11Name" + " have to be provided.");
        }

        parseCommomprops(props);

        keyStoreType = "PKCS11";
        providerType = "SunPKCS11-" + name;

        if (Security.getProvider(providerType) == null) {

            StringBuffer sb = new StringBuffer();

            sb.append("name = NSScrypto");
            sb.append(System.getProperty("line.separator"));
            sb.append("nssDbMode = noDb");
            sb.append(System.getProperty("line.separator"));

            sb.append("nssLibraryDirectory = C:\\tmp\\Mozilla");
            sb.append(System.getProperty("line.separator"));
            sb.append("attributes = compatibility");

            byte[] byteArray = sb.toString().getBytes(System.getProperty("file.encoding"));
            ByteArrayInputStream baos = new ByteArrayInputStream(byteArray);
            Provider p = new SunPKCS11(baos);
            Security.addProvider(p);
        }
    }

    public static void initialize(String password) throws Exception {
        keyStorePassword = password;
        keyStore = KeyStore.getInstance(keyStoreType);
        if (password != null) {
            keyStore.load(null, password.toCharArray());
        } else {
            keyStore.load(null, null);
        }
    }

}
