package io.army.util;

import io.army.dialect.DialectEnv;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

public abstract class ReflectionUtils {

    private ReflectionUtils() {
        throw new UnsupportedOperationException();
    }


    public static Method getStaticFactoryMethod(final String className, final Class<?> returnType, final String methodName,
                                                final Class<?>... paramTypeArray) {
        final Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            final Method method;
            method = clazz.getMethod(methodName, paramTypeArray);
            final int modifiers = method.getModifiers();
            if (!(Modifier.isPublic(modifiers)
                    && Modifier.isStatic(modifiers))) {
                final StringBuilder builder = new StringBuilder("Not found factory method ");
                appendMethodErrorInfo(builder, className, methodName, paramTypeArray);
                throw new IllegalArgumentException(builder.toString());
            }

            if (!returnType.isAssignableFrom(method.getReturnType())) {
                final StringBuilder builder = new StringBuilder("Return type ")
                        .append(returnType.getName())
                        .append(" and ");
                appendMethodErrorInfo(builder, className, methodName, paramTypeArray);
                builder.append(" not match.");
                throw new IllegalArgumentException(builder.toString());
            }
            return method;
        } catch (NoSuchMethodException e) {
            String m = String.format("Not found factory method,public static %s %s(%s) in class %s",
                    className, methodName, DialectEnv.class.getName(), className);
            throw new RuntimeException(m, e);
        }
    }

    public static Object invokeStaticFactoryMethod(Method method, Object... paramArray) {
        try {
            final Object result;
            result = method.invoke(null, paramArray);
            if (result == null) {
                String m = String.format("method %s return null", method);
                throw new NullPointerException(m);
            }
            return result;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object invokeStaticFactoryMethod(final String className, final Class<?> returnType, final String methodName) {
        return invokeStaticFactoryMethod(className, returnType, methodName, new Class<?>[0]);
    }

    public static Object invokeStaticFactoryMethod(final String className, final Class<?> returnType, final String methodName,
                                                   final Class<?>[] paramTypeArray, final Object... paramArray) {
        final Method method = getStaticFactoryMethod(className, returnType, methodName, paramTypeArray);
        return invokeStaticFactoryMethod(method, paramArray);
    }



    public static List<Class<?>> typeArgumentList(final Field field) {
        final Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType paramType)) {
            String m = String.format("%s.%s not a parameterized type", field.getDeclaringClass().getName(), field.getName());
            throw new IllegalArgumentException(m);
        }

        final Type[] actualTypeArguments = paramType.getActualTypeArguments();
        Type t;
        List<Class<?>> list = new ArrayList<>(actualTypeArguments.length);
        for (int i = 0; i < actualTypeArguments.length; i++) {
            t = actualTypeArguments[i];
            if (!(t instanceof Class<?>)) {
                continue;
            }
            list.add((Class<?>) t);
        }
        return list;
    }



    /*-------------------below private methods -------------------*/

    private static void appendMethodErrorInfo(final StringBuilder builder, final String className,
                                              final String methodName, final Class<?>... paramTypeArray) {
        builder.append("public static ")
                .append(methodName)
                .append('(');
        for (int i = 0; i < paramTypeArray.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(paramTypeArray[i].getName());
        }
        builder.append(") of class ")
                .append(className);
    }


}
