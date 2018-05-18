package cn.zzu.rpc;

import cn.zzu.rpc.netty.Packet;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.SocketChannel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public class DefaultInvocationHandler implements InvocationHandler {
    private String identifier;
    private Class<?> i;
    private SocketChannel channel;

    public DefaultInvocationHandler(final String identifier, final Class<?> i, final SocketChannel channel) {
        this.identifier = identifier;
        this.i = i;
        this.channel = channel;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) {
        Invocation invocation = new Invocation(identifier, i, method, args);
        Packet packet = Packet.invocation(invocation);
        InvocationFutureRepo.getInstance().put(invocation.getInvocationID());
        channel.writeAndFlush(packet).addListener((ChannelFutureListener) future->{
            if (!future.isSuccess()) {
                rpcSendFailed(invocation);
            }
        });

        Object o = InvocationFutureRepo.getInstance().get(invocation.getInvocationID()).get();
        if (o instanceof Exception) {
            throw (RpcException) o;
        }
        return o;
    }

    private void rpcSendFailed(Invocation invocation) {
        InvocationFutureRepo.getInstance().get(invocation.getInvocationID()).cancel();
        throw new RpcException(String
                .format("Failed call: %s - %s", invocation.getIdentifier(), invocation.getMethod()));
    }
}
