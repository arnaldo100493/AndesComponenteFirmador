/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.mail.util;

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Session;

/**
 *ñ--+
 * @author abarrime
 */
public final class MailLogger {

    private final Logger logger;
    private final String prefix;
    private final boolean debug;
    private final PrintStream out;
    
    public MailLogger(){
        this.logger = null;
        this.prefix = "";
        this.debug = false;
        this.out = null;
    }

    public MailLogger(String name, String prefix, boolean debug, PrintStream out) {
        this.logger = Logger.getLogger(name);
        this.prefix = prefix;
        this.debug = debug;
        this.out = (out != null) ? out : System.out;
    }

    public MailLogger(Class clazz, String prefix, boolean debug, PrintStream out) {
        String name = packageOf(clazz);
        this.logger = Logger.getLogger(name);
        this.prefix = prefix;
        this.debug = debug;
        this.out = (out != null) ? out : System.out;
    }

    public MailLogger(Class clazz, String subname, String prefix, boolean debug, PrintStream out) {
        String name = packageOf(clazz) + "." + subname;
        this.logger = Logger.getLogger(name);
        this.prefix = prefix;
        this.debug = debug;
        this.out = (out != null) ? out : System.out;
    }

    public MailLogger(String name, String prefix, Session session) {
        this(name, prefix, session.getDebug(), session.getDebugOut());
    }

    public MailLogger(Class clazz, String prefix, Session session) {
        this(clazz, prefix, session.getDebug(), session.getDebugOut());
    }

    public MailLogger getLogger(String name, String prefix) {
        return new MailLogger(name, prefix, this.debug, this.out);
    }

    public MailLogger getLogger(Class clazz, String prefix) {
        return new MailLogger(clazz, prefix, this.debug, this.out);
    }

    public MailLogger getSubLogger(String subname, String prefix) {
        return new MailLogger(this.logger.getName() + "." + subname, prefix, this.debug, this.out);
    }

    public MailLogger getSubLogger(String subname, String prefix, boolean debug) {
        return new MailLogger(this.logger.getName() + "." + subname, prefix, debug, this.out);
    }

    public void log(Level level, String msg) {
        ifDebugOut(msg);
        if (this.logger.isLoggable(level)) {
            String[] frame = inferCaller();
            this.logger.logp(level, frame[0], frame[1], msg);
        }
    }

    public void log(Level level, String msg, Object param1) {
        if (this.debug) {
            msg = MessageFormat.format(msg, new Object[]{param1});
            debugOut(msg);
        }

        if (this.logger.isLoggable(level)) {
            String[] frame = inferCaller();
            this.logger.logp(level, frame[0], frame[1], msg, param1);
        }
    }

    public void log(Level level, String msg, Object[] params) {
        if (this.debug) {
            msg = MessageFormat.format(msg, params);
            debugOut(msg);
        }

        if (this.logger.isLoggable(level)) {
            String[] frame = inferCaller();
            this.logger.logp(level, frame[0], frame[1], msg, params);
        }
    }

    public void log(Level level, String msg, Throwable thrown) {
        if (this.debug) {
            if (thrown != null) {
                debugOut(msg + ", THROW: ");
                thrown.printStackTrace(this.out);
            } else {
                debugOut(msg);
            }
        }

        if (this.logger.isLoggable(level)) {
            String[] frame = inferCaller();
            this.logger.logp(level, frame[0], frame[1], msg, thrown);
        }
    }

    public void config(String msg) {
        log(Level.CONFIG, msg);
    }

    public void fine(String msg) {
        log(Level.FINE, msg);
    }

    public void finer(String msg) {
        log(Level.FINER, msg);
    }

    public void finest(String msg) {
        log(Level.FINEST, msg);
    }

    public boolean isLoggable(Level level) {
        return (this.debug || this.logger.isLoggable(level));
    }

    private final void ifDebugOut(String msg) {
        if (this.debug) {
            debugOut(msg);
        }
    }

    private final void debugOut(String msg) {
        if (this.prefix != null) {
            this.out.println(this.prefix + ": " + msg);
        } else {
            this.out.println(msg);
        }
    }

    private String packageOf(Class clazz) {
        Package p = clazz.getPackage();
        if (p != null) {
            return p.getName();
        }
        String cname = clazz.getName();
        int i = cname.lastIndexOf('.');
        if (i > 0) {
            return cname.substring(0, i);
        }
        return "";
    }

    private String[] inferCaller() {
        StackTraceElement[] stack = (new Throwable()).getStackTrace();

        int ix = 0;
        while (ix < stack.length) {
            StackTraceElement frame = stack[ix];
            String cname = frame.getClassName();
            if (isLoggerImplFrame(cname)) {
                break;
            }
            ix++;
        }

        while (ix < stack.length) {
            StackTraceElement frame = stack[ix];
            String cname = frame.getClassName();
            if (!isLoggerImplFrame(cname)) {
                return new String[]{cname, frame.getMethodName()};
            }
            ix++;
        }

        return new String[]{null, null};
    }

    private boolean isLoggerImplFrame(String cname) {
        return MailLogger.class.getName().equals(cname);
    }

}
