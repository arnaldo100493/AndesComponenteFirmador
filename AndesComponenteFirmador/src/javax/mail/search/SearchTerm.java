/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail.search;

import java.io.Serializable;
import javax.mail.Message;

/**
 *
 * @author abarrime
 */
public abstract class SearchTerm implements Serializable {

    private static final long serialVersionUID = -6652358452205992789L;

    public SearchTerm() {

    }

    public abstract boolean match(Message paramMessage);

}
