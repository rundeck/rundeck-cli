package org.rundeck.client.belt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

import static org.rundeck.client.tool.App.tail;

/**
 * Created by greg on 5/23/16.
 */
public class ToolBuilder {
    CommandSet commands;
    CommandInput inputParser;
    Set<String> helpCommands;
    CommandOutput commandOutput;
    OutputFormatter formatter;

    public static ToolBuilder builder() {
        return new ToolBuilder();
    }

    public ToolBuilder() {
        commands = new CommandSet();
        helpCommands = new HashSet<>();
        formatter = new SameFormatter();
    }

    public ToolBuilder addCommands(final Object... instance) {
        for (Object o : instance) {
            addCommand(o);
        }
        return this;
    }

    /**
     * Use "-h","help","?" as help commands
     *
     * @return
     */
    public ToolBuilder defaultHelp() {
        return helpCommands("-h", "--help", "help", "?");
    }

    public ToolBuilder helpCommands(String... commands) {
        helpCommands.addAll(Arrays.asList(commands));
        return this;
    }

    public ToolBuilder addCommand(final Object instance) {
        introspect(instance);
        return this;
    }

    public ToolBuilder systemOutput() {
        commandOutput = new BaseSystemCommandOutput();
        return this;
    }

    static class CommandSet implements Tool, CommandInvoker {
        Map<String, CommandInvoker> commands;
        String defCommand;
        private CommandInput inputParser;
        Set<String> helpCommands;
        CommandOutput output;
        public String description;

        public CommandSet() {
            commands = new HashMap<>();
            helpCommands = new HashSet<>();
        }


        @Override
        public boolean runMain(final String[] args, final boolean exitSystem) throws CommandRunFailure {
            boolean result = run(inputParser, output, args);
            if (!result && exitSystem) {
                System.exit(2);
            }
            return result;
        }

        @Override
        public boolean run(CommandInput inputParser, CommandOutput output, final String[] args)
                throws CommandRunFailure
        {
            String[] cmdArgs = args;
            String cmd = defCommand;
            if (args.length > 0 && !(args[0].startsWith("-") && null != defCommand)) {
                cmd = args[0];
                cmdArgs = tail(args);
            }
            if (null == cmd) {
                output.error(String.format(
                        "No command was specified. Available commands: %s",
                        commands.keySet()
                ));
                output.error(String.format("You can use: COMMAND %s to get help.", helpCommands));
                return false;
            }
            if (helpCommands.contains(cmd)) {
                getHelp(inputParser, output);
                return false;
            }
            return runCommand(cmd, cmdArgs, inputParser, output);
        }

        @Override
        public void getHelp(CommandInput inputParser, CommandOutput output) throws CommandRunFailure {
            if (description != null && !"".equals(description)) {
                output.output(description);
            }
            boolean multi = commands.size() > 1;
            if (multi) {
                output.output(
                        String.format(
                                "Available commands: %s",
                                commands.keySet()
                        )
                );
            }
            for (String command : commands.keySet()) {
                CommandInvoker commandInvoker = commands.get(command);

                if (multi) {
                    output.output("--------------------");
                    output.output("+ Command: " + command);
                }
                commandInvoker.getHelp(inputParser, output);
            }
        }

        public boolean runCommand(String cmd, String[] args, CommandInput inputParser, CommandOutput output)
                throws CommandRunFailure
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
                commandInvoke.getHelp(inputParser, output);
                return false;
            }
            try {
                return commandInvoke.run(inputParser, output, args);
            } catch (InputError inputError) {
                output.error(String.format(
                        "Error parsing arguments for [%s]: %s",
                        cmd,
                        inputError.getMessage()
                ));
                output.error(String.format("You can use: \"%s %s\" to get help.", cmd, helpCommands.iterator().next()));
                return false;
            }
        }

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

        Method[] methods = aClass.getMethods();
        String defInvoke = null;
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            Command annotation = method.getAnnotation(Command.class);
            if (annotation != null) {
                String name = annotation.value();
                if ("".equals(name)) {
                    name = method.getName();
                }
                String description = annotation.description();
                CommandInvoke value = new CommandInvoke(name, method, instance);
                value.description = description;
                value.solo = annotation.isSolo();
                subCommands.put(name, value);
                if (annotation.isDefault()) {
                    defInvoke = name;
                }
            }
        }

        CommandSet commandSet = new CommandSet();
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

    public ToolBuilder setParser(CommandInput input) {
        this.inputParser = input;
        return this;
    }


    public Tool build() {
        commands.inputParser = inputParser;
        commands.helpCommands = helpCommands;
        if (commands.commands.size() == 1) {
            commands.defCommand = commands.commands.keySet().iterator().next();
        }
        commands.output = new FormattedOutput(commandOutput, formatter);
        return commands;
    }

    private static interface CommandInvoker {
        boolean run(CommandInput inputParser, CommandOutput output, String[] args) throws CommandRunFailure, InputError;

        void getHelp(CommandInput inputParser, CommandOutput output) throws CommandRunFailure;
    }

    private static class CommandInvoke implements CommandInvoker {
        String name;
        Method method;
        Object instance;
        public String description;
        public boolean solo;

        public CommandInvoke(final String name, final Method method, final Object instance) {
            this.name = name;
            this.method = method;
            this.instance = instance;
        }


        public boolean run(CommandInput inputParser, CommandOutput output, String[] args)
                throws CommandRunFailure, InputError
        {
            //get configured arguments to the method
            Parameter[] parameters = method.getParameters();
            Object[] objArgs = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                Class<?> type = parameter.getType();

                if (type.isAssignableFrom(CommandOutput.class)) {
                    objArgs[i] = output;
                } else {
                    Object t = inputParser.parseArgs(args, type);

                    objArgs[i] = t;
                }
            }
            Object invoke=null;
            try {
                invoke = method.invoke(instance, objArgs);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            if (invoke != null && invoke instanceof Boolean) {
                return ((Boolean) invoke);
            }
            //TODO: format output
            return true;
        }

        @Override
        public void getHelp(CommandInput inputParser, CommandOutput output) throws CommandRunFailure {
            Parameter[] parameters = method.getParameters();

            if (description != null && !"".equals(description)) {
                output.output(description);
            }
            if (parameters.length == 0) {
                output.output("(no options for this command)");
            }
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                Class<?> type = parameter.getType();
                if (type.isAssignableFrom(CommandOutput.class)) {
                    continue;
                }

                String helpt = inputParser.getHelp(type);

                output.output(helpt);
            }
        }


    }
}
