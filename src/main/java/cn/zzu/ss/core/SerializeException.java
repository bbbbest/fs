package cn.zzu.ss.core;

public class SerializeException extends RuntimeException {
    public SerializeException(String message) {
        super(message);
    }

    public SerializeException(final Throwable cause) {
        super(cause);
    }
}
