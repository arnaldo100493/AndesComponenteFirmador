/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail.search;

import javax.mail.Flags;
import javax.mail.Message;

/**
 *
 * @author abarrime
 */
public final class FlagTerm extends SearchTerm {

    private boolean set;
    private Flags flags;
    private static final long serialVersionUID = -142991500302030647L;

    public FlagTerm() {
        this.flags = null;
        this.set = false;
    }

    public FlagTerm(Flags flags, boolean set) {
        this.flags = flags;
        this.set = set;
    }

    public Flags getFlags() throws CloneNotSupportedException {
        return (Flags) this.flags.clone();
    }

    public boolean getTestSet() {
        return this.set;
    }

    @Override
    public boolean match(Message msg) {
        try {
            Flags f = msg.getFlags();
            if (this.set) {
                if (f.contains(this.flags)) {
                    return true;
                }
                return false;
            }

            Flags.Flag[] sf = this.flags.getSystemFlags();

            for (int i = 0; i < sf.length; i++) {
                if (f.contains(sf[i])) {
                    return false;
                }
            }
            String[] s = this.flags.getUserFlags();

            for (int i = 0; i < s.length; i++) {
                if (f.contains(s[i])) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof FlagTerm)) {
            return false;
        }
        FlagTerm ft = (FlagTerm) obj;
        return (ft.set == this.set && ft.flags.equals(this.flags));
    }

    public int hashCode() {
        return this.set ? this.flags.hashCode() : (this.flags.hashCode() ^ 0xFFFFFFFF);
    }

}
