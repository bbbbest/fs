package cn.zzu.rpc.netty;

import cn.zzu.rpc.InvocationFuture;
import cn.zzu.rpc.InvocationFutureRepo;
import cn.zzu.rpc.RpcException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@ChannelHandler.Sharable
public class InvocationClientHandler extends ChannelInboundHandlerAdapter {
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        Packet packet = (Packet) msg;
        int invocationID = packet.getInvocationID();
        InvocationFuture future = InvocationFutureRepo.getInstance().get(invocationID);
        PacketType type = PacketType.valueOf(packet.getType());
        switch (type) {
            case RESULT_INVOCATION:
                future.set(packet.getBody());
                break;
            case MISS_SERVICE:
                future.set(RpcException.noSuchService((String) packet.getBody()));
                break;
            case INVOKE_FAILED:
                future.set(RpcException.noSuchService((String) packet.getBody()));
                break;
            default:
                throw new InvalidPacketException("invalid packet");
        }
    }

    public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

}
