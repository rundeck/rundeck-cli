/**
 * <p>Defines a set of commands with subcommands, using annotations to indicate methods to expose as subcommands.</p>
 *
 * <p>Simplest usage:</p>
 * <pre><code>
 *     class Greet{
 *         {@literal @}Command void hi({@literal @}Arg("name") String name){
 *             System.out.println("Hello, "+name+".");
 *         }
 *         {@literal @}Command void remark({@literal @}Arg("age") int age){
 *             System.out.println("I see you are "+age+" years old.");
 *         }
 *     }
 *     class Main{
 *         public static void main(String[] args){
 *             ToolBelt.with(new Greet()).runMain(args);
 *         }
 *     }
 * </code></pre>
 * <p>Commandline: </p>
 * <code><pre>
 *     $ java ... Main greet hi --name bob
 *     Hello, bob.
 *     $ java ... Main greet remark --age 33
 *     I see you are 33 years old.
 * </pre></code>
 * <p>
 * This constructs a {@link org.rundeck.util.toolbelt.Tool} object, with a command "greet" (based on the class name
 * Greet).  "greet" has a "hi" and a "remark" subcommand. The class must have at least
 * one method annotated with {@link org.rundeck.util.toolbelt.Command @Command}.  The parameters of that
 * method (if any) should be annotated with {@link org.rundeck.util.toolbelt.Arg @Arg} to define their names.
 * (Alternately, if you compile your java class with {@code -parameters} flag to javac, the parameter names will be
 * introspected.)
 * This will use the {@link org.rundeck.util.toolbelt.SimpleCommandInput} to parse "--somearg value" for a
 * method parameter with arg name "somearg".
 * </p>
 * <p>You can define multiple commands (with their subcommands) in one step:</p>
 * <code><pre>
 *             ToolBelt.with(new Command1(), new Command2(),...).runMain(args);
 *</pre></code>
 * <p>
 * For more advanced usage, see below:
 * </p>
 * <p>
 * Use {@link org.rundeck.util.toolbelt.ToolBelt#belt()} to create a builder.
 * </p>
 * <pre><code>
 * ToolBelt.belt()
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
 * Within your MyCommand classes, use the {@link org.rundeck.util.toolbelt.Command @Command} annotation to indicate the
 * class is a top-level command (optional).  Add the same annotation on any methods within the class to expose them
 * as subcommands. At least one method should be annotated this way.
 * </p>
 * <pre><code>
 * {@literal @}Command(description = "Does something", name="doit")
 * public class MyCommand{
 *     {@literal @}Command(name="sub1") public void sub1(InputArgs args){
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