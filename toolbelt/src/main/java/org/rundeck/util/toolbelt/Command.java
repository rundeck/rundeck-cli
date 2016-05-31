package org.rundeck.util.toolbelt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declare a class or method as a command. The "value" can be used to indicate the name of the
 * command, otherwise, the lowercased class/method name is used.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Command {
    /**
     * @return the command name
     */
    String value() default "";

    /**
     * @return true if this is a subcommand and should be invoked by default when no subcommand is specified
     */
    boolean isDefault() default false;

    /**
     * @return true if this method should be treated as the only subcommand, and so be assumed by the top-level command
     */
    boolean isSolo() default false;

    /**
     * @return help text description
     */
    String description() default "";
}
