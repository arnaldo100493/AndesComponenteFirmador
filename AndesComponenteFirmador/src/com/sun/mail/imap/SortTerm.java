/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.mail.imap;

/**
 *
 * @author abarrime
 */
public final class SortTerm {

    public static final SortTerm ARRIVAL = new SortTerm("ARRIVAL");

    public static final SortTerm CC = new SortTerm("CC");

    public static final SortTerm DATE = new SortTerm("DATE");

    public static final SortTerm FROM = new SortTerm("FROM");

    public static final SortTerm REVERSE = new SortTerm("REVERSE");

    public static final SortTerm SIZE = new SortTerm("SIZE");

    public static final SortTerm SUBJECT = new SortTerm("SUBJECT");

    public static final SortTerm TO = new SortTerm("TO");

    private String term;

    private SortTerm(String term) {
        this.term = term;
    }

    @Override
    public String toString() {
        return this.term;
    }

}
