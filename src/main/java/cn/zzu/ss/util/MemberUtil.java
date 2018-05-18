package cn.zzu.ss.util;

import sun.reflect.ReflectionFactory;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;

public final class MemberUtil {

    /** reflection factory for obtaining serialization constructors */
    private static final ReflectionFactory reflFactory = AccessController
            .doPrivileged(new ReflectionFactory.GetReflectionFactoryAction());

    public static Field getField(String name, Class<?> fromClass, boolean accessible) throws ReflectionException {
        Field f;
        try {
            f = fromClass.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw ReflectionException.unResolvedField(name, fromClass);
        }
        if (accessible) {
            f.setAccessible(true);
        }
        return f;
    }

    public static Field[] getFields(Class<?> fromClass, boolean accessible) {
        Field[] fields = fromClass.getDeclaredFields();
        if (accessible) {
            for (Field f : fields) {
                f.setAccessible(true);
            }
        }
        return fields;
    }

    public static Method getMethod(String name, Class<?> fromClass, boolean accessible, Class<?>... parameterTypes) throws ReflectionException {
        Method method;
        try {
            method = fromClass.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw ReflectionException.unResolvedMethod(name, fromClass);
        }
        if (accessible) {
            method.setAccessible(true);
        }
        return method;
    }

    public static Method[] getMethods(Class<?> fromClass, boolean accessible) {
        Method[] methods = fromClass.getDeclaredMethods();
        if (accessible) {
            for (Method m : methods) {
                m.setAccessible(true);
            }
        }

        return methods;
    }


    public static String getPackageName(Class<?> cl) {
        String s = cl.getName();
        int i = s.lastIndexOf('[');
        if (i >= 0) {
            s = s.substring(i + 2);
        }
        i = s.lastIndexOf('.');
        return (i >= 0) ? s.substring(0, i) : "";
    }

    public static Method getInheritableMethod(Class<?> cl, String name, Class<?>[] argTypes, Class<?> returnType) {
        Method meth = null;
        Class<?> defCl = cl;
        while (defCl != null) {
            try {
                meth = defCl.getDeclaredMethod(name, argTypes);
                break;
            } catch (NoSuchMethodException ex) {
                defCl = defCl.getSuperclass();
            }
        }

        if ((meth == null) || (meth.getReturnType() != returnType)) {
            return null;
        }
        meth.setAccessible(true);
        int mods = meth.getModifiers();
        if ((mods & (Modifier.STATIC | Modifier.ABSTRACT)) != 0) {
            return null;
        } else if ((mods & (Modifier.PUBLIC | Modifier.PROTECTED)) != 0) {
            return meth;
        } else if ((mods & Modifier.PRIVATE) != 0) {
            return (cl == defCl) ? meth : null;
        } else {
            return packageEquals(cl, defCl) ? meth : null;
        }
    }

    public static boolean packageEquals(Class<?> cl1, Class<?> cl2) {
        return (cl1.getClassLoader() == cl2.getClassLoader() && getPackageName(cl1).equals(getPackageName(cl2)));
    }

    public static Method getPrivateMethod(Class<?> cl, String name, Class<?>[] argTypes, Class<?> returnType) {
        try {
            Method meth = cl.getDeclaredMethod(name, argTypes);
            meth.setAccessible(true);
            int mods = meth.getModifiers();
            return ((meth
                    .getReturnType() == returnType) && ((mods & Modifier.STATIC) == 0) && ((mods & Modifier.PRIVATE) != 0)) ? meth : null;
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    public static Constructor<?> getSerializableConstructor(Class<?> cl) {
        Class<?> initCl = cl;
        while (Serializable.class.isAssignableFrom(initCl)) {
            if ((initCl = initCl.getSuperclass()) == null) {
                return null;
            }
        }
        try {
            Constructor<?> cons = initCl.getDeclaredConstructor((Class<?>[]) null);
            int mods = cons.getModifiers();
            if ((mods & Modifier.PRIVATE) != 0 || ((mods & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0 && !packageEquals(cl, initCl))) {
                return null;
            }
            cons = reflFactory.newConstructorForSerialization(cl, cons);
            cons.setAccessible(true);
            return cons;
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    public static long getSerialVersionID(Class cl) {
        long svid = 1L;
        try {
            Field field = cl.getDeclaredField("serialVersionUID");
            field.setAccessible(true);
            int q = Modifier.STATIC | Modifier.FINAL;
            if ((field.getModifiers() & q) == q) {
                svid = (long) field.get(null);
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        return svid;
    }

}
