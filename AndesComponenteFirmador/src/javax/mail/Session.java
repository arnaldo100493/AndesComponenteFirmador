/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail;

import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.MailLogger;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;

/**
 *
 * @author abarrime
 */
public final class Session {

    private final Properties props;
    private final Authenticator authenticator;
    private final Hashtable authTable = new Hashtable<>();
    private boolean debug = false;
    private PrintStream out;
    private MailLogger logger;
    private final Vector providers = new Vector();
    private final Hashtable providersByProtocol = new Hashtable<>();
    private final Hashtable providersByClassName = new Hashtable<>();
    private final Properties addressMap = new Properties();

    private static Session defaultSession = null;

    private Session() {
        this.props = null;
        this.authenticator = null;
    }

    private Session(Properties props, Authenticator authenticator) {
        this.props = props;
        this.authenticator = authenticator;

        if (Boolean.valueOf(props.getProperty("mail.debug")).booleanValue()) {
            this.debug = true;
        }
        initLogger();
        this.logger.log(Level.CONFIG, "JavaMail version {0}", "${mail.version}");

        if (authenticator != null) {
            cl = authenticator.getClass();
        } else {
            cl = getClass();
        }
        loadProviders(cl);
        loadAddressMap(cl);
    }

    private final void initLogger() {
        this.logger = new MailLogger(getClass(), "DEBUG", this.debug, getDebugOut());
    }

    public static Session getInstance(Properties props, Authenticator authenticator) {
        return new Session(props, authenticator);
    }

    public static Session getInstance(Properties props) {
        return new Session(props, null);
    }

    public static synchronized Session getDefaultInstance(Properties props, Authenticator authenticator) {
        if (defaultSession == null) {
            defaultSession = new Session(props, authenticator);

        } else if (defaultSession.authenticator != authenticator) {

            if (defaultSession.authenticator == null || authenticator == null || defaultSession.authenticator.getClass().getClassLoader() != authenticator.getClass().getClassLoader()) {

                throw new SecurityException("Access to default session denied");
            }
        }
        return defaultSession;
    }

    public static Session getDefaultInstance(Properties props) {
        return getDefaultInstance(props, null);
    }

    public synchronized void setDebug(boolean debug) {
        this.debug = debug;
        initLogger();
        this.logger.log(Level.CONFIG, "setDebug: JavaMail version {0}", "${mail.version}");
    }

    public synchronized boolean getDebug() {
        return this.debug;
    }

    public synchronized void setDebugOut(PrintStream out) {
        this.out = out;
        initLogger();
    }

    public synchronized PrintStream getDebugOut() {
        if (this.out == null) {
            return System.out;
        }
        return this.out;
    }

    public synchronized Provider[] getProviders() {
        Provider[] _providers = new Provider[this.providers.size()];
        this.providers.copyInto((Object[]) _providers);
        return _providers;
    }

    public synchronized Provider getProvider(String protocol) throws NoSuchProviderException {
        if (protocol == null || protocol.length() <= 0) {
            throw new NoSuchProviderException("Invalid protocol: null");
        }

        Provider _provider = null;

        String _className = this.props.getProperty("mail." + protocol + ".class");
        if (_className != null) {
            if (this.logger.isLoggable(Level.FINE)) {
                this.logger.fine("mail." + protocol + ".class property exists and points to " + _className);
            }

            _provider = (Provider) this.providersByClassName.get(_className);
        }

        if (_provider != null) {
            return _provider;
        }

        _provider = (Provider) this.providersByProtocol.get(protocol);

        if (_provider == null) {
            throw new NoSuchProviderException("No provider for " + protocol);
        }
        if (this.logger.isLoggable(Level.FINE)) {
            this.logger.fine("getProvider() returning " + _provider.toString());
        }
        return _provider;
    }

    public synchronized void setProvider(Provider provider) throws NoSuchProviderException {
        if (provider == null) {
            throw new NoSuchProviderException("Can't set null provider");
        }
        this.providersByProtocol.put(provider.getProtocol(), provider);
        this.props.put("mail." + provider.getProtocol() + ".class", provider.getClassName());
    }

    public Store getStore() throws NoSuchProviderException {
        return getStore(getProperty("mail.store.protocol"));
    }

    public Store getStore(String protocol) throws NoSuchProviderException {
        return getStore(new URLName(protocol, null, -1, null, null, null));
    }

    public Store getStore(URLName url) throws NoSuchProviderException {
        String protocol = url.getProtocol();
        Provider p = getProvider(protocol);
        return getStore(p, url);
    }

    public Store getStore(Provider provider) throws NoSuchProviderException {
        return getStore(provider, null);
    }

    private Store getStore(Provider provider, URLName url) throws NoSuchProviderException {
        if (provider == null || provider.getType() != Provider.Type.STORE) {
            throw new NoSuchProviderException("invalid provider");
        }

        try {
            return (Store) getService(provider, url);
        } catch (ClassCastException cce) {
            throw new NoSuchProviderException("incorrect class");
        }
    }

    public Folder getFolder(URLName url) throws MessagingException {
        Store store = getStore(url);
        store.connect();
        return store.getFolder(url);
    }

    public Transport getTransport() throws NoSuchProviderException {
        return getTransport(getProperty("mail.transport.protocol"));
    }

    public Transport getTransport(String protocol) throws NoSuchProviderException {
        return getTransport(new URLName(protocol, null, -1, null, null, null));
    }

    public Transport getTransport(URLName url) throws NoSuchProviderException {
        String protocol = url.getProtocol();
        Provider p = getProvider(protocol);
        return getTransport(p, url);
    }

    public Transport getTransport(Provider provider) throws NoSuchProviderException {
        return getTransport(provider, null);
    }

    public Transport getTransport(Address address) throws NoSuchProviderException {
        String transportProtocol = getProperty("mail.transport.protocol." + address.getType());

        if (transportProtocol != null) {
            return getTransport(transportProtocol);
        }
        transportProtocol = (String) this.addressMap.get(address.getType());
        if (transportProtocol != null) {
            return getTransport(transportProtocol);
        }
        throw new NoSuchProviderException("No provider for Address type: " + address.getType());
    }

    private Transport getTransport(Provider provider, URLName url) throws NoSuchProviderException {
        if (provider == null || provider.getType() != Provider.Type.TRANSPORT) {
            throw new NoSuchProviderException("invalid provider");
        }

        try {
            return (Transport) getService(provider, url);
        } catch (ClassCastException cce) {
            throw new NoSuchProviderException("incorrect class");
        }
    }

    private Object getService(Provider provider, URLName url) throws NoSuchProviderException {
        ClassLoader cl;
        if (provider == null) {
            throw new NoSuchProviderException("null");
        }

        if (url == null) {
            url = new URLName(provider.getProtocol(), null, -1, null, null, null);
        }

        Object service = null;

        if (this.authenticator != null) {
            cl = this.authenticator.getClass().getClassLoader();
        } else {
            cl = getClass().getClassLoader();
        }

        Class<?> serviceClass = null;

        try {
            ClassLoader ccl = getContextClassLoader();
            if (ccl != null) {
                try {
                    serviceClass = Class.forName(provider.getClassName(), false, ccl);
                } catch (ClassNotFoundException ex) {
                }
            }

            if (serviceClass == null) {
                serviceClass = Class.forName(provider.getClassName(), false, cl);
            }
        } catch (Exception ex1) {

            try {

                serviceClass = Class.forName(provider.getClassName());
            } catch (Exception ex) {

                this.logger.log(Level.FINE, "Exception loading provider", ex);
                throw new NoSuchProviderException(provider.getProtocol());
            }
        }

        try {
            Class[] c = {Session.class, URLName.class};
            Constructor<?> cons = serviceClass.getConstructor(c);

            Object[] o = {this, url};
            service = cons.newInstance(o);
        } catch (Exception ex) {
            this.logger.log(Level.FINE, "Exception loading provider", ex);
            throw new NoSuchProviderException(provider.getProtocol());
        }

        return service;
    }

    public void setPasswordAuthentication(URLName url, PasswordAuthentication pw) {
        if (pw == null) {
            this.authTable.remove(url);
        } else {
            this.authTable.put(url, pw);
        }
    }

    public PasswordAuthentication getPasswordAuthentication(URLName url) {
        return (PasswordAuthentication) this.authTable.get(url);
    }

    public PasswordAuthentication requestPasswordAuthentication(InetAddress addr, int port, String protocol, String prompt, String defaultUserName) {
        if (this.authenticator != null) {
            return this.authenticator.requestPasswordAuthentication(addr, port, protocol, prompt, defaultUserName);
        }

        return null;
    }

    public Properties getProperties() {
        return this.props;
    }

    public String getProperty(String name) {
        return this.props.getProperty(name);
    }

    private void loadProviders(Class cl) {
        StreamLoader loader = new StreamLoader() {
            public void load(InputStream is) throws IOException {
                Session.this.loadProvidersFromStream(is);
            }
        };

        try {
            String res = System.getProperty("java.home") + File.separator + "lib" + File.separator + "javamail.providers";

            loadFile(res, loader);
        } catch (SecurityException sex) {
            this.logger.log(Level.CONFIG, "can't get java.home", sex);
        }

        loadAllResources("META-INF/javamail.providers", cl, loader);

        loadResource("/META-INF/javamail.default.providers", cl, loader);

        if (this.providers.size() == 0) {
            this.logger.config("failed to load any providers, using defaults");

            addProvider(new Provider(Provider.Type.STORE, "imap", "com.sun.mail.imap.IMAPStore", "Sun Microsystems, Inc.", "${mail.version}"));

            addProvider(new Provider(Provider.Type.STORE, "imaps", "com.sun.mail.imap.IMAPSSLStore", "Sun Microsystems, Inc.", "${mail.version}"));

            addProvider(new Provider(Provider.Type.STORE, "pop3", "com.sun.mail.pop3.POP3Store", "Sun Microsystems, Inc.", "${mail.version}"));

            addProvider(new Provider(Provider.Type.STORE, "pop3s", "com.sun.mail.pop3.POP3SSLStore", "Sun Microsystems, Inc.", "${mail.version}"));

            addProvider(new Provider(Provider.Type.TRANSPORT, "smtp", "com.sun.mail.smtp.SMTPTransport", "Sun Microsystems, Inc.", "${mail.version}"));

            addProvider(new Provider(Provider.Type.TRANSPORT, "smtps", "com.sun.mail.smtp.SMTPSSLTransport", "Sun Microsystems, Inc.", "${mail.version}"));
        }

        if (this.logger.isLoggable(Level.CONFIG)) {

            this.logger.config("Tables of loaded providers");
            this.logger.config("Providers Listed By Class Name: " + this.providersByClassName.toString());

            this.logger.config("Providers Listed By Protocol: " + this.providersByProtocol.toString());
        }
    }

    private void loadProvidersFromStream(InputStream is) throws IOException {
        if (is != null) {
            LineInputStream lis = new LineInputStream(is);

            String currLine;

            while ((currLine = lis.readLine()) != null) {

                if (currLine.startsWith("#")) {
                    continue;
                }
                Provider.Type type = null;
                String protocol = null, className = null;
                String vendor = null, version = null;

                StringTokenizer tuples = new StringTokenizer(currLine, ";");
                while (tuples.hasMoreTokens()) {
                    String currTuple = tuples.nextToken().trim();

                    int sep = currTuple.indexOf("=");
                    if (currTuple.startsWith("protocol=")) {
                        protocol = currTuple.substring(sep + 1);
                        continue;
                    }
                    if (currTuple.startsWith("type=")) {
                        String strType = currTuple.substring(sep + 1);
                        if (strType.equalsIgnoreCase("store")) {
                            type = Provider.Type.STORE;
                            continue;
                        }
                        if (strType.equalsIgnoreCase("transport")) {
                            type = Provider.Type.TRANSPORT;
                        }
                        continue;
                    }
                    if (currTuple.startsWith("class=")) {
                        className = currTuple.substring(sep + 1);
                        continue;
                    }
                    if (currTuple.startsWith("vendor=")) {
                        vendor = currTuple.substring(sep + 1);
                        continue;
                    }
                    if (currTuple.startsWith("version=")) {
                        version = currTuple.substring(sep + 1);
                    }
                }

                if (type == null || protocol == null || className == null || protocol.length() <= 0 || className.length() <= 0) {

                    this.logger.log(Level.CONFIG, "Bad provider entry: {0}", currLine);

                    continue;
                }
                Provider provider = new Provider(type, protocol, className, vendor, version);

                addProvider(provider);
            }
        }
    }

    public synchronized void addProvider(Provider provider) {
        this.providers.addElement(provider);
        this.providersByClassName.put(provider.getClassName(), provider);
        if (!this.providersByProtocol.containsKey(provider.getProtocol())) {
            this.providersByProtocol.put(provider.getProtocol(), provider);
        }
    }

    private void loadAddressMap(Class cl) {
        StreamLoader loader = new StreamLoader() {
            public void load(InputStream is) throws IOException {
                Session.this.addressMap.load(is);
            }
        };

        loadResource("/META-INF/javamail.default.address.map", cl, loader);

        loadAllResources("META-INF/javamail.address.map", cl, loader);

        try {
            String res = System.getProperty("java.home") + File.separator + "lib" + File.separator + "javamail.address.map";

            loadFile(res, loader);
        } catch (SecurityException sex) {
            this.logger.log(Level.CONFIG, "can't get java.home", sex);
        }

        if (this.addressMap.isEmpty()) {
            this.logger.config("failed to load address map, using defaults");
            this.addressMap.put("rfc822", "smtp");
        }
    }

    public synchronized void setProtocolForAddress(String addresstype, String protocol) {
        if (protocol == null) {
            this.addressMap.remove(addresstype);
        } else {
            this.addressMap.put(addresstype, protocol);
        }
    }

    private void loadFile(String name, StreamLoader loader) {
        InputStream clis = null;

        try {
            clis = new BufferedInputStream(new FileInputStream(name));
            loader.load(clis);
            this.logger.log(Level.CONFIG, "successfully loaded file: {0}", name);
        } catch (FileNotFoundException fex) {
            try {

                if (clis != null) {
                    clis.close();
                }
            } catch (IOException ex) {
            }
        } catch (IOException e) {
            if (this.logger.isLoggable(Level.CONFIG)) {
                this.logger.log(Level.CONFIG, "not loading file: " + name, e);
            }
        } catch (SecurityException sex) {
            if (this.logger.isLoggable(Level.CONFIG)) {
                this.logger.log(Level.CONFIG, "not loading file: " + name, sex);
            }
        } finally {
            try {
                if (clis != null) {
                    clis.close();
                }
            } catch (IOException ex) {
            }
        }

    }

    private void loadResource(String name, Class cl, StreamLoader loader) {
        InputStream clis = null;
        try {
            clis = getResourceAsStream(cl, name);
            if (clis != null) {
                loader.load(clis);
                this.logger.log(Level.CONFIG, "successfully loaded resource: {0}", name);

            }

        } catch (IOException e) {
            this.logger.log(Level.CONFIG, "Exception loading resource", e);
        } catch (SecurityException sex) {
            this.logger.log(Level.CONFIG, "Exception loading resource", sex);
        } finally {
            try {
                if (clis != null) {
                    clis.close();
                }
            } catch (IOException ex) {
            }
        }
    }

    private void loadAllResources(String name, Class cl, StreamLoader loader) {
        boolean anyLoaded = false;
        try {
            URL[] urls;
            ClassLoader cld = null;

            cld = getContextClassLoader();
            if (cld == null) {
                cld = cl.getClassLoader();
            }
            if (cld != null) {
                urls = getResources(cld, name);
            } else {
                urls = getSystemResources(name);
            }
            if (urls != null) {
                for (int i = 0; i < urls.length; i++) {
                    URL url = urls[i];
                    InputStream clis = null;
                    this.logger.log(Level.CONFIG, "URL {0}", url);

                }

            }

        } catch (Exception ex) {
            this.logger.log(Level.CONFIG, "Exception loading resource", ex);
        }

        if (!anyLoaded) {

            loadResource("/" + name, cl, loader);
        }
    }

    private static ClassLoader getContextClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public Object run() {
                ClassLoader cl = null;
                try {
                    cl = Thread.currentThread().getContextClassLoader();
                } catch (SecurityException ex) {
                }
                return cl;
            }
        });
    }

    private static InputStream getResourceAsStream(final Class c, final String name) throws IOException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
                public Object run() throws IOException {
                    return c.getResourceAsStream(name);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (IOException) e.getException();
        }
    }

    private static URL[] getResources(final ClassLoader cl, final String name) {
        return AccessController.doPrivileged(new PrivilegedAction<URL>() {
            @Override
            public Object run() {
                URL[] ret = null;

                try {
                    Vector<URL> v = new Vector();
                    Enumeration<URL> e = cl.getResources(name);
                    while (e != null && e.hasMoreElements()) {
                        URL url = e.nextElement();
                        if (url != null) {
                            v.addElement(url);
                        }
                    }
                    if (v.size() > 0) {
                        ret = new URL[v.size()];
                        v.copyInto((Object[]) ret);
                    }
                } catch (IOException ioex) {
                } catch (SecurityException ex) {
                }
                return ret;
            }
        });
    }

    private static URL[] getSystemResources(final String name) {
        return AccessController.doPrivileged(new PrivilegedAction<URL>() {
            @Override
            public Object run() {
                URL[] ret = null;

                try {
                    Vector<URL> v = new Vector();
                    Enumeration<URL> e = ClassLoader.getSystemResources(name);
                    while (e != null && e.hasMoreElements()) {
                        URL url = e.nextElement();
                        if (url != null) {
                            v.addElement(url);
                        }
                    }
                    if (v.size() > 0) {
                        ret = new URL[v.size()];
                        v.copyInto((Object[]) ret);
                    }
                } catch (IOException ioex) {
                } catch (SecurityException ex) {
                }
                return ret;
            }
        });
    }

    private static InputStream openStream(final URL url) throws IOException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
                @Override
                public Object run() throws IOException {
                    return url.openStream();
                }
            });
        } catch (PrivilegedActionException e) {
            throw (IOException) e.getException();
        }
    }

}
