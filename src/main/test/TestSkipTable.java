import com.github.mrlawrenc.filter.entity.Request;
import com.github.mrlawrenc.filter.entity.Response;
import com.github.mrlawrenc.filter.service.FilterChain;
import com.github.mrlawrenc.filter.service.FirstFilter;
import com.github.mrlawrenc.utils.collections.SkipTable;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

/**
 * @author hz20035009-逍遥
 * date   2020/9/29 15:47
 */
public class TestSkipTable extends FirstFilter {

    @Test
    public void t() {
        int num = 1000000;
        Supplier<Boolean> supplier = () -> new SecureRandom().nextDouble() < 0.5;
        SkipTable skipTable = new SkipTable(false, supplier);

        int a = 0;
        int b = 0;
        int c = 0;
        List<Integer> list = new ArrayList<>();
        long s0 = System.nanoTime();
        for (int i = 0; i < num; i++) {
            list.add(i);
        }
        System.out.println("数组插入耗时:" + (System.nanoTime() - s0) + "ns");


        long s1 = System.nanoTime();
        for (int i = 0; i < num; i++) {
            if (i == 3000) {
                a = i;
            }
            if (i == 6000) {
                b = i;
            }
            if (i == 9501) {
                c = i;
            }
            skipTable.insert(i);
        }
        System.out.println("跳表插入耗时:" + (System.nanoTime() - s1) + "ns  跳表开始查找");
        long start = System.nanoTime();
        System.out.println(skipTable.findNearLeft(a));
        long end1 = System.nanoTime();
        System.out.println(end1 - start + "ns");
        System.out.println(skipTable.findNearLeft(b));
        long end2 = System.nanoTime();
        System.out.println(end2 - end1 + "ns");
        System.out.println(skipTable.findNearLeft(c));
        System.out.println(System.nanoTime() - end2 + "ns");

        long ss = System.nanoTime();
     /*   int i = list.indexOf(a);
        int i1 = list.indexOf(b);*/
        int i2 = list.indexOf(c);

        LockSupport.park();
        LockSupport.unpark(Thread.currentThread());

        System.out.println("数组循环查找耗时:" + (System.nanoTime() - ss) + "ns");
    }
}