import org.jctools.queues.*;
import org.junit.Test;

import java.util.Queue;

/**
 * @author hz20035009-逍遥
 * date   2020/9/28 14:41
 */
public class TestJcTool {

    @Test
    public void test() {

        // spsc-有界/无界队列
        Queue<String> spscArrayQueue = new SpscArrayQueue<>(16);
        Queue<String> spscUnboundedArrayQueue = new SpscUnboundedArrayQueue<>(2);
        // spmc-有界队列
        Queue<String> spmcArrayQueue = new SpmcArrayQueue<>(16);
        // mpsc-有界/无界队列
        Queue<String> mpscArrayQueue = new MpscArrayQueue<>(16);
        Queue<String> mpscChunkedArrayQueue = new MpscChunkedArrayQueue<>(1024, 8 * 1024);
        Queue<String> mpscUnboundedArrayQueue = new MpscUnboundedArrayQueue<>(2);
        // mpmc-有界队列
        Queue<String> mpmcArrayQueue = new MpmcArrayQueue<>(16);

    }
}