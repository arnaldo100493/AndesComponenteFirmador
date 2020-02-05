/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.andesscd.pki.clases;

import co.com.andesscd.Auxiliar;
import co.com.andesscd.pki.excepciones.SntpException;
import co.com.andesscd.pki.excepciones.TsaException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertStoreException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSAttributeTableGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.DefaultSignedAttributeTableGenerator;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.SimpleAttributeTableGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampToken;

/**
 *
 * @author abarrime
 */
public class CMS {

    public enum FuenteHoraria {
        LOCAL, SNTP, TSA;
    }

    private static String reqPolicy = "1.3.6.1.4.1.601.10.3.1";

    private static boolean licenciaTotal = true;

    private static FuenteHoraria fuenteHoraria = FuenteHoraria.LOCAL;

    private static String urlFuenteHoraria = "";

    private static String loginFuenteHoraria = "";

    private static String passwordFuenteHoraria = "";

    private String nombreDocumento;

    private String descripcion;

    private static boolean full = true;

    private byte[] contenido;

    private boolean decodificado;

    private SignerInformationStore firmantes;

    private CertStore certificados;

    private CMSSignedData signedData;

    private Hashtable<String, byte[]> hashParaTimeStamp;

    private static boolean bcAgregado = false;

    public CMS() {

    }

    public CMS(String archivoEntrada) throws FileNotFoundException, Exception {
        File archivo = new File(archivoEntrada);
        this.nombreDocumento = archivo.getName();
        this.descripcion = "";
        iniciarCMS(new FileInputStream(archivoEntrada));
    }

    public CMS(InputStream streamEntrada) throws Exception {
        this.nombreDocumento = "";
        this.descripcion = "";
        iniciarCMS(streamEntrada);
    }

    public CMS(URL url) throws IOException, Exception {
        HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
        this.nombreDocumento = "";
        this.descripcion = "";
        iniciarCMS(conexion.getInputStream());
    }

    public static FuenteHoraria getFuenteHoraria() {
        return fuenteHoraria;
    }

    public static void setFuenteHorariaLocal() {
        urlFuenteHoraria = "";
        loginFuenteHoraria = "";
        passwordFuenteHoraria = "";
        fuenteHoraria = FuenteHoraria.LOCAL;
    }

    public static void setFuenteHorariaSNTP(String urlFuenteHoraria) {
        CMS.urlFuenteHoraria = urlFuenteHoraria;
        loginFuenteHoraria = "";
        passwordFuenteHoraria = "";
        fuenteHoraria = FuenteHoraria.SNTP;
    }

    public static void setFuenteHorariaTSA(String urlFuenteHoraria, String loginFuenteHoraria, String passwordFuenteHoraria) {
        CMS.urlFuenteHoraria = urlFuenteHoraria;
        CMS.loginFuenteHoraria = loginFuenteHoraria;
        CMS.passwordFuenteHoraria = passwordFuenteHoraria;
        fuenteHoraria = FuenteHoraria.TSA;
    }

    public static GregorianCalendar getFechaActual() throws SntpException {
        ByteArrayOutputStream tokenStream;
        CMS tempCMS;
        byte[] datosAleatorios;
        Random r = new Random();

        GregorianCalendar fechaActual = null;

        switch (fuenteHoraria) {
            case LOCAL:
                fechaActual = new GregorianCalendar(new SimpleTimeZone(0, "America/Bogota"));
                fechaActual.setTime(new Date());
                break;

            case SNTP:
                try {
                    SntpClient sntpClient = new SntpClient();
                    sntpClient.requestTime(urlFuenteHoraria, 10000);
                    fechaActual = sntpClient.getFecha();
                } catch (SocketException ex) {
                    throw new SntpException("Error al intentar obtener la fecha y hora mediante SNTP. Error de comunicacion en el Socket: " + ex.getMessage());
                } catch (UnknownHostException ex) {
                    throw new SntpException("Error al intentar obtener la fecha y hora mediante SNTP. Error al intentar conectar con el host: " + ex.getMessage());
                } catch (IOException ex) {
                    throw new SntpException("Error al intentar obtener la fecha y hora mediante SNTP. Error generico de lectura de datos: " + ex.getMessage());
                }
                break;

            case TSA:
                datosAleatorios = new byte[8];
                r.nextBytes(datosAleatorios);

                try {
                    tempCMS = new CMS(new ByteArrayInputStream(datosAleatorios));
                } catch (Exception ex) {
                    throw new SntpException("Error al intentar obtener la fecha y hora mediante TSA. Error generico de procesamiento: " + ex.getMessage());
                }

                tokenStream = new ByteArrayOutputStream();
                try {
                    tempCMS.getTimestampToken(urlFuenteHoraria, loginFuenteHoraria, passwordFuenteHoraria, tokenStream);
                } catch (IOException ex) {
                    throw new SntpException("Error al intentar obtener la fecha y hora mediante TSA. Error generico de lectura de datos: " + ex.getMessage());
                } catch (NoSuchAlgorithmException ex) {
                    throw new SntpException("Error al intentar obtener la fecha y hora mediante TSA. No se dispone del algoritmo de procesamiento: " + ex.getMessage());
                } catch (TSPException ex) {
                    throw new SntpException("Error al intentar obtener la fecha y hora mediante TSA: " + ex.getMessage());
                } catch (Exception ex) {
                    throw new SntpException("Error al intentar obtener la fecha y hora mediante TSA. Error generico de procesamiento: " + ex.getMessage());
                }

                try {
                    TimeStamped timeStamped = new TimeStamped(tokenStream.toByteArray());
                    fechaActual = timeStamped.getFechaEstampado();
                } catch (CMSException ex) {
                    throw new SntpException("Error al procesar la fecha y hora mediante TSA. Error verificando la respuesta: " + ex.getMessage());
                } catch (TSPException ex) {
                    throw new SntpException("Error al procesar la fecha y hora mediante TSA: " + ex.getMessage());
                } catch (IOException ex) {
                    throw new SntpException("Error al procesar la fecha y hora mediante TSA. Error generico de lectura de datos: " + ex.getMessage());
                } catch (CertificateException ex) {
                    throw new SntpException("Error al procesar la fecha y hora mediante TSA. Error al procesaro el certificado: " + ex.getMessage());
                } catch (Exception ex) {
                    throw new SntpException("Error al procesar la fecha y hora mediante TSA. Error generico de procesamiento: " + ex.getMessage());
                }
                break;
        }

        return fechaActual;
    }

    public static void setFull(String password) throws IOException, NoSuchAlgorithmException, TSPException, Exception {
        full = true;
    }

    public void setNombreDocumento(String nombreDocumento) {
        this.nombreDocumento = nombreDocumento;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public SignerInformationStore getFirmantes() {
        return this.firmantes;
    }

    public CertStore getCertificados() {
        return this.certificados;
    }

    private void iniciarCMS(InputStream streamEntrada) throws Exception {
        if (!full) {
            throw new Exception("Operacion criptografica no permitida, debe desbloquear el uso del componente");
        }
        if (!bcAgregado) {
            Security.addProvider((Provider) new BouncyCastleProvider());
        }
        if (streamEntrada == null) {
            throw new Exception("No se proporcionaron datos de entrada");
        }
        this.contenido = Auxiliar.inputStream2ByteArray(streamEntrada);
        this.hashParaTimeStamp = (Hashtable) new Hashtable<>();
    }

    private static byte[] GetTSAResponse(String url, String usuario, String contrase, byte[] requestBytes) throws MalformedURLException, IOException, TSPException, Exception {
        if (!full) {
            throw new Exception("Operacion criptografica no permitida, debe desbloquear el uso del componente");
        }
        URL uri = new URL(url);

        HttpURLConnection peticionHttp = (HttpURLConnection) uri.openConnection();
        peticionHttp.setDoOutput(true);
        peticionHttp.setDoInput(true);
        peticionHttp.setRequestMethod("POST");
        peticionHttp.setRequestProperty("Content-type", "application/timestamp-query");
        peticionHttp.setRequestProperty("Content-length", String.valueOf(requestBytes.length));

        if (usuario != null && !usuario.isEmpty()) {

            String userpassword = usuario + ":" + contrase;
            String encodedAuthorization = new String(Base64Coder.encode(userpassword.getBytes()));
            peticionHttp.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
        }

        OutputStream requestStream = peticionHttp.getOutputStream();
        requestStream.write(requestBytes);
        requestStream.flush();

        if (peticionHttp.getResponseCode() != 200) {
            throw new IOException("Received HTTP error: " + peticionHttp.getResponseCode() + " - " + peticionHttp.getResponseMessage());
        }
        return Auxiliar.inputStream2ByteArray(peticionHttp.getInputStream());
    }

    public static byte[] getTimestampToken(byte[] hash, String tsaURL, String tsaUserName, String tsaPassword) throws IOException, TSPException, Exception {
        if (!full) {
            throw new Exception("Operacion criptografica no permitida, debe desbloquear el uso del componente");
        }
        TimeStampRequestGenerator generadorDePeticion = new TimeStampRequestGenerator();
        generadorDePeticion.setCertReq(true);

        generadorDePeticion.setReqPolicy(reqPolicy);

        BigInteger nonce = BigInteger.valueOf(Calendar.getInstance().getTimeInMillis());
        TimeStampRequest peticionTsa = generadorDePeticion.generate(X509ObjectIdentifiers.id_SHA1.getId(), hash, nonce);
        byte[] bytesPeticion = peticionTsa.getEncoded();

        byte[] bytesRespuesta = GetTSAResponse(tsaURL, tsaUserName, tsaPassword, bytesPeticion);

        TimeStampResponse respuestaTsa = new TimeStampResponse(bytesRespuesta);
        respuestaTsa.validate(peticionTsa);
        PKIFailureInfo fallaTsa = respuestaTsa.getFailInfo();

        if (fallaTsa != null) {
            throw new Exception("No se puede conectar a la URL");
        }
        TimeStampToken tokenTsa = respuestaTsa.getTimeStampToken();
        if (tokenTsa == null) {
            throw new Exception("No se obtuvo respuesta esperada");
        }
        return tokenTsa.getEncoded();
    }

    public void getTimestampToken(String url, String usuario, String contrase, String archivoSalida) throws FileNotFoundException, IOException, NoSuchAlgorithmException, TSPException, Exception {
        getTimestampToken(url, usuario, contrase, new FileOutputStream(archivoSalida));
    }

    public void getTimestampToken(String url, String usuario, String contrase, OutputStream streamSalida) throws IOException, NoSuchAlgorithmException, TSPException, Exception {
        try {
            int leidos;
            byte[] buffer = new byte[1048576];

            MessageDigest sha1 = MessageDigest.getInstance("SHA");
            ByteArrayInputStream in = new ByteArrayInputStream(this.contenido);

            do {
                leidos = in.read(buffer);
                if (leidos <= 0) {
                    continue;
                }
                sha1.update(buffer, 0, leidos);
            } while (leidos > 0);

            byte[] hash = sha1.digest();

            byte[] tokenBytes = getTimestampToken(hash, url, usuario, contrase);
            streamSalida.write(tokenBytes, 0, tokenBytes.length);
        } finally {

            if (streamSalida != null) {
                streamSalida.close();
            }
        }
    }

    public void firmar(X509Certificate certificado, PrivateKey llavePrivada, Provider provedor, OutputStream streamSalida) throws NoSuchAlgorithmException, IOException, CMSException, CertStoreException, InvalidAlgorithmParameterException, CertificateEncodingException, OperatorCreationException, TSPException, SntpException, CertificateException, Exception {
        ArrayList<X509Certificate> listaDeCertificados = new ArrayList();

        try {
            CMSSignedDataGenerator generadorDeFirma = new CMSSignedDataGenerator();

            listaDeCertificados.add(certificado);
            JcaCertStore jcaCertStore = new JcaCertStore(listaDeCertificados);
            generadorDeFirma.addCertificates((Store) jcaCertStore);

            JcaSignerInfoGeneratorBuilder jcaSignerInfoGeneratorBuilder = new JcaSignerInfoGeneratorBuilder((new JcaDigestCalculatorProviderBuilder()).setProvider("BC").build());

            ASN1EncodableVector signedAttributes = new ASN1EncodableVector();
            Date fechaActual = null;
            if (fuenteHoraria == FuenteHoraria.LOCAL || fuenteHoraria == FuenteHoraria.SNTP) {
                fechaActual = getFechaActual().getTime();
            } else {
                MessageDigest sha = MessageDigest.getInstance("SHA1");
                byte[] timeStampToken = getTimestampToken(sha.digest(this.contenido), urlFuenteHoraria, loginFuenteHoraria, passwordFuenteHoraria);
                fechaActual = (new TimeStamped(timeStampToken)).getFechaEstampado().getTime();
                ASN1EncodableVector unSignedAttributes = new ASN1EncodableVector();
                unSignedAttributes.add((DEREncodable) new Attribute(new DERObjectIdentifier("1.2.840.113549.1.9.16.2.14"), (ASN1Set) new DERSet((DEREncodable) (new ASN1InputStream(timeStampToken)).readObject())));
                AttributeTable unSignedAttributesTable = new AttributeTable(unSignedAttributes);
                SimpleAttributeTableGenerator unSignedAttributeGenerator = new SimpleAttributeTableGenerator(unSignedAttributesTable);
                jcaSignerInfoGeneratorBuilder.setUnsignedAttributeGenerator((CMSAttributeTableGenerator) unSignedAttributeGenerator);
            }

            signedAttributes.add((DEREncodable) new Attribute((DERObjectIdentifier) CMSAttributes.signingTime, (ASN1Set) new DERSet((DEREncodable) new DERUTCTime(fechaActual))));
            AttributeTable signedAttributesTable = new AttributeTable(signedAttributes);
            DefaultSignedAttributeTableGenerator signedAttributeGenerator = new DefaultSignedAttributeTableGenerator(signedAttributesTable);
            jcaSignerInfoGeneratorBuilder.setSignedAttributeGenerator((CMSAttributeTableGenerator) signedAttributeGenerator);
            ContentSigner firmante = (new JcaContentSignerBuilder("SHA1withRSA")).setProvider(provedor).build(llavePrivada);
            SignerInfoGenerator signerInfoGenerator = jcaSignerInfoGeneratorBuilder.build(firmante, certificado);
            generadorDeFirma.addSignerInfoGenerator(signerInfoGenerator);

            ArrayList<SignerInformation> firmantes = new ArrayList();

            CMSProcessableByteArray cMSProcessableByteArray = new CMSProcessableByteArray(this.contenido);

            CMSSignedData datosFirmados = generadorDeFirma.generate((CMSTypedData) cMSProcessableByteArray, true);

            Collection<SignerInformation> firmantesLocal = datosFirmados.getSignerInfos().getSigners();

            for (SignerInformation si : firmantesLocal) {
                firmantes.add(si);
            }

            SignerInformationStore newSignerInformationStore = new SignerInformationStore(firmantes);
            CMSSignedData newSd = CMSSignedData.replaceSigners(datosFirmados, newSignerInformationStore);
            streamSalida.write(newSd.getEncoded());
        } finally {

            if (streamSalida != null) {
                streamSalida.close();
            }
        }
    }

    public void firmar(KeyStore keyStore, String alias, String contrase, Provider provedor, OutputStream streamSalida) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, Exception {
        if (!keyStore.containsAlias(alias)) {
            throw new Exception("El almacen no contiene el alias: " + alias);
        }
        if (!keyStore.isKeyEntry(alias)) {
            throw new Exception("El almacen no contiene una llave con alias: " + alias);
        }
        X509Certificate certificado = (X509Certificate) keyStore.getCertificate(alias);
        if (certificado == null) {
            throw new Exception("El almacen no contiene un certificado con alias: " + alias);
        }
        PrivateKey llavePrivada = (PrivateKey) keyStore.getKey(alias, (contrase != null) ? contrase.toCharArray() : null);
        if (llavePrivada == null) {
            throw new Exception("No se pudo recuperar la llave con alias: " + alias);
        }
        firmar(certificado, llavePrivada, provedor, streamSalida);
    }

    public void firmar(KeyStore keyStore, String alias, String contrase, OutputStream streamSalida) throws NoSuchAlgorithmException, IOException, CMSException, CertStoreException, InvalidAlgorithmParameterException, CertificateEncodingException, OperatorCreationException, KeyStoreException, Exception, TsaException, SntpException {
        firmar(keyStore, alias, contrase, keyStore.getProvider(), streamSalida);
    }

    public void firmar(KeyStore keyStore, String contrase, OutputStream streamSalida) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, Exception, TsaException, SntpException {
        String alias = null;

        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            alias = aliases.nextElement();
            if (keyStore.isKeyEntry(alias)) {
                break;
            }
        }

        if (alias == null || alias.isEmpty()) {
            throw new Exception("No se enocntro una llave util para firma dentro del contenedor");
        }
        firmar(keyStore, alias, contrase, streamSalida);
    }

    public static String firmar(String datos, String alias, String password) throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException, Exception, TsaException, SntpException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        KeyStore keystore = KeyStore.getInstance("Windows-MY", "SunMSCAPI");
        keystore.load(null, null);
        if (!keystore.containsAlias(alias)) {
            throw new Exception("El almacen de windows no contiene el alias " + alias);
        }
        Certificate certificado = keystore.getCertificate(alias);
        Key privateKey = keystore.getKey(alias, password.toCharArray());

        CMS cms = new CMS(new ByteArrayInputStream(datos.getBytes("UTF-8")));
        cms.firmar((X509Certificate) certificado, (PrivateKey) privateKey, keystore.getProvider(), out);
        return new String(Base64Coder.encode(out.toByteArray()));
    }

    public static HashMap listarWindowsStore() throws NoSuchProviderException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        HashMap<Object, Object> certificados = new HashMap<>();
        KeyStore keystore = KeyStore.getInstance("Windows-MY", "SunMSCAPI");
        keystore.load(null, null);
        Enumeration<String> aliases = keystore.aliases();

        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (!keystore.isKeyEntry(alias)) {
                continue;
            }
            X509Certificate cert = (X509Certificate) keystore.getCertificate(alias);
            certificados.put(alias, cert);
        }

        return certificados;
    }

}
