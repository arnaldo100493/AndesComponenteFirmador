/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail.search;

/**
 *
 * @author abarrime
 */
public abstract class StringTerm extends SearchTerm {

    protected String pattern;
    protected boolean ignoreCase;
    private static final long serialVersionUID = 1274042129007696269L;

    protected StringTerm() {
        this.pattern = "";
        this.ignoreCase = false;
    }

    protected StringTerm(String pattern) {
        this.pattern = pattern;
        this.ignoreCase = true;
    }

    protected StringTerm(String pattern, boolean ignoreCase) {
        this.pattern = pattern;
        this.ignoreCase = ignoreCase;
    }

    public String getPattern() {
        return this.pattern;
    }

    public boolean getIgnoreCase() {
        return this.ignoreCase;
    }

    protected boolean match(String s) {
        int len = s.length() - this.pattern.length();
        for (int i = 0; i <= len; i++) {
            if (s.regionMatches(this.ignoreCase, i, this.pattern, 0, this.pattern.length())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StringTerm)) {
            return false;
        }
        StringTerm st = (StringTerm) obj;
        if (this.ignoreCase) {
            return (st.pattern.equalsIgnoreCase(this.pattern) && st.ignoreCase == this.ignoreCase);
        }

        return (st.pattern.equals(this.pattern) && st.ignoreCase == this.ignoreCase);
    }

    @Override
    public int hashCode() {
        return this.ignoreCase ? this.pattern.hashCode() : (this.pattern.hashCode() ^ 0xFFFFFFFF);
    }

}
