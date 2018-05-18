package cn.zzu.rpc;

import cn.zzu.rpc.netty.HeartbeatServerHandler;
import cn.zzu.rpc.netty.InvocationServerHandler;
import cn.zzu.rpc.netty.PacketDecoder;
import cn.zzu.rpc.netty.PacketEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcServer {
    private static final Logger logger = LoggerFactory.getLogger("RpcServer");
    private RpcRegistry registry;
    private ConcurrentHashMap<String, Invoker<?>> cachedInvoker;

    private RpcServer(final RpcRegistry registry) {
        this.registry = registry;
        this.cachedInvoker = new ConcurrentHashMap<>();
    }

    public static RpcServer create(RpcRegistry registry) {
        return new RpcServer(registry);
    }

    public void start() throws InterruptedException {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 100)
                    .option(ChannelOption.SO_KEEPALIVE, true).childHandler(new RpcServerInitializer(cachedInvoker));


            ChannelFuture future = bootstrap.bind(registry.getHost(), registry.getPort()).sync();
            future.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public <T> void export(Class<T> cl, T service) {
        export(cl.getSimpleName(), cl, service, allPublicMethodName(cl));
    }

    public <T> void export(String identifier, Class<T> cl, T service) {
        export(identifier, cl, service, allPublicMethodName(cl));
    }

    public <T> void export(Class<T> cl, T service, String[] methods) {
        export(cl.getSimpleName(), cl, service, methods);
    }

    public <T> void export(String identifier, Class<T> cl, T service, String[] methods) {
        Invoker<T> invoker = new Invoker<>(identifier, cl, service, methods);
        cachedInvoker.put(identifier, invoker);
    }

    private String[] allPublicMethodName(Class<?> cl) {
        Method[] methods = cl.getMethods();
        String[] mNames = new String[methods.length];
        for (int i = 0; i < methods.length; i++) {
            mNames[i] = methods[i].getName();
        }
        return mNames;
    }

    private class RpcServerInitializer extends ChannelInitializer<SocketChannel> {

        private Map<String, Invoker<?>> cachedInvoker;

        public RpcServerInitializer(final Map<String, Invoker<?>> cachedInvoker) {
            this.cachedInvoker = cachedInvoker;
        }

        @Override
        protected void initChannel(final SocketChannel ch) {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                    .addLast("frameEncoder", new LengthFieldPrepender(4)).addLast("packetDecoder", new PacketDecoder())
                    .addLast("packetEncoder", new PacketEncoder())
                    .addLast("heartbeatRespHandler", new HeartbeatServerHandler())
                    .addLast("invocationRespHandler", new InvocationServerHandler(cachedInvoker));
        }
    }
}
