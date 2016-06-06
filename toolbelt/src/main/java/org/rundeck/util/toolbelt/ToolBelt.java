package org.rundeck.util.toolbelt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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
     * Create a simple CLI tool for the object, using {@link SimpleCommandInput} to parse
     * CLI args into  method parameters
     *
     * @param commands
     *
     * @return
     */
    public static Tool with(Object... commands) {
        return with(new SimpleCommandInput(), commands);
    }

    /**
     * Create a simple CLI tool for the object, using the specified input parser to parse
     * CLI args into  method parameters
     *
     * @param commands
     *
     * @return
     */
    public static Tool with(CommandInput input, Object... commands) {
        return belt().defaultHelpCommands()
                     .commandInput(input)
                     .systemOutput()
                     .add(commands)
                     .buckle();
    }

    /**
     * Create a simple CLI tool for the object, using the specified input parser to parse
     * CLI args into  method parameters
     *
     * @param commands
     *
     * @return
     */
    public static Tool with(CommandOutput output, Object... commands) {
        return belt().defaultHelpCommands()
                     .commandInput(new SimpleCommandInput())
                     .commandOutput(output)
                     .add(commands)
                     .buckle();
    }

    /**
     * Create a simple CLI tool for the object, using the specified input parser to parse
     * CLI args into  method parameters
     *
     * @param commands
     *
     * @return
     */
    public static Tool with(CommandInput input, CommandOutput output, Object... commands) {
        return belt().defaultHelpCommands()
                     .commandInput(input)
                     .commandOutput(output)
                     .add(commands)
                     .buckle();
    }

    /**
     * @return new ToolBelt
     */
    public static ToolBelt belt() {
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
    public ToolBelt add(final Object... instance) {
        Arrays.asList(instance).forEach(this::introspect);
        return this;
    }

    /**
     * Use "-h","help","?" as help commands
     *
     * @return this
     */
    public ToolBelt defaultHelpCommands() {
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
     * Use system out/err for command output
     *
     * @return this
     */
    public ToolBelt systemOutput() {
        return commandOutput(new SystemOutput());
    }

    /**
     * Use system out/err for command output
     *
     * @return this
     */
    public ToolBelt commandOutput(CommandOutput output) {
        commandOutput = output;
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
            context = new CommandContext();
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
        introspect(commands, instance);
    }
    private void introspect(CommandSet parent, final Object instance) {
        HashMap<String, CommandInvoke> subCommands = new HashMap<>();
        //look for methods
        Class<?> aClass = instance.getClass();
        Command annotation1 = aClass.getAnnotation(Command.class);
        String cmd = null != annotation1 ? annotation1.value() : "";
        if ("".equals(cmd)) {
            cmd = aClass.getSimpleName().toLowerCase();
        }
        String cmdDescription = null != annotation1 ? annotation1.description() : null;
        boolean isSub=false;
        if(null==annotation1){
            SubCommand annotation2 = aClass.getAnnotation(SubCommand.class);
            if (null != annotation2) {
                isSub=true;
            }
        }
        Method[] methods = aClass.getMethods();
        String defInvoke = null;
        for (Method method : methods) {
            Command annotation = method.getAnnotation(Command.class);
            if (annotation != null) {
                String name = annotation.value();
                if ("".equals(name)) {
                    name = method.getName().toLowerCase();
                }
                CommandInvoke value = new CommandInvoke(name, method, instance, commands.context);
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
        commandSet.context = commands.context;
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
        if(instance instanceof HasSubCommands){
            HasSubCommands subs = (HasSubCommands) instance;
            List<Object> subCommands1 = subs.getSubCommands();
            subCommands1.forEach(o -> introspect(commandSet, o));
        }
        if(!isSub) {
            parent.commands.put(cmd, commandSet);
        }else{
            parent.commands.putAll(commandSet.commands);
        }

    }

    /**
     * Set input parser
     *
     * @param input input parser
     *
     * @return this
     */
    public ToolBelt commandInput(CommandInput input) {
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
            Parameter[] params = method.getParameters();
            Object[] objArgs = new Object[parameters.length];
            for (int i = 0; i < params.length; i++) {
                Class<?> type = parameters[i];
                String paramName = getParameterName(params[i]);

                if (type.isAssignableFrom(CommandOutput.class)) {
                    objArgs[i] = context.getOutput();
                } else if (type.isAssignableFrom(String[].class)) {
                    objArgs[i] = args;
                } else {
                    Object t = context.getInputParser().parseArgs(name, args, type, paramName);

                    objArgs[i] = t;
                }
            }
            Object invoke = null;
            try {
                invoke = method.invoke(instance, objArgs);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return false;
            } catch (InvocationTargetException e) {
                if (e.getCause() != null) {
                    e.getCause().printStackTrace();
                } else {
                    e.printStackTrace();
                }
                return false;
            }
            if (invoke != null && (invoke instanceof Boolean || invoke.getClass().equals(boolean.class))) {
                return ((Boolean) invoke);
            }
            //TODO: format output
            return true;
        }

        @Override
        public void getHelp() throws CommandRunFailure {
            Parameter[] params = method.getParameters();
            if (description != null && !"".equals(description)) {
                context.getOutput().output(description);
            }
            if (params.length == 0) {
                context.getOutput().output("(no options for this command)");
            }
            for (int i = 0; i < params.length; i++) {
                Class<?> type = params[i].getType();
                String paramName = getParameterName(params[i]);
                if (type.isAssignableFrom(CommandOutput.class)) {
                    continue;
                }

                String helpt = context.getInputParser().getHelp(name, type, paramName);

                context.getOutput().output(helpt);
            }
        }


    }

    private static String getParameterName(final Parameter param) {
        if (param.getAnnotation(Arg.class) != null) {
            Arg annotation = param.getAnnotation(Arg.class);
            if (!"".equals(annotation.value())) {
                return annotation.value();
            }
        }
        return param.getName();
    }
}
