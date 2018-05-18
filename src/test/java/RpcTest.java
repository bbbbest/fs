import cn.zzu.rpc.ChannelPool;
import cn.zzu.rpc.Invocation;
import cn.zzu.rpc.InvocationFutureRepo;
import cn.zzu.rpc.RpcRegistry;
import cn.zzu.rpc.netty.Packet;
import org.junit.jupiter.api.Test;
import test.AnimalAction;
import test.Man;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RpcTest {
    @Test
    void blocking_queue() throws NoSuchMethodException, InterruptedException {
        Method method = AnimalAction.class.getDeclaredMethod("learn", Object.class);
        Invocation invocation = new Invocation("action", AnimalAction.class, method, new Object[]{new Man()});

        InvocationFutureRepo repo = InvocationFutureRepo.getInstance();

        Thread t1 = new Thread(()->{
            repo.put(invocation.getInvocationID());

            System.out.println(repo.get(invocation.getInvocationID()).get());
        });
        Thread t2 = new Thread(()->{
            repo.get(invocation.getInvocationID()).set(666);
        });

        t1.start();
        Thread.sleep(2000);
        t2.start();

        System.out.println(repo);

    }

    @Test
    void uuid() {
        System.out.println(UUID.randomUUID().toString());
    }

    @Test
    void concurrent_map() {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        System.out.println(map.get("666"));
    }

    @Test
    void packet_heartbeat_hash() {
        Packet p = Packet.heartbeatReq();
        System.out.println(p.getInvocationID());
    }

    @Test
    void proxy() {
        AnimalAction action = (AnimalAction) Proxy.newProxyInstance(Thread.currentThread()
                .getContextClassLoader(), new Class[]{AnimalAction.class}, new InvocationHandler() {
            @Override
            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                return method.invoke(new Man(), args);
            }
        });
        action.eat();
    }


    @Test
    void future() throws ExecutionException, InterruptedException {
        Callable callable = new Callable() {
            @Override
            public Object call() throws Exception {
                return "666";
            }
        };

        ExecutorService service = Executors.newFixedThreadPool(5, new ThreadFactory() {
            private AtomicInteger threadID = new AtomicInteger(1);

            @Override
            public Thread newThread(final Runnable r) {
                return new Thread(r, "invoke-thread-" + threadID.getAndIncrement());
            }
        });

        Future future = service.submit(callable);
        System.out.println(future.get());
    }

    @Test
    void channel_pool() throws InterruptedException {
        ChannelPool pool = new ChannelPool(new RpcRegistry("127.0.0.1", 8000));

        System.out.println(pool.syncGetChannel());
    }
}
