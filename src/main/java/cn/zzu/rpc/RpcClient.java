package cn.zzu.rpc;

import cn.zzu.rpc.netty.HeartbeatClientHandler;
import cn.zzu.rpc.netty.InvocationClientHandler;
import cn.zzu.rpc.netty.PacketDecoder;
import cn.zzu.rpc.netty.PacketEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RpcClient {
    private static final Logger logger = LoggerFactory.getLogger("RpcClient");
    private static final int DEFAULT_RECONNECT_DELAY = 5;
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private EventLoopGroup group = new NioEventLoopGroup();
    private RpcRegistry registry;
    private ConcurrentHashMap<Class, Object> cached = new ConcurrentHashMap<>();
    private int reconnectDelay = 5;
    private SocketChannel channel;

    private RpcClient(final RpcRegistry registry) {
        this.registry = registry;
    }

    public static RpcClient create(final RpcRegistry registry) {
        return new RpcClient(registry);
    }

    public void connect() {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, registry.getTimeout())
                    .option(ChannelOption.SO_KEEPALIVE, true).handler(new RpcClientInitializer());


            ChannelFuture future = bootstrap.connect(new InetSocketAddress(registry.getHost(), registry.getPort()))
                    .sync();

            if (reconnectDelay != DEFAULT_RECONNECT_DELAY) {
                reconnectDelay = DEFAULT_RECONNECT_DELAY;
            }

            if (future.isCancelled()) {
                if (logger.isInfoEnabled()) {
                    logger.info("connect canceled");
                }
            } else if (!future.isSuccess()) {
                if (logger.isErrorEnabled()) {
                    logger.error("error happened when connect to server {}/{}", registry.getHost(), registry.getPort());
                }
            }
            channel = (SocketChannel) future.channel();
            channel.closeFuture();
        } catch (Exception e) {
            if (e instanceof ConnectException) {
                if (reconnectDelay < 600) {
                    reconnectDelay *= 2;
                }
            }
            logger.error("server doesn't start, reconnect after {} seconds", reconnectDelay);
            executor.execute(()->{
                try {
                    TimeUnit.SECONDS.sleep(reconnectDelay);
                    connect();
                } catch (InterruptedException ie) {
                    logger.error("reconnect thread died.");
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T referTo(Class<T> cl) {
        return referTo(cl.getSimpleName(), cl);
    }

    @SuppressWarnings("unchecked")
    public <T> T referTo(String identifier, Class<T> cl) {
        if (!cl.isInterface()) {
            throw RpcException.invalidServiceClass(cl);
        }
        return (T) Proxy.newProxyInstance(cl
                .getClassLoader(), new Class[]{cl}, new DefaultInvocationHandler(identifier, cl, channel));
    }

    private class RpcClientInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(final SocketChannel ch) {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                    .addLast("frameEncoder", new LengthFieldPrepender(4)).addLast("packetDecoder", new PacketDecoder())
                    .addLast("packetEncoder", new PacketEncoder()).addLast(new ReadTimeoutHandler(50))
                    .addLast("heartbeatReqHandler", new HeartbeatClientHandler())
                    .addLast("invocationReqHandler", new InvocationClientHandler());
        }
    }
}
