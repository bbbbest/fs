package cn.zzu.rpc.netty;

public class InvalidPacketException extends Exception {
    public InvalidPacketException(final String message) {
        super(message);
    }

    public InvalidPacketException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public InvalidPacketException(final Throwable cause) {
        super(cause);
    }
}
