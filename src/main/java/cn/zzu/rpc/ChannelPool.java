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
import io.netty.util.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class ChannelPool {
    private static final Logger logger = LoggerFactory.getLogger("ChannelPoolLogger");
    private static final int MAX_POOL_SIZE = 6;
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private EventLoopGroup group = new NioEventLoopGroup();
    private Channel[] channels;
    private Object[] locks;
    private RpcRegistry registry;

    public ChannelPool(final RpcRegistry registry) {
        this.registry = registry;
        this.channels = new Channel[MAX_POOL_SIZE];
        this.locks = new Object[MAX_POOL_SIZE];
        for (int i = 0; i < MAX_POOL_SIZE; i++) {
            locks[i] = new Object();
        }
    }

    /**
     * 同步获取netty channel
     */
    public Channel syncGetChannel() throws InterruptedException {
        int index = new Random().nextInt(MAX_POOL_SIZE);
        Channel channel = channels[index];
        if (channel != null && channel.isActive()) {
            return channel;
        }

        synchronized (locks[index]) {
            channel = channels[index];
            if (channel != null && channel.isActive()) {
                return channel;
            }
            // 相当于建立了重连机制
            channel = connectToServer();
            channels[index] = channel;
        }

        return channel;
    }

    private Channel connectToServer() throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, registry.getTimeout())
                .option(ChannelOption.SO_KEEPALIVE, true).handler(new RpcClientInitializer());


        ChannelFuture future = bootstrap.connect(new InetSocketAddress(registry.getHost(), registry.getPort())).sync();

        if (future.isCancelled()) {
            if (logger.isInfoEnabled()) {
                logger.info("connect canceled");
            }
        } else if (!future.isSuccess()) {
            if (logger.isErrorEnabled()) {
                logger.error("error happened when connect to server {}/{}", registry.getHost(), registry.getPort());
            }
        }

        Channel channel = future.channel();

        Attribute<Map<Integer, Object>> attribute = channel.attr(ChannelUtils.DATA_MAP_ATTRIBUTE_KEY);
        ConcurrentHashMap<Integer, Object> dataMap = new ConcurrentHashMap<>();
        attribute.set(dataMap);
        return channel;
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
