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
public final class PasswordAuthentication {

    private final String userName;
    private final String password;
    
    public PasswordAuthentication(){
        this.userName = "";
        this.password = "";
    }

    public PasswordAuthentication(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getPassword() {
        return this.password;
    }

}
