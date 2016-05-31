package org.rundeck.util.toolbelt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Construct subcommands
 */
public class ToolBelt {
    private CommandSet commands;
    private CommandInput inputParser;
    private Set<String> helpCommands;
    private CommandOutput commandOutput;
    private OutputFormatter formatter;

    /**
     * @return new ToolBelt
     */
    public static ToolBelt builder() {
        return new ToolBelt();
    }

    private ToolBelt() {
        commands = new CommandSet();
        helpCommands = new HashSet<>();
        formatter = new ToStringFormatter();
    }

    /**
     * Add objects as commands
     *
     * @param instance objects
     *
     * @return this
     */
    public ToolBelt addCommands(final Object... instance) {
        for (Object o : instance) {
            addCommand(o);
        }
        return this;
    }

    /**
     * Use "-h","help","?" as help commands
     *
     * @return this
     */
    public ToolBelt defaultHelp() {
        return helpCommands("-h", "--help", "help", "?");
    }

    /**
     * Define commands indicating help
     *
     * @param commands list of commands
     *
     * @return this
     */
    public ToolBelt helpCommands(String... commands) {
        helpCommands.addAll(Arrays.asList(commands));
        return this;
    }

    /**
     * Add object as a command, the object must have the {@link Command} annotation on it.
     *
     * @param instance object
     *
     * @return this
     */
    public ToolBelt addCommand(final Object instance) {
        introspect(instance);
        return this;
    }

    /**
     * Use system out/err for command output
     *
     * @return this
     */
    public ToolBelt systemOutput() {
        commandOutput = new BaseSystemCommandOutput();
        return this;
    }

    private static class CommandContext {
        private CommandInput inputParser;
        private CommandOutput output;


        CommandInput getInputParser() {
            return inputParser;
        }

        public CommandOutput getOutput() {
            return output;
        }

        void setInputParser(CommandInput inputParser) {
            this.inputParser = inputParser;
        }

        public void setOutput(CommandOutput output) {
            this.output = output;
        }
    }

    private static class CommandSet implements Tool, CommandInvoker {
        Map<String, CommandInvoker> commands;
        String defCommand;
        Set<String> helpCommands;
        public String description;
        CommandContext context;

        CommandSet() {
            commands = new HashMap<>();
            helpCommands = new HashSet<>();
        }


        @Override
        public boolean runMain(final String[] args, final boolean exitSystem) throws CommandRunFailure {
            boolean result = run(args);
            if (!result && exitSystem) {
                System.exit(2);
            }
            return result;
        }

        @Override
        public boolean run(final String[] args)
                throws CommandRunFailure
        {
            String[] cmdArgs = args;
            String cmd = defCommand;
            if (args.length > 0 && !(args[0].startsWith("-") && null != defCommand)) {
                cmd = args[0];
                cmdArgs = tail(args);
            }
            if (null == cmd) {
                context.getOutput().error(String.format(
                        "No command was specified. Available commands: %s",
                        commands.keySet()
                ));
                context.getOutput().error(String.format("You can use: COMMAND %s to get help.", helpCommands));
                return false;
            }
            if (helpCommands.contains(cmd)) {
                getHelp();
                return false;
            }
            return runCommand(cmd, cmdArgs);
        }

        @Override
        public void getHelp() throws CommandRunFailure {
            if (description != null && !"".equals(description)) {
                context.getOutput().output(description);
            }
            boolean multi = commands.size() > 1;
            if (multi) {
                context.getOutput().output(
                        String.format(
                                "Available commands: %s",
                                commands.keySet()
                        )
                );
            }
            for (String command : commands.keySet()) {
                CommandInvoker commandInvoker = commands.get(command);

                if (multi) {
                    context.getOutput().output("--------------------");
                    context.getOutput().output("+ Command: " + command);
                }
                commandInvoker.getHelp();
            }
        }

        boolean runCommand(String cmd, String[] args) throws CommandRunFailure
        {
            CommandInvoker commandInvoke = commands.get(cmd);
            if (null == commandInvoke) {
                throw new CommandRunFailure(String.format(
                        "No such command: %s. Available commands: %s",
                        cmd,
                        commands.keySet()
                ));
            }
            if (args.length > 0 && helpCommands.contains(args[0])) {
                commandInvoke.getHelp();
                return false;
            }
            try {
                return commandInvoke.run(args);
            } catch (InputError inputError) {
                context.getOutput().error(String.format(
                        "Error parsing arguments for [%s]: %s",
                        cmd,
                        inputError.getMessage()
                ));
                context.getOutput().error(String.format(
                        "You can use: \"%s %s\" to get help.",
                        cmd,
                        helpCommands.iterator().next()
                ));
                return false;
            }
        }

    }

    public static String[] tail(final String[] args) {
        List<String> strings = new ArrayList<>(Arrays.asList(args));
        strings.remove(0);
        return strings.toArray(new String[strings.size()]);
    }

    private void introspect(final Object instance) {
        HashMap<String, CommandInvoke> subCommands = new HashMap<>();
        //look for methods
        Class<?> aClass = instance.getClass();
        Command annotation1 = aClass.getAnnotation(Command.class);
        if (null == annotation1) {
            throw new IllegalArgumentException("Specified object has no @Command annotation: " + aClass);
        }
        String cmd = annotation1.value();
        if ("".equals(cmd)) {
            cmd = aClass.getSimpleName().toLowerCase();
        }
        String cmdDescription = annotation1.description();
        CommandContext context = new CommandContext();
        Method[] methods = aClass.getMethods();
        String defInvoke = null;
        for (Method method : methods) {
            Command annotation = method.getAnnotation(Command.class);
            if (annotation != null) {
                String name = annotation.value();
                if ("".equals(name)) {
                    name = method.getName().toLowerCase();
                }
                CommandInvoke value = new CommandInvoke(name, method, instance, context);
                value.description = annotation.description();
                value.solo = annotation.isSolo();
                subCommands.put(name, value);
                if (annotation.isDefault()) {
                    defInvoke = name;
                }
            }
        }
        if (subCommands.size() < 1) {
            throw new IllegalArgumentException("Specified object has no methods with @Command annotation: " + aClass);
        }

        CommandSet commandSet = new CommandSet();
        commandSet.context = context;
        commandSet.helpCommands = helpCommands;
        commandSet.description = cmdDescription;
        if (subCommands.size() == 1) {
            //single command

            defInvoke = subCommands.keySet().iterator().next();
            commandSet.commands.putAll(subCommands);
            commandSet.defCommand = defInvoke;
        } else {
            commandSet.commands.putAll(subCommands);
            commandSet.defCommand = defInvoke;
        }
        commands.commands.put(cmd, commandSet);

    }

    /**
     * Set input parser
     *
     * @param input input parser
     *
     * @return this
     */
    public ToolBelt setParser(CommandInput input) {
        this.inputParser = input;
        return this;
    }


    /**
     * Build the Tool
     *
     * @return new Tool
     */
    public Tool buckle() {
        commands.context.setInputParser(inputParser);
        commands.helpCommands = helpCommands;
        if (commands.commands.size() == 1) {
            commands.defCommand = commands.commands.keySet().iterator().next();
        }
        commands.context.setOutput(new FormattedOutput(commandOutput, formatter));
        return commands;
    }

    private interface CommandInvoker {
        boolean run(String[] args) throws CommandRunFailure, InputError;

        void getHelp() throws CommandRunFailure;
    }

    private static class CommandInvoke implements CommandInvoker {
        String name;
        Method method;
        Object instance;
        public String description;
        boolean solo;
        CommandContext context;

        CommandInvoke(
                final String name,
                final Method method,
                final Object instance,
                final CommandContext context
        )
        {
            this.name = name;
            this.method = method;
            this.instance = instance;
            this.context = context;
        }


        public boolean run(String[] args) throws CommandRunFailure, InputError {
            //get configured arguments to the method
            Class[] parameters = method.getParameterTypes();
            Object[] objArgs = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                Class<?> type = parameters[i];

                if (type.isAssignableFrom(CommandOutput.class)) {
                    objArgs[i] = context.getOutput();
                } else {
                    Object t = context.getInputParser().parseArgs(args, type);

                    objArgs[i] = t;
                }
            }
            Object invoke = null;
            try {
                invoke = method.invoke(instance, objArgs);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            if (invoke != null && invoke instanceof Boolean) {
                return ((Boolean) invoke);
            }
            //TODO: format output
            return true;
        }

        @Override
        public void getHelp() throws CommandRunFailure {
            Class[] parameters = method.getParameterTypes();

            if (description != null && !"".equals(description)) {
                context.getOutput().output(description);
            }
            if (parameters.length == 0) {
                context.getOutput().output("(no options for this command)");
            }
            for (Class<?> type : parameters) {
                if (type.isAssignableFrom(CommandOutput.class)) {
                    continue;
                }

                String helpt = context.getInputParser().getHelp(type);

                context.getOutput().output(helpt);
            }
        }


    }
}
