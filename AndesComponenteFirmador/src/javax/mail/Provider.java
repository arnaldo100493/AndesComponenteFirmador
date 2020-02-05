/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail;

/**
 *
 * @author abarrime
 */
public class Provider {

    private Type type;
    private String protocol;
    private String className;
    private String vendor;
    private String version;

    public static class Type {

        public static final Type STORE = new Type("STORE");
        public static final Type TRANSPORT = new Type("TRANSPORT");

        private String type;

        private Type() {
            this.type = "";
        }

        private Type(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return this.type;
        }
    }

    public Provider() {
        this.type = null;
        this.protocol = "";
        this.className = "";
        this.vendor = "";
        this.version = "";
    }

    public Provider(Type type, String protocol, String classname, String vendor, String version) {
        this.type = type;
        this.protocol = protocol;
        this.className = classname;
        this.vendor = vendor;
        this.version = version;
    }

    public Type getType() {
        return this.type;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public String getClassName() {
        return this.className;
    }

    public String getVendor() {
        return this.vendor;
    }

    public String getVersion() {
        return this.version;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    
    @Override
    public String toString() {
        String s = "javax.mail.Provider[" + this.type + "," + this.protocol + "," + this.className;

        if (this.vendor != null) {
            s = s + "," + this.vendor;
        }
        if (this.version != null) {
            s = s + "," + this.version;
        }
        s = s + "]";
        return s;
    }

}
