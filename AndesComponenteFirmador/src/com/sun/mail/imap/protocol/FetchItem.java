/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ParsingException;
import javax.mail.FetchProfile;

/**
 *
 * @author abarrime
 */
public abstract class FetchItem {

    private String name;
    private FetchProfile.Item fetchProfileItem;

    public FetchItem() {

    }

    public FetchItem(String name, FetchProfile.Item fetchProfileItem) {
        this.name = name;
        this.fetchProfileItem = fetchProfileItem;
    }

    public String getName() {
        return this.name;
    }

    public FetchProfile.Item getFetchProfileItem() {
        return this.fetchProfileItem;
    }

    public abstract Object parseItem(FetchResponse paramFetchResponse) throws ParsingException;

}
