package cn.zzu.rpc.netty;

import cn.zzu.rpc.Invocation;
import cn.zzu.rpc.Invoker;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Map;

@ChannelHandler.Sharable
public class InvocationServerHandler extends ChannelInboundHandlerAdapter {

    private Map<String, Invoker<?>> cachedInvoker;

    public InvocationServerHandler(final Map<String, Invoker<?>> cachedInvoker) {
        this.cachedInvoker = cachedInvoker;
    }

    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        Packet packet = (Packet) msg;

        if (packet.getType() == PacketType.ASK_INVOCATION.value()) {
            Invocation invocation = (Invocation) packet.getBody();
            Invoker invoker = cachedInvoker.get(invocation.getIdentifier());
            Packet resp;
            int invocationID = packet.getInvocationID();
            if (invoker == null) {
                resp = Packet.missService(invocationID, invocation.getIdentifier());
            } else {
                try {
                    resp = Packet.result(invocationID, invoker.invoke(invocation));
                } catch (NoSuchMethodException e) {
                    resp = Packet.invokeFailed(invocationID, "no such method -> " + invocation.getMethod());
                } catch (Exception e) {
                    resp = Packet.invokeFailed(invocationID, e.getMessage());
                }
            }
            ctx.writeAndFlush(resp);

        } else {
            ctx.fireExceptionCaught(new InvalidPacketException("Server received a invalid packet[type invalid]"));
        }
    }
}
