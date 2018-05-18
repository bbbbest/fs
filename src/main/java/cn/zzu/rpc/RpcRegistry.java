package cn.zzu.rpc;

public class RpcRegistry {
    private static final int DEFAULT_TIMEOUT = 60 * 1000; // 60s
    private String host;
    private int port;
    private int timeout;

    public RpcRegistry(final String host, final int port) {
        this(host, port, DEFAULT_TIMEOUT);
    }

    public RpcRegistry(final String host, final int port, final int timeout) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getTimeout() {
        return timeout;
    }

}
