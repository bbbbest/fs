package cn.zzu.rpc;

import java.util.concurrent.atomic.AtomicLong;

final class IDCreator {
    private AtomicLong seed;

    public IDCreator(final long start) {
        this.seed = new AtomicLong(start);
    }

    public long next() {
        return this.seed.getAndIncrement();
    }
}
