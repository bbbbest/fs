package cn.zzu.ss.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TypeUtil {
    public static final Map<Class, String> CLASS_TO_SIGN;
    public static final Map<String, Class> SIGN_TO_CLASS;
    private static final Class[] PRIMITIVE = {Byte.TYPE, Boolean.TYPE, Character.TYPE, Short.TYPE, Integer.TYPE, Float.TYPE, Long.TYPE, Double.TYPE, Void.TYPE};
    private static final Class[] REF_PRIMITIVE = {Byte.class, Boolean.class, Character.class, Short.class, Integer.class, Float.class, Long.class, Double.class, Void.class};

    static {
        CLASS_TO_SIGN = new ConcurrentHashMap<>();
        SIGN_TO_CLASS = new ConcurrentHashMap<>();

        for (Class cl : PRIMITIVE) {
            String sign = getClassSignature(cl);
            CLASS_TO_SIGN.put(cl, sign);
            SIGN_TO_CLASS.put(sign, cl);
        }

        for (Class cl : REF_PRIMITIVE) {
            String sign = getClassSignature(cl);
            CLASS_TO_SIGN.put(cl, sign);
            SIGN_TO_CLASS.put(sign, cl);
        }
        CLASS_TO_SIGN.put(String.class, "T");
        SIGN_TO_CLASS.put("T", String.class);
    }

    public static String getPrimSign(Class cl) {
        return CLASS_TO_SIGN.get(cl);
    }

    public static Class getSignClass(String sign) {
        return SIGN_TO_CLASS.get(sign);
    }

    public static boolean isArraySign(String sign) {
        return sign != null && sign.length() > 0 && sign.charAt(0) == '[';
    }


    public static boolean isPrimitive(Class<?> cl) {
        for (Class cls : PRIMITIVE) {
            if (cl == cls) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRefPrimitive(Class<?> cl) {
        for (Class cls : REF_PRIMITIVE) {
            if (cl == cls) {
                return true;
            }
        }
        return false;
    }

    public static String getClassSignature(Class<?> cl) {
        StringBuilder sb = new StringBuilder();
        while (cl.isArray()) {
            sb.append('[');
            cl = cl.getComponentType();
        }
        // upper character represent reference type
        if (cl.isPrimitive()) {
            if (cl == Integer.TYPE) {
                sb.append('i');
            } else if (cl == Byte.TYPE) {
                sb.append('b');
            } else if (cl == Long.TYPE) {
                sb.append('j');
            } else if (cl == Float.TYPE) {
                sb.append('f');
            } else if (cl == Double.TYPE) {
                sb.append('d');
            } else if (cl == Short.TYPE) {
                sb.append('s');
            } else if (cl == Character.TYPE) {
                sb.append('c');
            } else if (cl == Boolean.TYPE) {
                sb.append('z');
            } else if (cl == Void.TYPE) {
                sb.append('v');
            } else {
                throw new InternalError();
            }
        } else if (cl == Integer.class) {
            sb.append('I');
        } else if (cl == Byte.class) {
            sb.append('B');
        } else if (cl == Long.class) {
            sb.append('J');
        } else if (cl == Float.class) {
            sb.append('F');
        } else if (cl == Double.class) {
            sb.append('D');
        } else if (cl == Short.class) {
            sb.append('S');
        } else if (cl == Character.class) {
            sb.append('C');
        } else if (cl == Boolean.class) {
            sb.append('Z');
        } else if (cl == Void.class) {
            sb.append('V');
        } else if (String.class == cl) {
            sb.append('T');
        } else {
            sb.append('L').append(cl.getName().replace('.', '/')).append(';');
        }
        return sb.toString();
    }
}
