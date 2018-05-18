package cn.zzu.rpc.netty;

import cn.zzu.ss.core.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public final class PacketEncoder extends MessageToByteEncoder<Packet> {
    @Override
    protected void encode(final ChannelHandlerContext ctx, final Packet msg, final ByteBuf out) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeShort(msg.getMagic());
        buf.writeByte(msg.getType());
        buf.writeInt(msg.getInvocationID());
        Serializer serializer = new Serializer();
        serializer.writeObject(msg.getBody());
        byte[] data = serializer.getSerialData();
        buf.writeBytes(data);

        out.writeBytes(buf);
    }
}
