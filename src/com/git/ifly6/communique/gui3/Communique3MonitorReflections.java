/*
 * Copyright (c) 2020 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this class file and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.git.ifly6.communique.gui3;

import com.git.ifly6.nsapi.ctelegram.monitors.CommMonitor;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Methods to instantiate {@link CommMonitor} with reflection.
 * @since version 3.0 (build 13)
 */
public class Communique3MonitorReflections {

    private Communique3MonitorReflections() { }

    /**
     * Gets names of the declared monitors
     * @return names of the monitors, without prefixes
     */
    public static List<String> getMonitorStrings() {
        return getMonitors().stream()
                .map(Class::toString)
                .map(s -> lastElement(s.split("\\.")))
                .collect(Collectors.toList());
    }

    /**
     * Lists monitors in the monitors package that are non-abstract concrete implementations.
     * @return list of monitors as classes
     */
    public static List<Class<? extends CommMonitor>> getMonitors() {
        Reflections reflections = new Reflections("com.git.ifly6.nsapi.ctelegram.monitors.reflected");
        return reflections.getSubTypesOf(CommMonitor.class).stream()
                .filter(c -> !c.isInterface())
                .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                .collect(Collectors.toList());
    }

    /**
     * Searches for monitor by name. Ignores case.
     * @param monitorName name to search for
     * @return class of corresponding monitor
     */
    public static Class<? extends CommMonitor> getMonitor(String monitorName) {
        return getMonitors().stream()
                .filter(c -> lastElement(c.toString().split("\\.")).equalsIgnoreCase(monitorName))
                .findFirst()
                .orElseThrow(NoClassDefFoundError::new);
    }

    /**
     * Filters constructors for whether they take only string parameters.
     * @param constructors to filter
     * @param allString    if true returns ones where all parameters are {@link String}; if false, otherwise
     * @return matching constructors
     */
    @SuppressWarnings("SimplifiableConditionalExpression")
    public static List<Constructor<?>> filterConstructors(List<Constructor<?>> constructors, boolean allString) {
        return constructors.stream()
                .filter(constructor -> {
                    Class<?>[] classes = constructor.getParameterTypes();
                    boolean allAreStrings = Arrays.stream(classes).allMatch(c -> c.equals(String.class));
                    return allString
                            ? allAreStrings
                            : !allAreStrings;
                })
                .collect(Collectors.toList());
    }

    /** @see #filterConstructors(List, boolean) */
    public static List<Constructor<?>> filterConstructors(Constructor<?>[] constructors, boolean allString) {
        return filterConstructors(Arrays.asList(constructors), allString);
    }

    /**
     * Returns a list of parameter names in form {@code blah$String}.
     * @param monitorName to search for
     * @return list of list of <b>strings</b> corresponding to type names of parameters; in singleton cases, because
     * there are no parameters, returns empty list.
     */
    public static List<List<String>> getInstantiationParameters(String monitorName) {
        Class<? extends CommMonitor> monitorClass = getMonitor(monitorName);
        if (isSingleton(monitorClass))
            return new ArrayList<>();

        List<List<String>> typeList = new ArrayList<>();
        Constructor<?>[] constructors = Arrays.stream(monitorClass.getDeclaredConstructors())
                .filter(c -> Modifier.isPublic(c.getModifiers()))
                .toArray(Constructor[]::new);
        for (Constructor<?> constructor : constructors) {
            List<String> typeNameList = new ArrayList<>();
            Parameter[] params = constructor.getParameters();
            Class<?>[] types = constructor.getParameterTypes();
            for (int i = 0; i < params.length; i++)
                typeNameList.add(String.format("%s$%s", params[i].getName(), types[i].getTypeName()));

            typeList.add(typeNameList);
        }
        return typeList;
    }

    /**
     * Deterimines whether class is a singleton.
     * @param monitorClass to determine
     * @return true if singleton
     */
    private static boolean isSingleton(Class<? extends CommMonitor> monitorClass) {
        return Arrays.stream(monitorClass.getDeclaredMethods())
                .map(Method::getName)
                .anyMatch(s -> s.equalsIgnoreCase("getinstance"));
    }

    /**
     * Instantiates a monitor from provided monitor name and arguments.
     * @param monitorName to construct
     * @param args        to provide to constructor
     * @return instantiated CommMonitor
     * @throws CommReflectInstantiationException if there is no constructor to call
     * @throws CommReflectException              if error occurred in construction
     */
    public static CommMonitor instantiate(String monitorName, Object[] args)
            throws CommReflectInstantiationException {
        Class<? extends CommMonitor> monitorClass = getMonitor(monitorName);
        if (isSingleton(monitorClass))
            try {
                return (CommMonitor) monitorClass.getDeclaredMethod("getInstance").invoke(null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new CommReflectException("Error in invoking singleton constructor method!", e);
            } catch (NoSuchMethodException | IllegalArgumentException e) {
                throw new CommReflectInstantiationException("Singleton did not have invokable getInstance method?", e);
            }

        try {
            return (CommMonitor) monitorClass
                    .getDeclaredMethod("create")
                    .invoke(null, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new CommReflectException(String.format("Error invoking %s make method", monitorName));
        } catch (NoSuchMethodException | IllegalArgumentException e) {
            throw new CommReflectInstantiationException("No matching make factory method");
        }
    }

    /**
     * Returns last element of array.
     * @param array to read
     * @param <T>   type of array
     * @return last element
     */
    private static <T> T lastElement(T[] array) {
        return array[array.length - 1];
    }

    /** Thown when there is no create method or the wrong number of arguments are provided. */
    private static class CommReflectInstantiationException extends Exception {
        public CommReflectInstantiationException(String message) { super(message); }

        public CommReflectInstantiationException(String message, Throwable cause) { super(message, cause); }
    }

    /** Thrown when some some other reflection error occurs. */
    private static class CommReflectException extends RuntimeException {
        public CommReflectException(String message) { super(message); }

        public CommReflectException(String message, Throwable e) { super(message, e); }
    }


}
