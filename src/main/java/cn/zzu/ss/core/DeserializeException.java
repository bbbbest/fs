package cn.zzu.ss.core;

public class DeserializeException extends RuntimeException {
    public DeserializeException(String message) {
        super(message);
    }

    public DeserializeException(Exception e) {
        super(e);
    }
}
