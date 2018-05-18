package cn.zzu.rpc;

/**
 * 这个类有且只会有两个线程访问：一个是get时wait掉的线程，一个是接收到结果时写入结果的线程。
 */
public final class InvocationFuture {
    private final Object monitor = new Object();
    private Object result;
    private volatile boolean writable = result == null;
    private InvocationFutureRepo futureRepo;

    public InvocationFuture(final InvocationFutureRepo futureRepo) {
        this.futureRepo = futureRepo;
    }

    public boolean isWritable() {
        return writable;
    }

    public boolean isReadable() {
        return !writable;
    }

    public Object get() {
        synchronized (monitor) {
            Object res;
            if (this.result == null) {
                try {
                    monitor.wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            res = result;
            result = null;
            writable = true;
            futureRepo.reuse(this);
            monitor.notify();
            return res;
        }
    }

    public void set(final Object result) {
        synchronized (monitor) {
            if (this.result != null) {
                try {
                    monitor.wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.result = result;
            writable = false;
            monitor.notify();
        }
    }

    public void cancel() {
        synchronized (monitor) {
            if (result != null) {
                result = null;
            }
            futureRepo.reuse(this);
        }
    }
}
