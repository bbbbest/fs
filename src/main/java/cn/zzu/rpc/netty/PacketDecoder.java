package cn.zzu.rpc.netty;

import cn.zzu.ss.core.Deserializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public final class PacketDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf buf, final List<Object> out) throws Exception {
        short magic = buf.readShort();
        Packet.checkMagic(magic);
        PacketType type = PacketType.valueOf(buf.readByte());
        int invocationID = buf.readInt();
        int len = buf.readableBytes();
        byte[] data = new byte[len];
        buf.readBytes(data);
        Deserializer deserializer = new Deserializer(data);
        Packet packet = new Packet(type, invocationID, deserializer.readObject());

        out.add(packet);
    }
}
