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
public class MessageSet {

    public int start;
    public int end;

    public MessageSet() {
        this.start = 0;
        this.end = 0;
    }

    public MessageSet(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int size() {
        return this.end - this.start + 1;
    }

    public static MessageSet[] createMessageSets(int[] msgs) {
        Vector<MessageSet> v = new Vector();

        for (int i = 0; i < msgs.length; i++) {
            MessageSet ms = new MessageSet();
            ms.start = msgs[i];

            int j;
            for (j = i + 1; j < msgs.length
                    && msgs[j] == msgs[j - 1] + 1; j++);

            ms.end = msgs[j - 1];
            v.addElement(ms);
            i = j - 1;
        }
        MessageSet[] msgsets = new MessageSet[v.size()];
        v.copyInto((Object[]) msgsets);
        return msgsets;
    }

    public static String toString(MessageSet[] msgsets) {
        if (msgsets == null || msgsets.length == 0) {
            return null;
        }
        int i = 0;
        StringBuffer s = new StringBuffer();
        int size = msgsets.length;

        while (true) {
            int start = (msgsets[i]).start;
            int end = (msgsets[i]).end;

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

    public static int size(MessageSet[] msgsets) {
        int count = 0;

        if (msgsets == null) {
            return 0;
        }
        for (int i = 0; i < msgsets.length; i++) {
            count += msgsets[i].size();
        }
        return count;
    }

}
