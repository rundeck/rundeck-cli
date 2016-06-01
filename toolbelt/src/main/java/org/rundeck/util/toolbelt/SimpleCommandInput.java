package org.rundeck.util.toolbelt;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * An input parameter parser with simple heuristics, for a method parameter named "abc" it looks
 * for an argument "--abc" followed by a value. For a method parameter named "a" (one character) it looks
 * for an argument "-a" followed by a value. If the parameter is a String, it is passed as-is. If it is a Boolean,
 * then no value is expected, and the argument flag indicates true, lack of argument flag indicates false.
 * If the parameter is a number (Integer, Long, Double, Float), it is parsed as such.
 */
public class SimpleCommandInput implements CommandInput {
    @Override
    public <T> T parseArgs(
            final String command,
            final String[] args,
            final Class<? extends T> clazz,
            final String paramName
    )
            throws InputError
    {
        if (primitiveType(clazz)) {
            return parseSimple(args, clazz, paramName);
        }
        //TODO: else look for fields/setters of the class, then parse all input values
//        for (String arg : args) {
//
//        }
        return null;
    }

    private <T> T parseSimple(final String[] args, final Class<? extends T> clazz, final String paramName) {
        String value = parseForKey(args, paramName, isBooleanType(clazz));
        return parseSimple(value, clazz);
    }

    private <T> boolean isBooleanType(final Class<? extends T> clazz) {
        return clazz.equals(Boolean.class) || clazz.equals(boolean.class);
    }

    /**
     * Look for a --param argument (if paramname is more than one character) or -x (if one character), followed by a
     * value not starting with "-" (if not boolean). If it is boolean, return "true" or "false" depending on existence
     * of the parameter
     *
     * @param args      input arguments
     * @param paramName parameter name
     * @param isboolean
     *
     * @return
     */
    private String parseForKey(final String[] args, final String paramName, final boolean isboolean) {
        boolean keyseen = false;
        String match = argForParam(paramName);
        for (String arg : args) {
            if (keyseen && !arg.startsWith("-")) {
                return arg;
            } else if (!keyseen && match.equals(arg)) {
                keyseen = true;
                if (isboolean) {
                    return "true";
                }
            }
        }
        return isboolean ? "false" : null;
    }

    @SuppressWarnings("unchecked")
    private <T> T parseSimple(final String value, final Class<? extends T> clazz) {
        if (clazz.equals(Boolean.class) || clazz.equals(boolean.class)) {
            return (T) Boolean.valueOf(value);
        }
        try {
            if (clazz.equals(Integer.class) || clazz.equals(int.class)) {
                return (T) new Integer(value);
            } else if (clazz.equals(Long.class) || clazz.equals(long.class)) {
                return (T) new Long(value);
            } else if (clazz.equals(String.class)) {
                return (T) value;
            } else if (clazz.equals(Float.class) || clazz.equals(float.class)) {
                return (T) new Float(value);
            } else if (clazz.equals(Double.class) || clazz.equals(double.class)) {
                return (T) new Double(value);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format(
                    "Could not parse into a %s: %s",
                    clazz.getSimpleName(),
                    value
            ), e);
        }
        return null;
    }

    /**
     * @param clazz
     * @param <T>
     *
     * @return true if the class is a simple type
     */
    private <T> boolean primitiveType(final Class<? extends T> clazz) {
        Set<Class> basicTypes = new HashSet<>(
                Arrays.asList(
                        String.class,
                        Integer.class,
                        int.class,
                        Long.class,
                        long.class,
                        Boolean.class,
                        boolean.class,
                        Float.class,
                        float.class,
                        Double.class,
                        double.class
                )
        );
        return basicTypes.contains(clazz);
    }

    @Override
    public String getHelp(final String command, final Class<?> type, final String paramName) {

        if (primitiveType(type)) {
            return getPrimitiveHelp(type, paramName);
        }
        return null;
    }

    private String getPrimitiveHelp(final Class<?> type, final String paramName) {
        String match = argForParam(paramName);
        boolean isboolean = isBooleanType(type);
        return match + (!isboolean ? String.format(" <%s>", type.getSimpleName()) : "");
    }

    private String argForParam(final String paramName) {
        return paramName.length() > 1 ? "--" + paramName.toLowerCase() : "-" + paramName;
    }
}
