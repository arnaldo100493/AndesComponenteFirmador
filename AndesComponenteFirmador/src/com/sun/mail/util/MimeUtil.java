/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.mail.util;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.mail.internet.MimePart;

/**
 *
 * @author abarrime
 */
public class MimeUtil {

    private static final Method cleanContentType;

    public MimeUtil() {

    }

    static {
        Method meth = null;
        try {
            String cth = System.getProperty("mail.mime.contenttypehandler");
            if (cth != null) {
                ClassLoader cl = getContextClassLoader();
                Class<?> clsHandler = null;
                if (cl != null) {
                    try {
                        clsHandler = Class.forName(cth, false, cl);
                    } catch (ClassNotFoundException cex) {
                    }
                }
                if (clsHandler == null) {
                    clsHandler = Class.forName(cth);
                }
                meth = clsHandler.getMethod("cleanContentType", new Class[]{MimePart.class, String.class});
            }

        } catch (ClassNotFoundException ex) {

        } catch (NoSuchMethodException ex) {

        } catch (RuntimeException ex) {

        } finally {
            cleanContentType = meth;
        }
    }

    public static String cleanContentType(MimePart mp, String contentType) {
        if (cleanContentType != null) {
            try {
                return (String) cleanContentType.invoke(null, new Object[]{mp, contentType});
            } catch (Exception ex) {
                return contentType;
            }
        }
        return contentType;
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

}
