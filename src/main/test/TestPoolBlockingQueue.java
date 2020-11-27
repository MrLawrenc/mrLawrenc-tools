import com.github.mrlawrenc.thread.pool.DynamicThreadPool;
import com.github.mrlawrenc.thread.pool.PoolBlockingQueue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 测试队列性能
 *
 * @author hz20035009-逍遥
 * date   2020/11/27 9:53
 */
public class TestPoolBlockingQueue {

    DynamicThreadPool newPool = new DynamicThreadPool(Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors(),
            3, TimeUnit.MINUTES
            , new PoolBlockingQueue<>(num));


    ThreadPoolExecutor jdkPool = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors(),
            3, TimeUnit.MINUTES
            , new ArrayBlockingQueue<>(num));

    private static int num = 1000;
    private long start;

    @Before
    public void before() {
        start = System.currentTimeMillis();
    }

    @After
    public void after() {
        System.out.println("耗时:" + (System.currentTimeMillis() - start));
    }

    @Test
    public void testNew() {
        List<Future<?>> submit = submit(newPool, num);
        submit.forEach(f -> {
            try {
                f.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    @Test
    public void testJdk() {

        List<Future<?>> submit = submit(newPool, num);
        submit.forEach(f -> {
            try {
                f.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public List<Future<?>> submit(ThreadPoolExecutor pool, int num) {
        return IntStream.range(0, num).mapToObj(i -> pool.submit(this::task)).collect(Collectors.toList());
    }

    public void task() {
        try {
            TimeUnit.MILLISECONDS.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}