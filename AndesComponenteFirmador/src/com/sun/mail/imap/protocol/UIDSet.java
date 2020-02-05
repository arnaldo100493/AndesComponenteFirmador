/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.mail.imap.protocol;

import java.util.Vector;

/**
 *
 * @author abarrime
 */
public class UIDSet {

    public long start;
    public long end;

    public UIDSet() {
        this.start = 0L;
        this.end = 0L;
    }

    public UIDSet(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public long size() {
        return this.end - this.start + 1L;
    }

    public static UIDSet[] createUIDSets(long[] msgs) {
        Vector<UIDSet> v = new Vector();

        for (int i = 0; i < msgs.length; i++) {
            UIDSet ms = new UIDSet();
            ms.start = msgs[i];

            int j;
            for (j = i + 1; j < msgs.length
                    && msgs[j] == msgs[j - 1] + 1L; j++);

            ms.end = msgs[j - 1];
            v.addElement(ms);
            i = j - 1;
        }
        UIDSet[] msgsets = new UIDSet[v.size()];
        v.copyInto((Object[]) msgsets);
        return msgsets;
    }

    public static String toString(UIDSet[] msgsets) {
        if (msgsets == null || msgsets.length == 0) {
            return null;
        }
        int i = 0;
        StringBuffer s = new StringBuffer();
        int size = msgsets.length;

        while (true) {
            long start = (msgsets[i]).start;
            long end = (msgsets[i]).end;

            if (end > start) {
                s.append(start).append(':').append(end);
            } else {
                s.append(start);
            }
            i++;
            if (i >= size) {
                break;
            }
            s.append(',');
        }
        return s.toString();
    }

    public static long size(UIDSet[] msgsets) {
        long count = 0L;

        if (msgsets == null) {
            return 0L;
        }
        for (int i = 0; i < msgsets.length; i++) {
            count += msgsets[i].size();
        }
        return count;
    }

}
