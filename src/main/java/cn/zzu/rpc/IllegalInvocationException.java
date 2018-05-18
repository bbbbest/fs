package cn.zzu.rpc;

public class IllegalInvocationException extends RuntimeException {
    public IllegalInvocationException(final String message) {
        super(message);
    }

    public IllegalInvocationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public IllegalInvocationException(final Throwable cause) {
        super(cause);
    }
}
