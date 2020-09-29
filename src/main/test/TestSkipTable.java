import com.github.mrlawrenc.utils.collections.SkipTable;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.function.Supplier;

/**
 * @author hz20035009-逍遥
 * date   2020/9/29 15:47
 */
public class TestSkipTable {

    @Test
    public void t() {
        SecureRandom secureRandom = new SecureRandom();
        Supplier<Boolean> supplier = () -> new SecureRandom().nextDouble() < 0.5;
        SkipTable skipTable = new SkipTable(true, supplier);

        int a=0;
        int b=0;
        int c=0;

        for (int i = 0; i < 10000; i++) {
            int i1 =secureRandom.nextInt(100000) + i;
            if (i==3000){
                a=i1;
            }
            if (i==6000){
                b=i1;
            }
            if (i==9501){
                c=i1;
            }
            skipTable.insert(i1);
        }
        System.out.println("开始查找");
        long start = System.nanoTime();
        System.out.println(skipTable.findNearLeft(a));
        System.out.println(System.nanoTime()-start+"ns");
        System.out.println(skipTable.findNearLeft(b));
        System.out.println(System.nanoTime()-start+"ns");
        System.out.println(skipTable.findNearLeft(c));
        System.out.println(System.nanoTime()-start+"ns");
    }
}