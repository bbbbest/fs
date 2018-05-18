package cn.zzu.ss.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author fzh
 * @since 2018/4/12
 */
public final class UnsafeUtil {

    private static Unsafe unsafe;

    public static Unsafe getUnsafe() {
        if (unsafe == null) {
            synchronized (UnsafeUtil.class) {
                if (unsafe == null) {
                    try {
                        Class<Unsafe> unsafeClass = Unsafe.class;
                        Field[] fields = unsafeClass.getDeclaredFields();
                        for (Field field : fields) {
                            field.setAccessible(true);
                            Object o = field.get(null);
                            if (unsafeClass.isInstance(o)) {
                                unsafe = unsafeClass.cast(o);
                            }
                        }
                    } catch (IllegalAccessException ignored) {
                    }
                }
            }
        }
        return unsafe;
    }
}
