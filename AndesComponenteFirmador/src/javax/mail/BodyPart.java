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
public abstract class BodyPart implements Part {

    protected Multipart parent;

    public BodyPart() {
    
    }

    public BodyPart(Multipart parent) {
        this.parent = parent;
    }    

    public Multipart getParent() {
        return this.parent;
    }

    public void setParent(Multipart parent) {
        this.parent = parent;
    }
}
