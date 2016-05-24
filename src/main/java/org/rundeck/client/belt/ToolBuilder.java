package org.rundeck.client.belt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.rundeck.client.tool.App.tail;

/**
 * Created by greg on 5/23/16.
 */
public class ToolBuilder {
    CommandSet commands;
    CommandInput inputParser;

    public static ToolBuilder builder() {
        return new ToolBuilder();
    }

    public ToolBuilder() {
        commands = new CommandSet();
    }

    public ToolBuilder addCommands(final Object... instance) {
        for (Object o : instance) {
            addCommand(o);
        }
        return this;
    }
    public ToolBuilder addCommand(final Object instance) {
        introspect(instance);
        return this;
    }

    static class CommandSet implements Tool, CommandInvoker {
        Map<String, CommandInvoker> commands;
        String defCommand;
        private CommandInput inputParser;

        public CommandSet() {
            commands = new HashMap<>();
        }


        @Override
        public boolean run(final String[] args) throws CommandRunFailure {
            String[] cmdArgs = args;
            String cmd = defCommand;
            if (args.length > 0) {
                cmd = args[0];
                cmdArgs = tail(args);
            }
            if (null == cmd) {
                throw new CommandRunFailure(String.format(
                        "No command was specified. Available commands: %s",
                        commands.keySet()
                ));
            }
            if ("-h".equals(cmd)) {
                for (int i = 0; i < getHelp().length; i++) {
                    String s = getHelp()[i];
                    System.out.println(s);

                }
                return false;
            }
            return runCommand(cmd, cmdArgs);
        }

        @Override
        public String[] getHelp() throws CommandRunFailure {
            ArrayList<String> strings = new ArrayList<>();
            strings.add(
                    String.format(
                            "Available commands: %s",
                            commands.keySet()
                    )
            );
            for (String command : commands.keySet()) {
                CommandInvoker commandInvoker = commands.get(command);
                commandInvoker.setInputParser(inputParser);

                strings.add("--------------------");
                strings.add("Command: " + command);
                strings.addAll(Arrays.asList(commandInvoker.getHelp()));
            }
            return strings.toArray(new String[strings.size()]);
        }

        public boolean runCommand(String cmd, String[] args) throws CommandRunFailure {
            CommandInvoker commandInvoke = commands.get(cmd);
            if (null == commandInvoke) {
                throw new CommandRunFailure(String.format(
                        "No such command: %s. Available commands: %s",
                        cmd,
                        commands.keySet()
                ));
            }
            commandInvoke.setInputParser(inputParser);
            return commandInvoke.run(args);
        }

        @Override
        public void setInputParser(CommandInput inputParser) {
            this.inputParser = inputParser;
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
                CommandInvoke value = new CommandInvoke(name, method, instance);
                subCommands.put(name, value);
                if (annotation.isDefault()) {
                    defInvoke = name;
                }
            }
        }

        CommandSet commandSet = new CommandSet();
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
        if(commands.commands.size()==1){
            commands.defCommand = commands.commands.keySet().iterator().next();
        }
        return new Tool() {
            @Override
            public boolean run(final String[] args) throws CommandRunFailure {
                return commands.run(args);
            }

            @Override
            public boolean runCommand(final String name, final String[] args) throws CommandRunFailure {
                return commands.runCommand(name, args);
            }
        };
    }

    private static interface CommandInvoker {
        boolean run(String[] args) throws CommandRunFailure;

        String[] getHelp() throws CommandRunFailure;

        public void setInputParser(CommandInput inputParser);
    }

    private static class CommandInvoke implements CommandInvoker {
        String name;
        Method method;
        Object instance;
        private CommandInput inputParser;

        public CommandInvoke(final String name, final Method method, final Object instance) {
            this.name = name;
            this.method = method;
            this.instance = instance;
        }


        public boolean run(String[] args) {
            //get configured arguments to the method
            Parameter[] parameters = method.getParameters();
            Object[] objArgs = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                Class<?> type = parameter.getType();

                Object t = inputParser.parseArgs(args, type);
                objArgs[i] = t;
            }
            try {
                Object invoke = method.invoke(instance, objArgs);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        public String[] getHelp() throws CommandRunFailure {
            Parameter[] parameters = method.getParameters();

            if (parameters.length == 0) {
                return new String[]{"(no options for this command)"};
            }
            String[] help = new String[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                Class<?> type = parameter.getType();

                String helpt = inputParser.getHelp(type);
                help[i] = helpt;
            }
            return help;
        }


        public void setInputParser(CommandInput inputParser) {
            this.inputParser = inputParser;
        }
    }
}
