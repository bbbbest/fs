package cn.zzu.rpc.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
public class HeartbeatClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger("HeartbeatReqLogger");
    private static final int HEARTBEAT_RATE = 10;
    private volatile ScheduledFuture<?> heartbeat;

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        if (heartbeat != null) {
            heartbeat.cancel(true);
            heartbeat = null;
        }
    }

    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        Packet packet = (Packet) msg;
        if (packet.getType() == PacketType.HEARTBEAT_REQ.value() && packet.getBody() == null) {
            heartbeat = ctx.executor().scheduleAtFixedRate(new HeartBeatTask(ctx), 0, HEARTBEAT_RATE, TimeUnit.SECONDS);
        } else if (packet.getType() == PacketType.HEARTBEAT_RESP.value()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Client receive server heartbeat response packet.");
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
