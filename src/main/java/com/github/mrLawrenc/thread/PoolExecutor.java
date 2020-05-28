package com.github.mrLawrenc.thread;


import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author hz20035009-逍遥
 * date  2020/5/25 15:37
 * 定义池规范，
 */
public interface PoolExecutor {

    void execute(SerializableRunnable task);

    Future<?> submit(SerializableRunnable task);

    <T> Future<T> submit(SerializableCallable<T> task);

    int getCorePoolSize();

    int getMaxPoolSize();

    int getKeepAliveSeconds();

    int getPoolSize();

    int getActiveCount();

    void shutdown();

    List<Runnable> shutdownNow();

    boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;

    List<Thread> getAllThread();

    boolean prestartAllCoreThreads();
}
