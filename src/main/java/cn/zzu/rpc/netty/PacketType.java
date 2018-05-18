package cn.zzu.rpc.netty;

public enum PacketType {
    ASK_INVOCATION((byte) 0x70),     // 请求调用
    RESULT_INVOCATION((byte) 0x71),  // 调用结果
    HEARTBEAT_REQ((byte) 0x72),             // 心跳包
    HEARTBEAT_RESP((byte) 0x73),            // 心跳包回复
    MISS_SERVICE((byte) 0x74),              // 服务缺失
    INVOKE_FAILED((byte) 0x75);             // 调用失败

    private byte val;

    PacketType(final byte val) {
        this.val = val;
    }

    public static PacketType valueOf(byte val) {
        PacketType type;
        switch (val) {
            case 0x70:
                type = PacketType.ASK_INVOCATION;
                break;
            case 0x71:
                type = PacketType.RESULT_INVOCATION;
                break;
            case 0x72:
                type = PacketType.HEARTBEAT_REQ;
                break;
            case 0x73:
                type = PacketType.HEARTBEAT_RESP;
                break;
            case 0x74:
                type = PacketType.MISS_SERVICE;
                break;
            case 0x75:
                type = PacketType.INVOKE_FAILED;
                break;
            default:
                throw new InternalError();
        }
        return type;
    }

    public byte value() {
        return val;
    }
}
