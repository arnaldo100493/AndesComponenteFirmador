/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.mail.imap;

import com.sun.mail.imap.protocol.MessageSet;
import com.sun.mail.imap.protocol.UIDSet;
import java.util.Vector;
import javax.mail.Message;

/**
 *
 * @author abarrime
 */
public class Utility {

    public static MessageSet[] toMessageSet(Message[] msgs, Condition cond) {
        Vector<MessageSet> v = new Vector(1);

        for (int i = 0; i < msgs.length; i++) {
            IMAPMessage msg = (IMAPMessage) msgs[i];
            if (!msg.isExpunged()) {

                int current = msg.getSequenceNumber();

                if (cond == null || cond.test(msg)) {

                    MessageSet set = new MessageSet();
                    set.start = current;

                    for (; ++i < msgs.length; i++) {

                        msg = (IMAPMessage) msgs[i];

                        if (!msg.isExpunged()) {

                            int next = msg.getSequenceNumber();

                            if (cond == null || cond.test(msg)) {

                                if (next == current + 1) {
                                    current = next;

                                } else {

                                    i--;
                                    break;
                                }
                            }
                        }
                    }
                    set.end = current;
                    v.addElement(set);
                }
            }
        }
        if (v.isEmpty()) {
            return null;
        }
        MessageSet[] sets = new MessageSet[v.size()];
        v.copyInto((Object[]) sets);
        return sets;
    }

    public static UIDSet[] toUIDSet(Message[] msgs) {
        Vector<UIDSet> v = new Vector(1);

        for (int i = 0; i < msgs.length; i++) {
            IMAPMessage msg = (IMAPMessage) msgs[i];
            if (!msg.isExpunged()) {

                long current = msg.getUID();

                UIDSet set = new UIDSet();
                set.start = current;

                for (; ++i < msgs.length; i++) {

                    msg = (IMAPMessage) msgs[i];

                    if (!msg.isExpunged()) {

                        long next = msg.getUID();

                        if (next == current + 1L) {
                            current = next;

                        } else {

                            i--;
                            break;
                        }
                    }
                }
                set.end = current;
                v.addElement(set);
            }
        }
        if (v.isEmpty()) {
            return null;
        }
        UIDSet[] sets = new UIDSet[v.size()];
        v.copyInto((Object[]) sets);
        return sets;
    }

    public static interface Condition {

        boolean test(IMAPMessage param1IMAPMessage);
    }

}
