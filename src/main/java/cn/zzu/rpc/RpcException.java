package cn.zzu.rpc;

public class RpcException extends RuntimeException {

    public RpcException(final String message) {
        super(message);
    }

    public RpcException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public RpcException(final Throwable cause) {
        super(cause);
    }

    public static RpcException invalidRegistry() {
        return new RpcException("This registry is useless.");
    }

    public static RpcException invalidMagic() {
        return new RpcException("This magic number is useless.");
    }

    public static RpcException invalidServiceClass(Class<?> cl) {
        return new RpcException(String
                .format("Service should to be interface, this class %s is not a interface", cl.getCanonicalName()));
    }

    public static RpcException noSuchService(String service) {
        return new RpcException(String
                .format("No such service: ", service));
    }

    public static RpcException invokeFailed(String reason) {
        return new RpcException(String
                .format("Service invoked failed, because of: ", reason));
    }
}
