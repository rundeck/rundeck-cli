/**
 * Defines a set of commands with subcommands, using annotations to indicate methods to expose as subcommands.
 *
 * <p>
 * Use {@link org.rundeck.util.toolbelt.ToolBelt#builder()} to create a builder.
 * </p>
 * <pre><code>
 * ToolBelt.builder()
 *   .defaultHelp()
 *   .systemOutput()
 *   .addCommands(
 *     new MyCommand(),
 *     new MyCommand2()
 *   )
 *   .setParser(new JewelInput())
 *   .buckle();
 * </code></pre>
 * <p>
 * Within the MyCommand class, use the {@link org.rundeck.util.toolbelt.Command} annotation to indicate the
 * class is a top-level command.  Add the same annotation on any methods.
 * </p>
 * <pre><code>
 * \@Command(description = "Does something", name="doit")
 * public class MyCommand{
 *     \@Command(name="sub1") public void sub1(InputArgs args){
 *         System.out.println("Input args: "+args);
 *     }
 * }
 * </code></pre>
 *
 * <p>
 * The annotation can exclude the "name" attribute, and the lowercase name of the class or method is used as the
 * command/subcommand.  The method parameters will be parsed using the input parser (e.g. JewelCLI), so
 * <code>InputArgs</code> must be defined with appropriate annotations.
 *
 * </p>
 */
package org.rundeck.util.toolbelt;