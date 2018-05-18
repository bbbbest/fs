package cn.zzu.rpc;

import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public final class InvocationFutureRepo {

    private static final int CACHE_SIZE = 2048;
    private static InvocationFutureRepo repo = new InvocationFutureRepo();
    private BlockingQueue<InvocationFuture> usableFutures;
    private ConcurrentHashMap<Integer, InvocationFuture> usedFutures;

    private InvocationFutureRepo() {
        usableFutures = new ArrayBlockingQueue<>(CACHE_SIZE);
        usedFutures = new ConcurrentHashMap<>();
    }

    public static InvocationFutureRepo getInstance() {
        return repo;
    }

    public void put(Integer t) {
        InvocationFuture f = usableFutures.poll();
        if (f == null) {
            f = new InvocationFuture(this);
        }
        usedFutures.put(t, f);
    }

    /*
     * t -> invocationID
     **/
    public InvocationFuture get(Integer t) {
        InvocationFuture f = usedFutures.get(t);
        if (f == null) throw new NoSuchElementException();
        return f;
    }

    /*
     * package-private
     *
     **/
    void reuse(InvocationFuture future) {
        usedFutures.values().remove(future);
        usableFutures.offer(future);
    }
}
