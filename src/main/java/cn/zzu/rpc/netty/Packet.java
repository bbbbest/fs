package cn.zzu.rpc.netty;

import cn.zzu.rpc.Invocation;
import cn.zzu.rpc.RpcException;
import cn.zzu.ss.core.SS;

@SS
public final class Packet {
    private static final short magic = (short) 0xcafe;
    private byte type;
    private int invocationID;
    private Object body;

    public Packet() {
    }

    public Packet(final PacketType type, final int invocationID, final Object body) {
        this.type = type.value();
        this.invocationID = invocationID;
        this.body = body;
    }

    public Packet(final PacketType type, final Object body) {
        this.type = type.value();
        this.body = body;
    }

    public static Packet requireHeartbeat() {
        return new Packet(PacketType.HEARTBEAT_REQ, null);
    }

    public static Packet heartbeatReq() {
        return new Packet(PacketType.HEARTBEAT_REQ, System.currentTimeMillis());
    }

    public static Packet heartbeatResp() {
        return new Packet(PacketType.HEARTBEAT_RESP, System.currentTimeMillis());
    }

    public static Packet invocation(final Invocation body) {
        return new Packet(PacketType.ASK_INVOCATION, body.getInvocationID(), body);
    }

    public static Packet result(final int invocationID, final Object body) {
        return new Packet(PacketType.RESULT_INVOCATION, invocationID, body);
    }

    public static Packet missService(final int invocationID, final Object body) {
        return new Packet(PacketType.MISS_SERVICE, invocationID, body);
    }

    public static Packet invokeFailed(final int invocationID, final Object body) {
        return new Packet(PacketType.INVOKE_FAILED, invocationID, body);
    }


    public static void checkMagic(short magic) {
        if (magic != Packet.magic) {
            throw RpcException.invalidMagic();
        }
    }

    public byte getType() {
        return type;
    }

    public void setType(final PacketType type) {
        this.type = type.value();
    }

    public Object getBody() {
        return body;
    }

    public void setBody(final Object body) {
        this.body = body;
    }

    public short getMagic() {
        return magic;
    }

    public int getInvocationID() {
        return invocationID;
    }

    @Override
    public String toString() {
        return "Packet{" + "type=" + type + ", invocationID=" + invocationID + ", body=" + body + '}';
    }
}
