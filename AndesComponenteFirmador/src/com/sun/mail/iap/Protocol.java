/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.mail.iap;

import com.sun.mail.util.MailLogger;
import com.sun.mail.util.PropUtil;
import com.sun.mail.util.SocketFetcher;
import com.sun.mail.util.TraceInputStream;
import com.sun.mail.util.TraceOutputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;

/**
 *
 * @author abarrime
 */
public class Protocol {

    protected String host;
    private Socket socket;
    protected boolean quote;
    protected MailLogger logger;
    protected MailLogger traceLogger;
    protected Properties props;
    protected String prefix;
    private boolean connected = false;
    private TraceInputStream traceInput;
    private volatile ResponseInputStream input;
    private TraceOutputStream traceOutput;
    private volatile DataOutputStream output;
    private int tagCounter = 0;

    private String localHostName;

    private final Vector handlers = new Vector();

    private volatile long timestamp;

    private static final byte[] CRLF = new byte[]{13, 10};

    public Protocol() throws IOException, ProtocolException {
        try {
            this.host = "";
            this.props = null;
            this.prefix = "";
            this.logger = null;
            this.traceLogger = null;

            initStreams();

            processGreeting(readResponse());

            this.timestamp = System.currentTimeMillis();

            this.connected = true;

        } finally {

            if (!this.connected) {
                disconnect();
            }
        }
    }

    public Protocol(String host, int port, Properties props, String prefix, boolean isSSL, MailLogger logger) throws IOException, ProtocolException {
        try {
            this.host = host;
            this.props = props;
            this.prefix = prefix;
            this.logger = logger;
            this.traceLogger = logger.getSubLogger("protocol", null);

            this.socket = SocketFetcher.getSocket(host, port, props, prefix, isSSL);
            this.quote = PropUtil.getBooleanProperty(props, "mail.debug.quote", false);

            initStreams();

            processGreeting(readResponse());

            this.timestamp = System.currentTimeMillis();

            this.connected = true;

        } finally {

            if (!this.connected) {
                disconnect();
            }
        }
    }

    private void initStreams() throws IOException {
        this.traceInput = new TraceInputStream(this.socket.getInputStream(), this.traceLogger);
        this.traceInput.setQuote(this.quote);
        this.input = new ResponseInputStream((InputStream) this.traceInput);

        this.traceOutput = new TraceOutputStream(this.socket.getOutputStream(), this.traceLogger);

        this.traceOutput.setQuote(this.quote);
        this.output = new DataOutputStream(new BufferedOutputStream((OutputStream) this.traceOutput));
    }

    public Protocol(InputStream in, PrintStream out, boolean debug) throws IOException {
        this.host = "localhost";
        this.quote = false;
        this.logger = new MailLogger(getClass(), "DEBUG", debug, out);
        this.traceLogger = this.logger.getSubLogger("protocol", null);

        this.traceInput = new TraceInputStream(in, this.traceLogger);
        this.traceInput.setQuote(this.quote);
        this.input = new ResponseInputStream((InputStream) this.traceInput);

        this.traceOutput = new TraceOutputStream(out, this.traceLogger);
        this.traceOutput.setQuote(this.quote);
        this.output = new DataOutputStream(new BufferedOutputStream((OutputStream) this.traceOutput));

        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void addResponseHandler(ResponseHandler h) {
        this.handlers.addElement(h);
    }

    public void removeResponseHandler(ResponseHandler h) {
        this.handlers.removeElement(h);
    }

    public void notifyResponseHandlers(Response[] responses) {
        if (this.handlers.size() == 0) {
            return;
        }
        for (int i = 0; i < responses.length; i++) {
            Response r = responses[i];

            if (r != null) {

                Object[] h = this.handlers.toArray();

                for (int j = 0; j < h.length; j++) {
                    if (h[j] != null) {
                        ((ResponseHandler) h[j]).handleResponse(r);
                    }
                }
            }
        }
    }

    protected void processGreeting(Response r) throws ProtocolException {
        if (r.isBYE()) {
            throw new ConnectionException(this, r);
        }
    }

    protected ResponseInputStream getInputStream() {
        return this.input;
    }

    protected OutputStream getOutputStream() {
        return this.output;
    }

    protected synchronized boolean supportsNonSyncLiterals() {
        return false;
    }

    public Response readResponse() throws IOException, ProtocolException {
        return new Response(this);
    }

    protected ByteArray getResponseBuffer() {
        return null;
    }

    public String writeCommand(String command, Argument args) throws IOException, ProtocolException {
        String tag = "A" + Integer.toString(this.tagCounter++, 10);

        this.output.writeBytes(tag + " " + command);

        if (args != null) {
            this.output.write(32);
            args.write(this);
        }

        this.output.write(CRLF);
        this.output.flush();
        return tag;
    }

    public synchronized Response[] command(String command, Argument args) {
        commandStart(command);
        Vector<Response> v = new Vector();
        boolean done = false;
        String tag = null;
        Response r = null;

        try {
            tag = writeCommand(command, args);
        } catch (LiteralException lex) {
            v.addElement(lex.getResponse());
            done = true;
        } catch (Exception ex) {

            v.addElement(Response.byeResponse(ex));
            done = true;
        }

        Response byeResp = null;
        while (!done) {
            try {
                r = readResponse();
            } catch (IOException ioex) {
                if (byeResp != null) {
                    break;
                }
                r = Response.byeResponse(ioex);
            } catch (ProtocolException pex) {
                continue;
            }

            if (r.isBYE()) {
                byeResp = r;

                continue;
            }
            v.addElement(r);

            if (r.isTagged() && r.getTag().equals(tag)) {
                done = true;
            }
        }
        if (byeResp != null) {
            v.addElement(byeResp);
        }
        Response[] responses = new Response[v.size()];
        v.copyInto((Object[]) responses);
        this.timestamp = System.currentTimeMillis();
        commandEnd();
        return responses;
    }

    public void handleResult(Response response) throws ProtocolException {
        if (response.isOK()) {
            return;
        }
        if (response.isNO()) {
            throw new CommandFailedException(response);
        }
        if (response.isBAD()) {
            throw new BadCommandException(response);
        }
        if (response.isBYE()) {
            disconnect();
            throw new ConnectionException(this, response);
        }
    }

    public void simpleCommand(String cmd, Argument args) throws ProtocolException {
        Response[] r = command(cmd, args);

        notifyResponseHandlers(r);

        handleResult(r[r.length - 1]);
    }

    public synchronized void startTLS(String cmd) throws IOException, ProtocolException {
        if (this.socket instanceof javax.net.ssl.SSLSocket) {
            return;
        }
        simpleCommand(cmd, null);
        this.socket = SocketFetcher.startTLS(this.socket, this.host, this.props, this.prefix);
        initStreams();
    }

    public boolean isSSL() {
        return this.socket instanceof javax.net.ssl.SSLSocket;
    }

    protected synchronized void disconnect() {
        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (IOException e) {
            }

            this.socket = null;
        }
    }

    protected synchronized String getLocalHost() {
        if (this.localHostName == null || this.localHostName.length() <= 0) {
            this.localHostName = this.props.getProperty(this.prefix + ".localhost");
        }
        if (this.localHostName == null || this.localHostName.length() <= 0) {
            this.localHostName = this.props.getProperty(this.prefix + ".localaddress");
        }
        try {
            if (this.localHostName == null || this.localHostName.length() <= 0) {
                InetAddress localHost = InetAddress.getLocalHost();
                this.localHostName = localHost.getCanonicalHostName();

                if (this.localHostName == null) {
                    this.localHostName = "[" + localHost.getHostAddress() + "]";
                }
            }
        } catch (UnknownHostException uhex) {
        }

        if ((this.localHostName == null || this.localHostName.length() <= 0)
                && this.socket != null && this.socket.isBound()) {
            InetAddress localHost = this.socket.getLocalAddress();
            this.localHostName = localHost.getCanonicalHostName();

            if (this.localHostName == null) {
                this.localHostName = "[" + localHost.getHostAddress() + "]";
            }
        }
        return this.localHostName;
    }

    protected boolean isTracing() {
        return this.traceLogger.isLoggable(Level.FINEST);
    }

    protected void suspendTracing() {
        if (this.traceLogger.isLoggable(Level.FINEST)) {
            this.traceInput.setTrace(false);
            this.traceOutput.setTrace(false);
        }
    }

    protected void resumeTracing() {
        if (this.traceLogger.isLoggable(Level.FINEST)) {
            this.traceInput.setTrace(true);
            this.traceOutput.setTrace(true);
        }
    }

    protected void finalize() throws Throwable {
        super.finalize();
        disconnect();
    }

    private void commandStart(String command) {
    }

    private void commandEnd() {
    }

}
