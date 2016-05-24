package org.rundeck.client.belt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by greg on 5/23/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface Command {
    String value() default "";
    boolean isDefault() default false;
    boolean isSolo() default false;
    String description() default "";
}
