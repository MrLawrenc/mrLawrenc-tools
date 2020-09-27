import com.github.mrlawrenc.utils.clz.ClassLoaderUtil;
import com.github.mrlawrenc.utils.clz.CompileUtil;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hz20035009-逍遥
 * date   2020/9/25 13:50
 */
public class TestCompile {
    private final CompileUtil compileUtil = new CompileUtil();


    @Test
    public void compileFile() throws Exception {
        compileUtil.compileFile(new File("F:\\tmp\\Test.java"));
    }

    @Test
    public void compileFile2Writer() throws Exception {
        compileUtil.compileFileErrorOut2Writer(new File("F:\\tmp\\Test.java"), new FileWriter(new File("E:\\Test.class")));
    }


    @Test
    public void compileFileOutMemory() throws Exception {
        Map<String, byte[]> map = new HashMap<>();
        compileUtil.compileFileOutMemory(map, new File("F:\\tmp\\Test.java"));

        map.keySet().forEach(System.out::println);
    }

    @Test
    public void compileSourceCode2Memory() throws Exception {
        String sourceCode = "package com;public class Test{\n" +
                "  @Override\n" +
                "  public String toString() {\n" +
                "    return \"hello java compiler\";\n" +
                "  }\n" +
                "}";

        Map<String, byte[]> map = new HashMap<>();
        compileUtil.compileSourceCode2Memory("Test", sourceCode, map);

        map.keySet().forEach(System.out::println);
    }


    /**
     * 测试 源码编译-class加载
     */
    @Test
    public void testLoad() throws Exception {
        ClassLoaderUtil loaderUtil = new ClassLoaderUtil();
        String sourceCode = "package com;public class Test{\n" +
                "  @Override\n" +
                "  public String toString() {\n" +
                "    return \"hello java compiler\";\n" +
                "  }\n" +
                "}";

        Map<String, byte[]> map = new HashMap<>();
        compileUtil.compileSourceCode2Memory("Test", sourceCode, map);
        map.keySet().forEach(k -> {
            Class<?> r = loaderUtil.loadClass0(k, map.get(k));
            try {
                System.out.println(r.newInstance().toString());
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });

    }

}