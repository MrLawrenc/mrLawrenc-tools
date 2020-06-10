package com.github.mrlawrenc.thread;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author hz20035009-逍遥
 * date  2020/5/25 15:37
 * 定义可序列化任务的线程池
 */
@Slf4j
public class SerializablePoolExecutor {

    //private final ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("hz-ins-provider-%d").build();

    private final DefaultThreadFactory factory = new DefaultThreadFactory();
    private final InnerThreadPoolExecutor pool;

    private final BlockingQueue<Runnable> workQueue;

    public SerializablePoolExecutor(int corePoolSize,
                                    int maximumPoolSize,
                                    long keepAliveTime,
                                    TimeUnit unit,
                                    BlockingQueue<Runnable> workQueue, RejectedExecutionHandler rejectedHandler) {
        this.workQueue = workQueue;
        this.pool = new InnerThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit
                , workQueue, factory, rejectedHandler);


    }

    public SerializablePoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, int queueLength) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, new ArrayBlockingQueue<>(queueLength)
                , (runnable, executor) -> {
                    String word = executor.isShutdown() ? "stopped" : "full";
                    if (log.isErrorEnabled()) {
                        log.error("Current pool state is {}!", word);
                    }
                    throw new RuntimeException(String.format("Current pool state is %s!", word));
                });
    }

    /**
     * 非阻塞的获取队列中所有元素
     * @return 剩余任务
     */
    public List<Runnable> peekQueueTask() {
        List<Runnable> runnableList = new ArrayList<>(workQueue.size());
        while (true) {
            Runnable poll = workQueue.poll();
            if (poll == null) {
                break;
            }
            runnableList.add(poll);
        }
        return runnableList;
    }


    public List<Thread> getAllThread() {
        return factory.getThreadList();
    }


    public void execute(SerializableRunnable runnable) {
        pool.execute(runnable);
    }


    public <V> Future<V> submit(SerializableCallable<V> callable) {
        if (callable == null) {
            throw new NullPointerException();
        }
        SerializableFutureTask<V> futureTask = new SerializableFutureTask<>(callable);
        execute(futureTask);
        return futureTask;

    }


    public Future<Object> submit(SerializableRunnable runnable) {
        SerializableCallable<Object> callable = new RunnableAdapter<>(runnable, null);
        SerializableFutureTask<Object> futureTask = new SerializableFutureTask<>(callable);
        execute(runnable);
        return futureTask;
    }


    public <T> Future<T> submit(SerializableRunnable runnable, T result) {
        SerializableCallable<T> callable = new RunnableAdapter<>(runnable, result);
        SerializableFutureTask<T> myTask = new SerializableFutureTask<>(callable);
        execute(runnable);
        return myTask;
    }


    public void shutdown() {
        pool.shutdown();
    }


    public List<Runnable> shutdownNow() {
        return pool.shutdownNow();
    }


    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return pool.awaitTermination(timeout, unit);
    }


    public int getCorePoolSize() {
        return pool.getCorePoolSize();
    }


    public int getMaxPoolSize() {
        return pool.getMaximumPoolSize();
    }


    public int getKeepAliveSeconds() {
        return (int) pool.getKeepAliveTime(TimeUnit.SECONDS);
    }


    public int getPoolSize() {
        return pool.getPoolSize();
    }


    public int getActiveCount() {
        return pool.getActiveCount();
    }


    public boolean prestartAllCoreThreads() {
        return pool.prestartAllCoreThreads() > 0;
    }

    /**
     * 包装callable为runnable
     *
     * @param <T> 结果值
     */
    static final class RunnableAdapter<T> implements SerializableCallable<T> {
        final Runnable task;
        final T result;

        RunnableAdapter(Runnable task, T result) {
            this.task = task;
            this.result = result;
        }


        public T call() {
            task.run();
            return result;
        }
    }

    private static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        @Getter
        private final List<Thread> threadList = new ArrayList<>();

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "poolExecutor-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }


        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            threadList.add(t);
            return t;
        }
    }

    private static class InnerThreadPoolExecutor extends ThreadPoolExecutor {

        public InnerThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        public InnerThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        }

        public InnerThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
        }

        public InnerThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        }

        /**
         * 预留扩展
         */
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
        }


        protected void beforeExecute(Thread t, Runnable r) {
            super.beforeExecute(t, r);
        }

    }
}
