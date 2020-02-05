/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.mail.imap.protocol;

import com.sun.mail.auth.Ntlm;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.ByteArray;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.Literal;
import com.sun.mail.iap.LiteralException;
import com.sun.mail.iap.ParsingException;
import com.sun.mail.iap.Protocol;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.Rights;
import com.sun.mail.imap.SortTerm;
import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.BASE64EncoderStream;
import com.sun.mail.util.MailLogger;
import com.sun.mail.util.PropUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import javax.mail.Flags;
import javax.mail.Quota;
import javax.mail.internet.MimeUtility;
import javax.mail.search.SearchException;
import javax.mail.search.SearchTerm;

/**
 *
 * @author abarrime
 */
public class IMAPProtocol extends Protocol {

    private boolean connected = false;
    private boolean rev1 = false;
    private boolean noauthdebug = true;
    private boolean authenticated;
    private Map capabilities;
    private List authmechs;
    protected SearchSequence searchSequence;
    protected String[] searchCharsets;
    private String name;
    private SaslAuthenticator saslAuthenticator;
    private ByteArray ba;
    private static final byte[] CRLF = new byte[]{13, 10};

    private static final FetchItem[] fetchItems = new FetchItem[0];

    private volatile String idleTag;

    public IMAPProtocol(String name, String host, int port, Properties props, boolean isSSL, MailLogger logger) throws IOException, ProtocolException {
        super(host, port, props, "mail." + name, isSSL, logger);

        try {
            this.name = name;
            this.noauthdebug = !PropUtil.getBooleanProperty(props, "mail.debug.auth", false);

            if (this.capabilities == null) {
                capability();
            }
            if (hasCapability("IMAP4rev1")) {
                this.rev1 = true;
            }
            this.searchCharsets = new String[2];
            this.searchCharsets[0] = "UTF-8";
            this.searchCharsets[1] = MimeUtility.mimeCharset(MimeUtility.getDefaultJavaCharset());

            this.connected = true;

        } finally {

            if (!this.connected) {
                disconnect();
            }
        }
    }

    public FetchItem[] getFetchItems() {
        return fetchItems;
    }

    public void capability() throws ProtocolException {
        Response[] r = command("CAPABILITY", null);

        if (!r[r.length - 1].isOK()) {
            throw new ProtocolException(r[r.length - 1].toString());
        }
        this.capabilities = new HashMap<>(10);
        this.authmechs = new ArrayList(5);
        for (int i = 0, len = r.length; i < len; i++) {
            if (r[i] instanceof IMAPResponse) {

                IMAPResponse ir = (IMAPResponse) r[i];

                if (ir.keyEquals("CAPABILITY")) {
                    parseCapabilities(ir);
                }
            }
        }
    }

    protected void setCapabilities(Response r) {
        byte b;
        while ((b = r.readByte()) > 0 && b != 91);

        if (b == 0) {
            return;
        }
        String s = r.readAtom();
        if (!s.equalsIgnoreCase("CAPABILITY")) {
            return;
        }
        this.capabilities = new HashMap<>(10);
        this.authmechs = new ArrayList(5);
        parseCapabilities(r);
    }

    protected void parseCapabilities(Response r) {
        String s;
        while ((s = r.readAtom(']')) != null) {
            if (s.length() == 0) {
                if (r.peekByte() == 93) {
                    break;
                }

                r.skipToken();
                continue;
            }
            this.capabilities.put(s.toUpperCase(Locale.ENGLISH), s);
            if (s.regionMatches(true, 0, "AUTH=", 0, 5)) {
                this.authmechs.add(s.substring(5));
                if (this.logger.isLoggable(Level.FINE)) {
                    this.logger.fine("AUTH: " + s.substring(5));
                }
            }
        }
    }

    protected void processGreeting(Response r) throws ProtocolException {
        super.processGreeting(r);
        if (r.isOK()) {
            setCapabilities(r);

            return;
        }
        IMAPResponse ir = (IMAPResponse) r;
        if (ir.keyEquals("PREAUTH")) {
            this.authenticated = true;
            setCapabilities(r);
        } else {
            throw new ConnectionException(this, r);
        }
    }

    public boolean isAuthenticated() {
        return this.authenticated;
    }

    public boolean isREV1() {
        return this.rev1;
    }

    protected boolean supportsNonSyncLiterals() {
        return hasCapability("LITERAL+");
    }

    public Response readResponse() throws IOException, ProtocolException {
        IMAPResponse r = new IMAPResponse(this);
        if (r.keyEquals("FETCH")) {
            r = new FetchResponse(r, getFetchItems());
        }
        return r;
    }

    public boolean hasCapability(String c) {
        if (c.endsWith("*")) {
            c = c.substring(0, c.length() - 1).toUpperCase(Locale.ENGLISH);
            Iterator<String> it = this.capabilities.keySet().iterator();
            while (it.hasNext()) {
                if (((String) it.next()).startsWith(c)) {
                    return true;
                }
            }
            return false;
        }
        return this.capabilities.containsKey(c.toUpperCase(Locale.ENGLISH));
    }

    public Map getCapabilities() {
        return this.capabilities;
    }

    public void disconnect() {
        super.disconnect();
        this.authenticated = false;
    }

    public void noop() throws ProtocolException {
        this.logger.fine("IMAPProtocol noop");
        simpleCommand("NOOP", null);
    }

    public void logout() throws ProtocolException {
        try {
            Response[] r = command("LOGOUT", null);

            this.authenticated = false;

            notifyResponseHandlers(r);
        } finally {
            disconnect();
        }
    }

    public void login(String u, String p) throws ProtocolException {
        Argument args = new Argument();
        args.writeString(u);
        args.writeString(p);

        Response[] r = null;
        try {
            if (this.noauthdebug && isTracing()) {
                this.logger.fine("LOGIN command trace suppressed");
                suspendTracing();
            }
            r = command("LOGIN", args);
        } finally {
            resumeTracing();
        }

        notifyResponseHandlers(r);

        if (this.noauthdebug && isTracing()) {
            this.logger.fine("LOGIN command result: " + r[r.length - 1]);
        }
        handleResult(r[r.length - 1]);

        setCapabilities(r[r.length - 1]);

        this.authenticated = true;
    }

    public synchronized void authlogin(String u, String p) throws ProtocolException {
        Vector<Response> v = new Vector();
        String tag = null;
        Response r = null;
        boolean done = false;

        try {
            if (this.noauthdebug && isTracing()) {
                this.logger.fine("AUTHENTICATE LOGIN command trace suppressed");
                suspendTracing();
            }

            try {
                tag = writeCommand("AUTHENTICATE LOGIN", null);
            } catch (Exception ex) {

                r = Response.byeResponse(ex);
                done = true;
            }

            OutputStream os = getOutputStream();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            BASE64EncoderStream bASE64EncoderStream = new BASE64EncoderStream(bos, 2147483647);
            boolean first = true;

            while (!done) {
                try {
                    r = readResponse();
                    if (r.isContinuation()) {
                        String s;

                        if (first) {
                            s = u;
                            first = false;
                        } else {
                            s = p;
                        }

                        bASE64EncoderStream.write(ASCIIUtility.getBytes(s));
                        bASE64EncoderStream.flush();

                        bos.write(CRLF);
                        os.write(bos.toByteArray());
                        os.flush();
                        bos.reset();
                        continue;
                    }
                    if (r.isTagged() && r.getTag().equals(tag)) {

                        done = true;
                        continue;
                    }
                    if (r.isBYE()) {
                        done = true;
                        continue;
                    }
                    v.addElement(r);
                } catch (Exception ioex) {

                    r = Response.byeResponse(ioex);
                    done = true;
                }
            }
        } finally {

            resumeTracing();
        }

        Response[] responses = new Response[v.size()];
        v.copyInto((Object[]) responses);
        notifyResponseHandlers(responses);

        if (this.noauthdebug && isTracing()) {
            this.logger.fine("AUTHENTICATE LOGIN command result: " + r);
        }
        handleResult(r);

        setCapabilities(r);

        this.authenticated = true;
    }

    public synchronized void authplain(String authzid, String u, String p) throws ProtocolException {
        Vector<Response> v = new Vector();
        String tag = null;
        Response r = null;
        boolean done = false;

        try {
            if (this.noauthdebug && isTracing()) {
                this.logger.fine("AUTHENTICATE PLAIN command trace suppressed");
                suspendTracing();
            }

            try {
                tag = writeCommand("AUTHENTICATE PLAIN", null);
            } catch (Exception ex) {

                r = Response.byeResponse(ex);
                done = true;
            }

            OutputStream os = getOutputStream();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            BASE64EncoderStream bASE64EncoderStream = new BASE64EncoderStream(bos, 2147483647);

            while (!done) {
                try {
                    r = readResponse();
                    if (r.isContinuation()) {

                        String nullByte = "\000";
                        String s = ((authzid == null) ? "" : authzid) + "\000" + u + "\000" + p;

                        bASE64EncoderStream.write(ASCIIUtility.getBytes(s));
                        bASE64EncoderStream.flush();

                        bos.write(CRLF);
                        os.write(bos.toByteArray());
                        os.flush();
                        bos.reset();
                        continue;
                    }
                    if (r.isTagged() && r.getTag().equals(tag)) {

                        done = true;
                        continue;
                    }
                    if (r.isBYE()) {
                        done = true;
                        continue;
                    }
                    v.addElement(r);
                } catch (Exception ioex) {

                    r = Response.byeResponse(ioex);
                    done = true;
                }
            }
        } finally {

            resumeTracing();
        }

        Response[] responses = new Response[v.size()];
        v.copyInto((Object[]) responses);
        notifyResponseHandlers(responses);

        if (this.noauthdebug && isTracing()) {
            this.logger.fine("AUTHENTICATE PLAIN command result: " + r);
        }
        handleResult(r);

        setCapabilities(r);

        this.authenticated = true;
    }

    public synchronized void authntlm(String authzid, String u, String p) throws ProtocolException {
        Vector<Response> v = new Vector();
        String tag = null;
        Response r = null;
        boolean done = false;

        String type1Msg = null;
        int flags = PropUtil.getIntProperty(this.props, "mail." + this.name + ".auth.ntlm.flags", 0);

        String domain = this.props.getProperty("mail." + this.name + ".auth.ntlm.domain", "");

        Ntlm ntlm = new Ntlm(domain, getLocalHost(), u, p, this.logger);

        try {
            if (this.noauthdebug && isTracing()) {
                this.logger.fine("AUTHENTICATE NTLM command trace suppressed");
                suspendTracing();
            }

            try {
                tag = writeCommand("AUTHENTICATE NTLM", null);
            } catch (Exception ex) {

                r = Response.byeResponse(ex);
                done = true;
            }

            OutputStream os = getOutputStream();
            boolean first = true;

            while (!done) {
                try {
                    r = readResponse();
                    if (r.isContinuation()) {
                        String s;

                        if (first) {
                            s = ntlm.generateType1Msg(flags);
                            first = false;
                        } else {
                            s = ntlm.generateType3Msg(r.getRest());
                        }

                        os.write(ASCIIUtility.getBytes(s));
                        os.write(CRLF);
                        os.flush();
                        continue;
                    }
                    if (r.isTagged() && r.getTag().equals(tag)) {

                        done = true;
                        continue;
                    }
                    if (r.isBYE()) {
                        done = true;
                        continue;
                    }
                    v.addElement(r);
                } catch (Exception ioex) {

                    r = Response.byeResponse(ioex);
                    done = true;
                }
            }
        } finally {

            resumeTracing();
        }

        Response[] responses = new Response[v.size()];
        v.copyInto((Object[]) responses);
        notifyResponseHandlers(responses);

        if (this.noauthdebug && isTracing()) {
            this.logger.fine("AUTHENTICATE NTLM command result: " + r);
        }
        handleResult(r);

        setCapabilities(r);

        this.authenticated = true;
    }

    public void sasllogin(String[] allowed, String realm, String authzid, String u, String p) throws ProtocolException {
        Object v;
        if (this.saslAuthenticator == null) {
            try {
                v = Class.forName("com.sun.mail.imap.protocol.IMAPSaslAuthenticator");

                Constructor<?> c = v.getConstructor(new Class[]{IMAPProtocol.class, String.class, Properties.class, MailLogger.class, String.class});

                this.saslAuthenticator = (SaslAuthenticator) c.newInstance(new Object[]{this, this.name, this.props, this.logger, this.host});

            } catch (Exception ex) {
                this.logger.log(Level.FINE, "Can't load SASL authenticator", (Throwable) v);

                return;
            }
        }

        if (allowed != null && allowed.length > 0) {

            v = new ArrayList(allowed.length);
            for (int i = 0; i < allowed.length; i++) {
                if (this.authmechs.contains(allowed[i])) {
                    v.add(allowed[i]);
                }
            }
        } else {
            v = this.authmechs;
        }
        String[] mechs = (String[]) v.toArray((Object[]) new String[v.size()]);

        try {
            if (this.noauthdebug && isTracing()) {
                this.logger.fine("SASL authentication command trace suppressed");
                suspendTracing();
            }

            if (this.saslAuthenticator.authenticate(mechs, realm, authzid, u, p)) {
                if (this.noauthdebug && isTracing()) {
                    this.logger.fine("SASL authentication succeeded");
                }
                this.authenticated = true;
            } else if (this.noauthdebug && isTracing()) {
                this.logger.fine("SASL authentication failed");
            }
        } finally {
            resumeTracing();
        }
    }

    OutputStream getIMAPOutputStream() {
        return getOutputStream();
    }

    public void proxyauth(String u) throws ProtocolException {
        Argument args = new Argument();
        args.writeString(u);

        simpleCommand("PROXYAUTH", args);
    }

    public void id(String guid) throws ProtocolException {
        simpleCommand("ID (\"GUID\" \"" + guid + "\")", null);
    }

    public void startTLS() throws ProtocolException {
        try {
            startTLS("STARTTLS");
        } catch (ProtocolException pex) {
            this.logger.log(Level.FINE, "STARTTLS ProtocolException", (Throwable) pex);

            throw pex;
        } catch (Exception ex) {
            this.logger.log(Level.FINE, "STARTTLS Exception", ex);

            Response[] r = {Response.byeResponse(ex)};
            notifyResponseHandlers(r);
            disconnect();
            throw new ProtocolException("STARTTLS failure", ex);
        }
    }

    public MailboxInfo select(String mbox) throws ProtocolException {
        mbox = BASE64MailboxEncoder.encode(mbox);

        Argument args = new Argument();
        args.writeString(mbox);

        Response[] r = command("SELECT", args);

        MailboxInfo minfo = new MailboxInfo(r);

        notifyResponseHandlers(r);

        Response response = r[r.length - 1];

        if (response.isOK()) {
            if (response.toString().indexOf("READ-ONLY") != -1) {
                minfo.mode = 1;
            } else {
                minfo.mode = 2;
            }
        }
        handleResult(response);
        return minfo;
    }

    public MailboxInfo examine(String mbox) throws ProtocolException {
        mbox = BASE64MailboxEncoder.encode(mbox);

        Argument args = new Argument();
        args.writeString(mbox);

        Response[] r = command("EXAMINE", args);

        MailboxInfo minfo = new MailboxInfo(r);
        minfo.mode = 1;

        notifyResponseHandlers(r);

        handleResult(r[r.length - 1]);
        return minfo;
    }

    public void unselect() throws ProtocolException {
        if (!hasCapability("UNSELECT")) {
            throw new BadCommandException("UNSELECT not supported");
        }
        simpleCommand("UNSELECT", null);
    }

    public Status status(String mbox, String[] items) throws ProtocolException {
        if (!isREV1() && !hasCapability("IMAP4SUNVERSION")) {

            throw new BadCommandException("STATUS not supported");
        }

        mbox = BASE64MailboxEncoder.encode(mbox);

        Argument args = new Argument();
        args.writeString(mbox);

        Argument itemArgs = new Argument();
        if (items == null) {
            items = Status.standardItems;
        }
        for (int i = 0, len = items.length; i < len; i++) {
            itemArgs.writeAtom(items[i]);
        }
        args.writeArgument(itemArgs);

        Response[] r = command("STATUS", args);

        Status status = null;
        Response response = r[r.length - 1];

        if (response.isOK()) {
            for (int i = 0, len = r.length; i < len; i++) {
                if (r[i] instanceof IMAPResponse) {

                    IMAPResponse ir = (IMAPResponse) r[i];
                    if (ir.keyEquals("STATUS")) {
                        if (status == null) {
                            status = new Status(ir);
                        } else {
                            Status.add(status, new Status(ir));
                        }
                        r[i] = null;
                    }
                }
            }
        }

        notifyResponseHandlers(r);
        handleResult(response);
        return status;
    }

    public void create(String mbox) throws ProtocolException {
        mbox = BASE64MailboxEncoder.encode(mbox);

        Argument args = new Argument();
        args.writeString(mbox);

        simpleCommand("CREATE", args);
    }

    public void delete(String mbox) throws ProtocolException {
        mbox = BASE64MailboxEncoder.encode(mbox);

        Argument args = new Argument();
        args.writeString(mbox);

        simpleCommand("DELETE", args);
    }

    public void rename(String o, String n) throws ProtocolException {
        o = BASE64MailboxEncoder.encode(o);
        n = BASE64MailboxEncoder.encode(n);

        Argument args = new Argument();
        args.writeString(o);
        args.writeString(n);

        simpleCommand("RENAME", args);
    }

    public void subscribe(String mbox) throws ProtocolException {
        Argument args = new Argument();

        mbox = BASE64MailboxEncoder.encode(mbox);
        args.writeString(mbox);

        simpleCommand("SUBSCRIBE", args);
    }

    public void unsubscribe(String mbox) throws ProtocolException {
        Argument args = new Argument();

        mbox = BASE64MailboxEncoder.encode(mbox);
        args.writeString(mbox);

        simpleCommand("UNSUBSCRIBE", args);
    }

    public ListInfo[] list(String ref, String pattern) throws ProtocolException {
        return doList("LIST", ref, pattern);
    }

    public ListInfo[] lsub(String ref, String pattern) throws ProtocolException {
        return doList("LSUB", ref, pattern);
    }

    protected ListInfo[] doList(String cmd, String ref, String pat) throws ProtocolException {
        ref = BASE64MailboxEncoder.encode(ref);
        pat = BASE64MailboxEncoder.encode(pat);

        Argument args = new Argument();
        args.writeString(ref);
        args.writeString(pat);

        Response[] r = command(cmd, args);

        ListInfo[] linfo = null;
        Response response = r[r.length - 1];

        if (response.isOK()) {
            Vector<ListInfo> v = new Vector(1);
            for (int i = 0, len = r.length; i < len; i++) {
                if (r[i] instanceof IMAPResponse) {

                    IMAPResponse ir = (IMAPResponse) r[i];
                    if (ir.keyEquals(cmd)) {
                        v.addElement(new ListInfo(ir));
                        r[i] = null;
                    }
                }
            }
            if (v.size() > 0) {
                linfo = new ListInfo[v.size()];
                v.copyInto((Object[]) linfo);
            }
        }

        notifyResponseHandlers(r);
        handleResult(response);
        return linfo;
    }

    public void append(String mbox, Flags f, Date d, Literal data) throws ProtocolException {
        appenduid(mbox, f, d, data, false);
    }

    public AppendUID appenduid(String mbox, Flags f, Date d, Literal data) throws ProtocolException {
        return appenduid(mbox, f, d, data, true);
    }

    public AppendUID appenduid(String mbox, Flags f, Date d, Literal data, boolean uid) throws ProtocolException {
        mbox = BASE64MailboxEncoder.encode(mbox);

        Argument args = new Argument();
        args.writeString(mbox);

        if (f != null) {

            if (f.contains(Flags.Flag.RECENT)) {
                f = new Flags(f);
                f.remove(Flags.Flag.RECENT);
            }

            args.writeAtom(createFlagList(f));
        }
        if (d != null) {
            args.writeString(INTERNALDATE.format(d));
        }
        args.writeBytes(data);

        Response[] r = command("APPEND", args);

        notifyResponseHandlers(r);

        handleResult(r[r.length - 1]);

        if (uid) {
            return getAppendUID(r[r.length - 1]);
        }
        return null;
    }

    private AppendUID getAppendUID(Response r) {
        if (!r.isOK()) {
            return null;
        }
        byte b;
        while ((b = r.readByte()) > 0 && b != 91);

        if (b == 0) {
            return null;
        }
        String s = r.readAtom();
        if (!s.equalsIgnoreCase("APPENDUID")) {
            return null;
        }
        long uidvalidity = r.readLong();
        long uid = r.readLong();
        return new AppendUID(uidvalidity, uid);
    }

    public void check() throws ProtocolException {
        simpleCommand("CHECK", null);
    }

    public void close() throws ProtocolException {
        simpleCommand("CLOSE", null);
    }

    public void expunge() throws ProtocolException {
        simpleCommand("EXPUNGE", null);
    }

    public void uidexpunge(UIDSet[] set) throws ProtocolException {
        if (!hasCapability("UIDPLUS")) {
            throw new BadCommandException("UID EXPUNGE not supported");
        }
        simpleCommand("UID EXPUNGE " + UIDSet.toString(set), null);
    }

    public BODYSTRUCTURE fetchBodyStructure(int msgno) throws ProtocolException {
        Response[] r = fetch(msgno, "BODYSTRUCTURE");
        notifyResponseHandlers(r);

        Response response = r[r.length - 1];
        if (response.isOK()) {
            return (BODYSTRUCTURE) FetchResponse.getItem(r, msgno, BODYSTRUCTURE.class);
        }
        if (response.isNO()) {
            return null;
        }
        handleResult(response);
        return null;
    }

    public BODY peekBody(int msgno, String section) throws ProtocolException {
        return fetchBody(msgno, section, true);
    }

    public BODY fetchBody(int msgno, String section) throws ProtocolException {
        return fetchBody(msgno, section, false);
    }

    protected BODY fetchBody(int msgno, String section, boolean peek) throws ProtocolException {
        Response[] r;
        if (peek) {
            r = fetch(msgno, "BODY.PEEK[" + ((section == null) ? "]" : (section + "]")));
        } else {

            r = fetch(msgno, "BODY[" + ((section == null) ? "]" : (section + "]")));
        }

        notifyResponseHandlers(r);

        Response response = r[r.length - 1];
        if (response.isOK()) {
            return (BODY) FetchResponse.getItem(r, msgno, BODY.class);
        }
        if (response.isNO()) {
            return null;
        }
        handleResult(response);
        return null;
    }

    public BODY peekBody(int msgno, String section, int start, int size) throws ProtocolException {
        return fetchBody(msgno, section, start, size, true, null);
    }

    public BODY fetchBody(int msgno, String section, int start, int size) throws ProtocolException {
        return fetchBody(msgno, section, start, size, false, null);
    }

    public BODY peekBody(int msgno, String section, int start, int size, ByteArray ba) throws ProtocolException {
        return fetchBody(msgno, section, start, size, true, ba);
    }

    public BODY fetchBody(int msgno, String section, int start, int size, ByteArray ba) throws ProtocolException {
        return fetchBody(msgno, section, start, size, false, ba);
    }

    protected BODY fetchBody(int msgno, String section, int start, int size, boolean peek, ByteArray ba) throws ProtocolException {
        this.ba = ba;
        Response[] r = fetch(msgno, (peek ? "BODY.PEEK[" : "BODY[") + ((section == null) ? "]<" : (section + "]<")) + String.valueOf(start) + "." + String.valueOf(size) + ">");

        notifyResponseHandlers(r);

        Response response = r[r.length - 1];
        if (response.isOK()) {
            return (BODY) FetchResponse.getItem(r, msgno, BODY.class);
        }
        if (response.isNO()) {
            return null;
        }
        handleResult(response);
        return null;
    }

    protected ByteArray getResponseBuffer() {
        ByteArray ret = this.ba;
        this.ba = null;
        return ret;
    }

    public RFC822DATA fetchRFC822(int msgno, String what) throws ProtocolException {
        Response[] r = fetch(msgno, (what == null) ? "RFC822" : ("RFC822." + what));

        notifyResponseHandlers(r);

        Response response = r[r.length - 1];
        if (response.isOK()) {
            return (RFC822DATA) FetchResponse.getItem(r, msgno, RFC822DATA.class);
        }
        if (response.isNO()) {
            return null;
        }
        handleResult(response);
        return null;
    }

    public Flags fetchFlags(int msgno) throws ProtocolException {
        Flags flags = null;
        Response[] r = fetch(msgno, "FLAGS");

        for (int i = 0, len = r.length; i < len; i++) {
            if (r[i] != null && r[i] instanceof FetchResponse && ((FetchResponse) r[i]).getNumber() == msgno) {

                FetchResponse fr = (FetchResponse) r[i];
                if ((flags = (Flags) fr.getItem(Flags.class)) != null) {
                    r[i] = null;

                    break;
                }
            }
        }
        notifyResponseHandlers(r);
        handleResult(r[r.length - 1]);
        return flags;
    }

    public UID fetchUID(int msgno) throws ProtocolException {
        Response[] r = fetch(msgno, "UID");

        notifyResponseHandlers(r);

        Response response = r[r.length - 1];
        if (response.isOK()) {
            return (UID) FetchResponse.getItem(r, msgno, UID.class);
        }
        if (response.isNO()) {
            return null;
        }
        handleResult(response);
        return null;
    }

    public UID fetchSequenceNumber(long uid) throws ProtocolException {
        UID u = null;
        Response[] r = fetch(String.valueOf(uid), "UID", true);

        for (int i = 0, len = r.length; i < len; i++) {
            if (r[i] != null && r[i] instanceof FetchResponse) {

                FetchResponse fr = (FetchResponse) r[i];
                if ((u = (UID) fr.getItem(UID.class)) != null) {
                    if (u.uid == uid) {
                        break;
                    }
                    u = null;
                }
            }
        }
        notifyResponseHandlers(r);
        handleResult(r[r.length - 1]);
        return u;
    }

    public UID[] fetchSequenceNumbers(long start, long end) throws ProtocolException {
        Response[] r = fetch(String.valueOf(start) + ":" + ((end == -1L) ? "*" : String.valueOf(end)), "UID", true);

        Vector<UID> v = new Vector();
        for (int i = 0, len = r.length; i < len; i++) {
            if (r[i] != null && r[i] instanceof FetchResponse) {

                FetchResponse fr = (FetchResponse) r[i];
                UID u;
                if ((u = (UID) fr.getItem(UID.class)) != null) {
                    v.addElement(u);
                }
            }
        }
        notifyResponseHandlers(r);
        handleResult(r[r.length - 1]);

        UID[] ua = new UID[v.size()];
        v.copyInto((Object[]) ua);
        return ua;
    }

    public UID[] fetchSequenceNumbers(long[] uids) throws ProtocolException {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < uids.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(String.valueOf(uids[i]));
        }

        Response[] r = fetch(sb.toString(), "UID", true);

        Vector<UID> v = new Vector();
        for (int i = 0, len = r.length; i < len; i++) {
            if (r[i] != null && r[i] instanceof FetchResponse) {

                FetchResponse fr = (FetchResponse) r[i];
                UID u;
                if ((u = (UID) fr.getItem(UID.class)) != null) {
                    v.addElement(u);
                }
            }
        }
        notifyResponseHandlers(r);
        handleResult(r[r.length - 1]);

        UID[] ua = new UID[v.size()];
        v.copyInto((Object[]) ua);
        return ua;
    }

    public Response[] fetch(MessageSet[] msgsets, String what) throws ProtocolException {
        return fetch(MessageSet.toString(msgsets), what, false);
    }

    public Response[] fetch(int start, int end, String what) throws ProtocolException {
        return fetch(String.valueOf(start) + ":" + String.valueOf(end), what, false);
    }

    public Response[] fetch(int msg, String what) throws ProtocolException {
        return fetch(String.valueOf(msg), what, false);
    }

    private Response[] fetch(String msgSequence, String what, boolean uid) throws ProtocolException {
        if (uid) {
            return command("UID FETCH " + msgSequence + " (" + what + ")", null);
        }
        return command("FETCH " + msgSequence + " (" + what + ")", null);
    }

    public void copy(MessageSet[] msgsets, String mbox) throws ProtocolException {
        copy(MessageSet.toString(msgsets), mbox);
    }

    public void copy(int start, int end, String mbox) throws ProtocolException {
        copy(String.valueOf(start) + ":" + String.valueOf(end), mbox);
    }

    private void copy(String msgSequence, String mbox) throws ProtocolException {
        mbox = BASE64MailboxEncoder.encode(mbox);

        Argument args = new Argument();
        args.writeAtom(msgSequence);
        args.writeString(mbox);

        simpleCommand("COPY", args);
    }

    public void storeFlags(MessageSet[] msgsets, Flags flags, boolean set) throws ProtocolException {
        storeFlags(MessageSet.toString(msgsets), flags, set);
    }

    public void storeFlags(int start, int end, Flags flags, boolean set) throws ProtocolException {
        storeFlags(String.valueOf(start) + ":" + String.valueOf(end), flags, set);
    }

    public void storeFlags(int msg, Flags flags, boolean set) throws ProtocolException {
        storeFlags(String.valueOf(msg), flags, set);
    }

    private void storeFlags(String msgset, Flags flags, boolean set) throws ProtocolException {
        Response[] r;
        if (set) {
            r = command("STORE " + msgset + " +FLAGS " + createFlagList(flags), null);
        } else {

            r = command("STORE " + msgset + " -FLAGS " + createFlagList(flags), null);
        }

        notifyResponseHandlers(r);
        handleResult(r[r.length - 1]);
    }

    private String createFlagList(Flags flags) {
        StringBuffer sb = new StringBuffer();
        sb.append("(");

        Flags.Flag[] sf = flags.getSystemFlags();
        boolean first = true;
        for (int i = 0; i < sf.length; i++) {
            String s;
            Flags.Flag f = sf[i];
            if (f == Flags.Flag.ANSWERED) {
                s = "\\Answered";
            } else if (f == Flags.Flag.DELETED) {
                s = "\\Deleted";
            } else if (f == Flags.Flag.DRAFT) {
                s = "\\Draft";
            } else if (f == Flags.Flag.FLAGGED) {
                s = "\\Flagged";
            } else if (f == Flags.Flag.RECENT) {
                s = "\\Recent";
            } else if (f == Flags.Flag.SEEN) {
                s = "\\Seen";
            } else {
                continue;
            }
            if (first) {
                first = false;
            } else {
                sb.append(' ');
            }
            sb.append(s);
            continue;
        }
        String[] uf = flags.getUserFlags();
        for (int i = 0; i < uf.length; i++) {
            if (first) {
                first = false;
            } else {
                sb.append(' ');
            }
            sb.append(uf[i]);
        }

        sb.append(")");
        return sb.toString();
    }

    public int[] search(MessageSet[] msgsets, SearchTerm term) throws ProtocolException, SearchException {
        return search(MessageSet.toString(msgsets), term);
    }

    public int[] search(SearchTerm term) throws ProtocolException, SearchException {
        return search("ALL", term);
    }

    private int[] search(String msgSequence, SearchTerm term) throws ProtocolException, SearchException {
        getSearchSequence();
        if (SearchSequence.isAscii(term)) {
            try {
                return issueSearch(msgSequence, term, null);
            } catch (IOException ioex) {
            }
        }

        for (int i = 0; i < this.searchCharsets.length; i++) {
            if (this.searchCharsets[i] != null) {

                try {

                    return issueSearch(msgSequence, term, this.searchCharsets[i]);
                } catch (CommandFailedException cfx) {

                    this.searchCharsets[i] = null;
                } catch (IOException ioex) {

                } catch (ProtocolException pex) {
                    throw pex;
                } catch (SearchException sex) {
                    throw sex;
                }
            }
        }

        throw new SearchException("Search failed");
    }

    private int[] issueSearch(String msgSequence, SearchTerm term, String charset) throws ProtocolException, SearchException, IOException {
        Response[] r;
        Argument args = getSearchSequence().generateSequence(term, (charset == null) ? null : MimeUtility.javaCharset(charset));

        args.writeAtom(msgSequence);

        if (charset == null) {
            r = command("SEARCH", args);
        } else {
            r = command("SEARCH CHARSET " + charset, args);
        }
        Response response = r[r.length - 1];
        int[] matches = null;

        if (response.isOK()) {
            Vector<Integer> v = new Vector();

            for (int i = 0, len = r.length; i < len; i++) {
                if (r[i] instanceof IMAPResponse) {

                    IMAPResponse ir = (IMAPResponse) r[i];

                    if (ir.keyEquals("SEARCH")) {
                        int num;
                        while ((num = ir.readNumber()) != -1) {
                            v.addElement(new Integer(num));
                        }
                        r[i] = null;
                    }
                }
            }

            int vsize = v.size();
            matches = new int[vsize];
            for (int i = 0; i < vsize; i++) {
                matches[i] = ((Integer) v.elementAt(i)).intValue();
            }
        }

        notifyResponseHandlers(r);
        handleResult(response);
        return matches;
    }

    protected SearchSequence getSearchSequence() {
        if (this.searchSequence == null) {
            this.searchSequence = new SearchSequence();
        }
        return this.searchSequence;
    }

    public int[] sort(SortTerm[] term, SearchTerm sterm) throws ProtocolException, SearchException {
        if (!hasCapability("SORT*")) {
            throw new BadCommandException("SORT not supported");
        }
        if (term == null || term.length == 0) {
            throw new BadCommandException("Must have at least one sort term");
        }
        Argument args = new Argument();
        Argument sargs = new Argument();
        for (int i = 0; i < term.length; i++) {
            sargs.writeAtom(term[i].toString());
        }
        args.writeArgument(sargs);

        args.writeAtom("UTF-8");
        if (sterm != null) {
            try {
                args.append(getSearchSequence().generateSequence(sterm, "UTF-8"));
            } catch (IOException ioex) {

                throw new SearchException(ioex.toString());
            }
        } else {
            args.writeAtom("ALL");
        }
        Response[] r = command("SORT", args);
        Response response = r[r.length - 1];
        int[] matches = null;

        if (response.isOK()) {
            Vector<Integer> v = new Vector();

            for (int i = 0, len = r.length; i < len; i++) {
                if (r[i] instanceof IMAPResponse) {

                    IMAPResponse ir = (IMAPResponse) r[i];
                    if (ir.keyEquals("SORT")) {
                        int num;
                        while ((num = ir.readNumber()) != -1) {
                            v.addElement(new Integer(num));
                        }
                        r[i] = null;
                    }
                }
            }

            int vsize = v.size();
            matches = new int[vsize];
            for (int i = 0; i < vsize; i++) {
                matches[i] = ((Integer) v.elementAt(i)).intValue();
            }
        }

        notifyResponseHandlers(r);
        handleResult(response);
        return matches;
    }

    public Namespaces namespace() throws ProtocolException {
        if (!hasCapability("NAMESPACE")) {
            throw new BadCommandException("NAMESPACE not supported");
        }
        Response[] r = command("NAMESPACE", null);

        Namespaces namespace = null;
        Response response = r[r.length - 1];

        if (response.isOK()) {
            for (int i = 0, len = r.length; i < len; i++) {
                if (r[i] instanceof IMAPResponse) {

                    IMAPResponse ir = (IMAPResponse) r[i];
                    if (ir.keyEquals("NAMESPACE")) {
                        if (namespace == null) {
                            namespace = new Namespaces(ir);
                        }
                        r[i] = null;
                    }
                }
            }
        }

        notifyResponseHandlers(r);
        handleResult(response);
        return namespace;
    }

    public Quota[] getQuotaRoot(String mbox) throws ProtocolException {
        if (!hasCapability("QUOTA")) {
            throw new BadCommandException("GETQUOTAROOT not supported");
        }

        mbox = BASE64MailboxEncoder.encode(mbox);

        Argument args = new Argument();
        args.writeString(mbox);

        Response[] r = command("GETQUOTAROOT", args);

        Response response = r[r.length - 1];

        Hashtable<Object, Object> tab = new Hashtable<>();

        if (response.isOK()) {
            for (int i = 0, len = r.length; i < len; i++) {
                if (r[i] instanceof IMAPResponse) {

                    IMAPResponse ir = (IMAPResponse) r[i];
                    if (ir.keyEquals("QUOTAROOT")) {

                        ir.readAtomString();

                        String root = null;
                        while ((root = ir.readAtomString()) != null && root.length() > 0) {
                            tab.put(root, new Quota(root));
                        }
                        r[i] = null;
                    } else if (ir.keyEquals("QUOTA")) {
                        Quota quota = parseQuota(ir);
                        Quota q = (Quota) tab.get(quota.quotaRoot);
                        if (q == null || q.resources != null);

                        tab.put(quota.quotaRoot, quota);
                        r[i] = null;
                    }
                }
            }
        }

        notifyResponseHandlers(r);
        handleResult(response);

        Quota[] qa = new Quota[tab.size()];
        Enumeration<Quota> e = tab.elements();
        for (int i = 0; e.hasMoreElements(); i++) {
            qa[i] = e.nextElement();
        }
        return qa;
    }

    public Quota[] getQuota(String root) throws ProtocolException {
        if (!hasCapability("QUOTA")) {
            throw new BadCommandException("QUOTA not supported");
        }
        Argument args = new Argument();
        args.writeString(root);

        Response[] r = command("GETQUOTA", args);

        Quota quota = null;
        Vector<Quota> v = new Vector();
        Response response = r[r.length - 1];

        if (response.isOK()) {
            for (int i = 0, len = r.length; i < len; i++) {
                if (r[i] instanceof IMAPResponse) {

                    IMAPResponse ir = (IMAPResponse) r[i];
                    if (ir.keyEquals("QUOTA")) {
                        quota = parseQuota(ir);
                        v.addElement(quota);
                        r[i] = null;
                    }
                }
            }
        }

        notifyResponseHandlers(r);
        handleResult(response);
        Quota[] qa = new Quota[v.size()];
        v.copyInto((Object[]) qa);
        return qa;
    }

    public void setQuota(Quota quota) throws ProtocolException {
        if (!hasCapability("QUOTA")) {
            throw new BadCommandException("QUOTA not supported");
        }
        Argument args = new Argument();
        args.writeString(quota.quotaRoot);
        Argument qargs = new Argument();
        if (quota.resources != null) {
            for (int i = 0; i < quota.resources.length; i++) {
                qargs.writeAtom((quota.resources[i]).name);
                qargs.writeNumber((quota.resources[i]).limit);
            }
        }
        args.writeArgument(qargs);

        Response[] r = command("SETQUOTA", args);
        Response response = r[r.length - 1];

        notifyResponseHandlers(r);
        handleResult(response);
    }

    private Quota parseQuota(Response r) throws ParsingException {
        String quotaRoot = r.readAtomString();
        Quota q = new Quota(quotaRoot);
        r.skipSpaces();

        if (r.readByte() != 40) {
            throw new ParsingException("parse error in QUOTA");
        }
        Vector<Quota.Resource> v = new Vector();
        while (r.peekByte() != 41) {

            String name = r.readAtom();
            if (name != null) {
                long usage = r.readLong();
                long limit = r.readLong();
                Quota.Resource res = new Quota.Resource(name, usage, limit);
                v.addElement(res);
            }
        }
        r.readByte();
        q.resources = new Quota.Resource[v.size()];
        v.copyInto((Object[]) q.resources);
        return q;
    }

    public void setACL(String mbox, char modifier, ACL acl) throws ProtocolException {
        if (!hasCapability("ACL")) {
            throw new BadCommandException("ACL not supported");
        }

        mbox = BASE64MailboxEncoder.encode(mbox);

        Argument args = new Argument();
        args.writeString(mbox);
        args.writeString(acl.getName());
        String rights = acl.getRights().toString();
        if (modifier == '+' || modifier == '-') {
            rights = modifier + rights;
        }
        args.writeString(rights);

        Response[] r = command("SETACL", args);
        Response response = r[r.length - 1];

        notifyResponseHandlers(r);
        handleResult(response);
    }

    public void deleteACL(String mbox, String user) throws ProtocolException {
        if (!hasCapability("ACL")) {
            throw new BadCommandException("ACL not supported");
        }

        mbox = BASE64MailboxEncoder.encode(mbox);

        Argument args = new Argument();
        args.writeString(mbox);
        args.writeString(user);

        Response[] r = command("DELETEACL", args);
        Response response = r[r.length - 1];

        notifyResponseHandlers(r);
        handleResult(response);
    }

    public ACL[] getACL(String mbox) throws ProtocolException {
        if (!hasCapability("ACL")) {
            throw new BadCommandException("ACL not supported");
        }

        mbox = BASE64MailboxEncoder.encode(mbox);

        Argument args = new Argument();
        args.writeString(mbox);

        Response[] r = command("GETACL", args);
        Response response = r[r.length - 1];

        Vector<ACL> v = new Vector();
        if (response.isOK()) {
            for (int i = 0, len = r.length; i < len; i++) {
                if (r[i] instanceof IMAPResponse) {

                    IMAPResponse ir = (IMAPResponse) r[i];
                    if (ir.keyEquals("ACL")) {

                        ir.readAtomString();
                        String name = null;
                        while ((name = ir.readAtomString()) != null) {
                            String rights = ir.readAtomString();
                            if (rights == null) {
                                break;
                            }
                            ACL acl = new ACL(name, new Rights(rights));
                            v.addElement(acl);
                        }
                        r[i] = null;
                    }
                }
            }
        }

        notifyResponseHandlers(r);
        handleResult(response);
        ACL[] aa = new ACL[v.size()];
        v.copyInto((Object[]) aa);
        return aa;
    }

    public Rights[] listRights(String mbox, String user) throws ProtocolException {
        if (!hasCapability("ACL")) {
            throw new BadCommandException("ACL not supported");
        }

        mbox = BASE64MailboxEncoder.encode(mbox);

        Argument args = new Argument();
        args.writeString(mbox);
        args.writeString(user);

        Response[] r = command("LISTRIGHTS", args);
        Response response = r[r.length - 1];

        Vector<Rights> v = new Vector();
        if (response.isOK()) {
            for (int i = 0, len = r.length; i < len; i++) {
                if (r[i] instanceof IMAPResponse) {

                    IMAPResponse ir = (IMAPResponse) r[i];
                    if (ir.keyEquals("LISTRIGHTS")) {

                        ir.readAtomString();

                        ir.readAtomString();
                        String rights;
                        while ((rights = ir.readAtomString()) != null) {
                            v.addElement(new Rights(rights));
                        }
                        r[i] = null;
                    }
                }
            }
        }

        notifyResponseHandlers(r);
        handleResult(response);
        Rights[] ra = new Rights[v.size()];
        v.copyInto((Object[]) ra);
        return ra;
    }

    public Rights myRights(String mbox) throws ProtocolException {
        if (!hasCapability("ACL")) {
            throw new BadCommandException("ACL not supported");
        }

        mbox = BASE64MailboxEncoder.encode(mbox);

        Argument args = new Argument();
        args.writeString(mbox);

        Response[] r = command("MYRIGHTS", args);
        Response response = r[r.length - 1];

        Rights rights = null;
        if (response.isOK()) {
            for (int i = 0, len = r.length; i < len; i++) {
                if (r[i] instanceof IMAPResponse) {

                    IMAPResponse ir = (IMAPResponse) r[i];
                    if (ir.keyEquals("MYRIGHTS")) {

                        ir.readAtomString();
                        String rs = ir.readAtomString();
                        if (rights == null) {
                            rights = new Rights(rs);
                        }
                        r[i] = null;
                    }
                }
            }
        }

        notifyResponseHandlers(r);
        handleResult(response);
        return rights;
    }

    public synchronized void idleStart() throws ProtocolException {
        if (!hasCapability("IDLE")) {
            throw new BadCommandException("IDLE not supported");
        }
        Vector<Response> v = new Vector();
        boolean done = false;
        Response r = null;

        try {
            this.idleTag = writeCommand("IDLE", null);
        } catch (LiteralException lex) {
            v.addElement(lex.getResponse());
            done = true;
        } catch (Exception ex) {

            v.addElement(Response.byeResponse(ex));
            done = true;
        }

        while (!done) {
            try {
                r = readResponse();
            } catch (IOException ioex) {

                r = Response.byeResponse(ioex);
            } catch (ProtocolException pex) {
                continue;
            }

            v.addElement(r);

            if (r.isContinuation() || r.isBYE()) {
                done = true;
            }
        }
        Response[] responses = new Response[v.size()];
        v.copyInto((Object[]) responses);
        r = responses[responses.length - 1];

        notifyResponseHandlers(responses);
        if (!r.isContinuation()) {
            handleResult(r);
        }
    }

    public synchronized Response readIdleResponse() {
        if (this.idleTag == null) {
            return null;
        }
        Response r = null;
        while (r == null) {
            try {
                r = readResponse();
            } catch (InterruptedIOException iioex) {

                if (iioex.bytesTransferred == 0) {
                    r = null;
                    continue;
                }
                r = Response.byeResponse(iioex);
            } catch (IOException ioex) {

                r = Response.byeResponse(ioex);
            } catch (ProtocolException pex) {

                r = Response.byeResponse((Exception) pex);
            }
        }
        return r;
    }

    public boolean processIdleResponse(Response r) throws ProtocolException {
        Response[] responses = new Response[1];
        responses[0] = r;
        boolean done = false;
        notifyResponseHandlers(responses);

        if (r.isBYE()) {
            done = true;
        }

        if (r.isTagged() && r.getTag().equals(this.idleTag)) {
            done = true;
        }
        if (done) {
            this.idleTag = null;
        }
        handleResult(r);
        return !done;
    }

    private static final byte[] DONE = new byte[]{68, 79, 78, 69, 13, 10};

    public void idleAbort() throws ProtocolException {
        OutputStream os = getOutputStream();
        try {
            os.write(DONE);
            os.flush();
        } catch (IOException ex) {
        }
    }

}
