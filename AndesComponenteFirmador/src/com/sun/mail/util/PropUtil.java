/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.mail.util;

import java.util.Properties;
import javax.mail.Session;

/**
 *
 * @author abarrime
 */
public class PropUtil {

    public PropUtil() {

    }

    public static int getIntProperty(Properties props, String name, int def) {
        return getInt(getProp(props, name), def);
    }

    public static boolean getBooleanProperty(Properties props, String name, boolean def) {
        return getBoolean(getProp(props, name), def);
    }

    public static int getIntSessionProperty(Session session, String name, int def) {
        return getInt(getProp(session.getProperties(), name), def);
    }

    public static boolean getBooleanSessionProperty(Session session, String name, boolean def) {
        return getBoolean(getProp(session.getProperties(), name), def);
    }

    public static boolean getBooleanSystemProperty(String name, boolean def) {
        try {
            return getBoolean(getProp(System.getProperties(), name), def);
        } catch (SecurityException sex) {

            try {

                String value = System.getProperty(name);
                if (value == null) {
                    return def;
                }
                if (def) {
                    return !value.equalsIgnoreCase("false");
                }
                return value.equalsIgnoreCase("true");
            } catch (SecurityException s) {
                return def;
            }
        }
    }

    private static Object getProp(Properties props, String name) {
        Object val = props.get(name);
        if (val != null) {
            return val;
        }
        return props.getProperty(name);
    }

    private static int getInt(Object value, int def) {
        if (value == null) {
            return def;
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException nfex) {
            }
        }
        if (value instanceof Integer) {
            return ((Integer) value).intValue();
        }
        return def;
    }

    private static boolean getBoolean(Object value, boolean def) {
        if (value == null) {
            return def;
        }
        if (value instanceof String) {

            if (def) {
                return !((String) value).equalsIgnoreCase("false");
            }
            return ((String) value).equalsIgnoreCase("true");
        }
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        return def;
    }

}
