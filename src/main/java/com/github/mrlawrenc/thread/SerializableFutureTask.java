package com.github.mrlawrenc.thread;


import lombok.SneakyThrows;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author hz20035009-逍遥
 * date  2020/5/22 10:06
 * 扩展自{@link SerializableRunnable}和{@link Future}
 * 包装有返回值的任务
 */
public class SerializableFutureTask<V> implements SerializableRunnable, Future<V> {
    private final SerializableCallable<V> callable;

    private V result = null;

    public SerializableFutureTask(SerializableCallable<V> callable) {
        this.callable = callable;
    }

    public SerializableCallable<V> getCallable() {
        return callable;
    }

    @SneakyThrows
    @Override
    public void run() {
        synchronized (this) {
            result = callable.call();
            notify();
        }
    }


    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        if (result == null) {
            synchronized (this) {
                if (result == null) {
                    wait();
                }

            }
        }
        return result;
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }
}