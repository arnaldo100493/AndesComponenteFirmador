/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author abarrime
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MailSessionDefinition {

    public String description() default "";

    public String name();

    public String storeProtocol() default "";

    public String transportProtocol() default "";

    public String host() default "";

    public String user() default "";

    public String password() default "";

    public String from() default "";

    public String[] properties() default {};

}
