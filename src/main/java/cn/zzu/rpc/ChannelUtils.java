package cn.zzu.rpc;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.Map;

class ChannelUtils {
    public static final int MESSAGE_LENGTH = 16;
    public static final AttributeKey<Map<Integer, Object>> DATA_MAP_ATTRIBUTE_KEY = new AttributeKey<>("dataMap");

    public static <T> void putCallback2DataMap(Channel channel, int seq, T callback) {
        channel.attr(DATA_MAP_ATTRIBUTE_KEY).get().put(seq, callback);
    }

    public static <T> T removeCallback(Channel channel, int seq) {
        return (T) channel.attr(DATA_MAP_ATTRIBUTE_KEY).get().remove(seq);
    }
}
