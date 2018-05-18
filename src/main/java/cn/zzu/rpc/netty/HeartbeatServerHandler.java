package cn.zzu.rpc.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class HeartbeatServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger("HeartbeatRespLogger");

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        ctx.writeAndFlush(Packet.requireHeartbeat());
    }

    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        Packet packet = (Packet) msg;
        if (packet.getType() == PacketType.HEARTBEAT_REQ.value()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Server receive client heartbeat request packet.");
            }
            ctx.writeAndFlush(Packet.heartbeatResp());
        } else {
            ctx.fireChannelRead(msg);
        }
    }

}
