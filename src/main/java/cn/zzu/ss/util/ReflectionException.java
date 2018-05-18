package cn.zzu.ss.util;

public class ReflectionException extends Exception {
    private ReflectionException(String message) {
        super(message);
    }

    public ReflectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReflectionException(Throwable cause) {
        super(cause);
    }

    public static ReflectionException unResolvedField(String fieldName, Class<?> fromClass) {
        return new ReflectionException(String
                .format("Couldn't found field %s in class %s", fieldName, fromClass.getCanonicalName()));
    }

    public static ReflectionException unResolvedMethod(String methodName, Class<?> fromClass) {
        return new ReflectionException(String
                .format("Couldn't found method %s(...) in class %s", methodName, fromClass.getCanonicalName()));
    }

    public static ReflectionException unResolvedDefaultConstructor(String constructorName, Class<?> fromClass) {
        return new ReflectionException(String
                .format("Couldn't found Constructor %s() in class %s", constructorName, fromClass.getCanonicalName()));
    }
}
