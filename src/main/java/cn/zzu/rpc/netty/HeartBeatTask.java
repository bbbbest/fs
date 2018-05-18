package cn.zzu.rpc.netty;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartBeatTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger("heartbeatTaskLogger");

    private ChannelHandlerContext ctx;

    public HeartBeatTask(final ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void run() {
        ctx.writeAndFlush(Packet.heartbeatReq());
    }
}
